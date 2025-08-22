package com.jaeseok.groupStudy.auth.infrastructure.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
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
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidationInMilliSec;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validation-in-seconds}")
            long accessTokenValidationInSeconds,
            UserDetailsService userDetailsService
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidationInMilliSec = accessTokenValidationInSeconds * 1000;
        this.userDetailsService = userDetailsService;
    }

    public String generateToken(Authentication authentication) {
        String memberEmail = authentication.getName();

        long now = new Date().getTime();
        Date accessTokenExpiresIn = new Date(now + this.accessTokenValidationInMilliSec);

        return Jwts.builder()
                .subject(memberEmail)
                .expiration(accessTokenExpiresIn)
                .signWith(this.key)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        String email = Jwts.parser()
                .setSigningKey(this.key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

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
