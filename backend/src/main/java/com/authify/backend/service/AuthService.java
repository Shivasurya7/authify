package com.authify.backend.service;

import com.authify.backend.dto.*;
import com.authify.backend.exception.InvalidTokenException;
import com.authify.backend.exception.TokenExpiredException;
import com.authify.backend.exception.UserAlreadyExistsException;
import com.authify.backend.model.*;
import com.authify.backend.repository.RoleRepository;
import com.authify.backend.repository.UserRepository;
import com.authify.backend.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final TfaService tfaService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);

        // Send verification email
        EmailVerificationToken verificationToken = tokenService.createEmailVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());

        return new MessageResponse("Registration successful! Please check your email to verify your account.");
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if 2FA is enabled
        if (user.isTfaEnabled()) {
            if (request.getTfaCode() == null || request.getTfaCode().isBlank()) {
                return AuthResponse.builder()
                        .message("2FA code required")
                        .email(user.getEmail())
                        .tfaEnabled(true)
                        .tfaRequired(true)
                        .build();
            }

            if (!tfaService.verifyCode(user.getTfaSecret(), request.getTfaCode())) {
                throw new IllegalArgumentException("Invalid 2FA code");
            }
        }

        // Generate access token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        addAccessTokenCookie(response, accessToken);

        // Handle remember me
        if (request.isRememberMe()) {
            RefreshToken refreshToken = tokenService.createRefreshToken(user);
            addRefreshTokenCookie(response, refreshToken.getToken());
        }

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .message("Login successful")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .tfaEnabled(user.isTfaEnabled())
                .tfaRequired(false)
                .build();
    }

    @Transactional
    public MessageResponse logout(String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue != null) {
            tokenService.findRefreshToken(refreshTokenValue).ifPresent(token -> {
                tokenService.deleteRefreshTokensByUser(token.getUser());
            });
        }

        clearAuthCookies(response);
        return new MessageResponse("Logout successful");
    }

    public AuthResponse refreshToken(String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue == null) {
            throw new InvalidTokenException("Refresh token not found");
        }

        RefreshToken refreshToken = tokenService.findRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        tokenService.verifyRefreshTokenExpiration(refreshToken);

        User user = refreshToken.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        addAccessTokenCookie(response, accessToken);

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .message("Token refreshed")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .tfaEnabled(user.isTfaEnabled())
                .tfaRequired(false)
                .build();
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenService.findEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new TokenExpiredException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        return new MessageResponse("Email verified successfully!");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            PasswordResetToken resetToken = tokenService.createPasswordResetToken(user);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
        });

        // Always return success to prevent email enumeration
        return new MessageResponse("If an account exists with that email, a password reset link has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        PasswordResetToken resetToken = tokenService.findPasswordResetToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (resetToken.isExpired()) {
            throw new TokenExpiredException("Reset token has expired");
        }

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Reset token has already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);

        return new MessageResponse("Password reset successfully!");
    }

    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .message("User info")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .tfaEnabled(user.isTfaEnabled())
                .tfaRequired(false)
                .build();
    }

    // TFA Methods
    @Transactional
    public TfaSetupResponse enableTfa(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String secret = tfaService.generateSecret();
        user.setTfaSecret(secret);
        userRepository.save(user);

        String qrCodeUri = tfaService.generateQrCodeUri(secret, email);

        return TfaSetupResponse.builder()
                .secret(secret)
                .qrCodeUri(qrCodeUri)
                .message("Scan the QR code with your authenticator app, then verify with a code")
                .build();
    }

    @Transactional
    public MessageResponse verifyAndActivateTfa(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTfaSecret() == null) {
            throw new IllegalStateException("TFA setup not initiated");
        }

        if (!tfaService.verifyCode(user.getTfaSecret(), code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setTfaEnabled(true);
        userRepository.save(user);

        return new MessageResponse("Two-factor authentication enabled successfully!");
    }

    @Transactional
    public MessageResponse disableTfa(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTfaEnabled(false);
        user.setTfaSecret(null);
        userRepository.save(user);

        return new MessageResponse("Two-factor authentication disabled");
    }

    // Cookie helpers
    private void addAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(900); // 15 minutes
        response.addCookie(cookie);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(604800); // 7 days
        response.addCookie(cookie);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/api/auth/refresh");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}
