package com.jaeseok.groupStudy.auth.infrastructure.jwt;

import com.jaeseok.groupStudy.auth.application.LoadUserPrincipalService;
import com.jaeseok.groupStudy.auth.application.TokenProvider;
import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider implements TokenProvider {

    private final Key key;
    private final long accessTokenValidationInMilliSec;
    private final UserDetailsService userDetailsService;
    private final LoadUserPrincipalService loadUserPrincipalService;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validation-in-seconds}")
            long accessTokenValidationInSeconds,
            UserDetailsService userDetailsService, LoadUserPrincipalService loadUserPrincipalService
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidationInMilliSec = accessTokenValidationInSeconds * 1000;
        this.userDetailsService = userDetailsService;
        this.loadUserPrincipalService = loadUserPrincipalService;
    }

    @Override
    public String generateToken(Authentication authentication) {
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        Long userId = userDetails.userId();

        long now = new Date().getTime();
        Date accessTokenExpiresIn = new Date(now + this.accessTokenValidationInMilliSec);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(accessTokenExpiresIn)
                .signWith(this.key)
                .compact();
    }

    @Override
    public Authentication getAuthentication(String accessToken) {
        String userId = Jwts.parser()
                .setSigningKey(this.key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getSubject();

        UserDetails userDetails = loadUserPrincipalService.loadUserById(Long.valueOf(userId));

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(this.key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않는 토큰 입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("토큰이 존재하지 않습니다.", e);
        }

        return false;
    }
}
