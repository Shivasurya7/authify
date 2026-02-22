package com.authify.backend.controller;

import com.authify.backend.dto.*;
import com.authify.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request,
                                                    HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);
        return ResponseEntity.ok(authService.logout(refreshToken, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request,
                                                      HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);
        return ResponseEntity.ok(authService.refreshToken(refreshToken, response));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
    }

    // TFA Endpoints
    @PostMapping("/tfa/enable")
    public ResponseEntity<TfaSetupResponse> enableTfa(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.enableTfa(userDetails.getUsername()));
    }

    @PostMapping("/tfa/verify")
    public ResponseEntity<MessageResponse> verifyTfa(@AuthenticationPrincipal UserDetails userDetails,
                                                      @Valid @RequestBody VerifyTfaRequest request) {
        return ResponseEntity.ok(authService.verifyAndActivateTfa(userDetails.getUsername(), request.getCode()));
    }

    @PostMapping("/tfa/disable")
    public ResponseEntity<MessageResponse> disableTfa(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.disableTfa(userDetails.getUsername()));
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
