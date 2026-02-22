package com.authify.backend.service;

import com.authify.backend.model.EmailVerificationToken;
import com.authify.backend.model.PasswordResetToken;
import com.authify.backend.model.RefreshToken;
import com.authify.backend.model.User;
import com.authify.backend.repository.EmailVerificationTokenRepository;
import com.authify.backend.repository.PasswordResetTokenRepository;
import com.authify.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    // Refresh Token
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiry))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyRefreshTokenExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token has expired. Please login again.");
        }
        return token;
    }

    @Transactional
    public void deleteRefreshTokensByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // Password Reset Token
    public PasswordResetToken createPasswordResetToken(User user) {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(3600000)) // 1 hour
                .build();
        return passwordResetTokenRepository.save(token);
    }

    public Optional<PasswordResetToken> findPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    // Email Verification Token
    public EmailVerificationToken createEmailVerificationToken(User user) {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(86400000)) // 24 hours
                .build();
        return emailVerificationTokenRepository.save(token);
    }

    public Optional<EmailVerificationToken> findEmailVerificationToken(String token) {
        return emailVerificationTokenRepository.findByToken(token);
    }
}
