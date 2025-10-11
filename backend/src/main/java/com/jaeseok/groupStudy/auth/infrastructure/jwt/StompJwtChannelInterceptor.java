package com.jaeseok.groupStudy.auth.infrastructure.jwt;

import com.jaeseok.groupStudy.auth.application.MemberDetailsService;
import com.jaeseok.groupStudy.auth.application.TokenProvider;
import java.util.StringTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final MemberDetailsService memberDetailsService;

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP CONNECT 명령어만 처리
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // HTTP 헤더에서 JWT 추출 (클라이언트가 stomp.connect()시 headers에 담아서 보냄)
            String authorizationHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER_NAME);
            String token = resolveToken(authorizationHeader);

            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                // 토큰 유효성 검사 및 인증 객체 생성
                Authentication authentication = tokenProvider.getAuthentication(token);

                // 세션에 인증 정보 저장 (@AuthenticationPrincipal의 기반)
                accessor.setUser(authentication);
            } else {
                // 토큰이 없거나 유효하지 않은 경우
                throw new SecurityException("유효하지 않은 정보입니다.");
            }
        }

        return message;
    }

    private String resolveToken(String header) {
        if (StringUtils.hasText(header) && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(7);
        }

        return null;
    }
}
