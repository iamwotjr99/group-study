package com.jaeseok.groupStudy.auth.application;

import org.springframework.security.core.Authentication;

public interface TokenProvider {
    String generateToken(Authentication authentication);
    Authentication getAuthentication(String accessToken);
    boolean validateToken(String token);
}
