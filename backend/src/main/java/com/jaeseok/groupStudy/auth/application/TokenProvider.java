package com.jaeseok.groupStudy.auth.application;

import java.time.Instant;
import org.springframework.security.core.Authentication;

public interface TokenProvider {
    String generateAccessToken(Authentication authentication);
    String generateRefreshToken(Authentication authentication);
    Authentication getAuthentication(String accessToken);
    boolean validateToken(String token);
    Instant getRefreshTokenExpiryAsInstant();
    long getRefreshTokenValidityInMilliSec();
}
