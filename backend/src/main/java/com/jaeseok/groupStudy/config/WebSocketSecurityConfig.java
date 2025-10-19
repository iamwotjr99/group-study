package com.jaeseok.groupStudy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.web.csrf.CsrfChannelInterceptor;
import org.springframework.security.messaging.web.csrf.XorCsrfChannelInterceptor;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages

                // CONNECT, DISCONNECT 등 목적지 없는 메시지 처리
                // WebSocket 연결을 시도하는 모든 클라이언트에게 토큰 검증을 요구
                .simpTypeMatchers(
                        SimpMessageType.CONNECT,
                        SimpMessageType.DISCONNECT,
                        SimpMessageType.UNSUBSCRIBE
                ).permitAll()

                // 메시지 전송 권한 (SEND)
                // /pub으로 메시지를 보내는 요청은 인증된 사용자만 혀용
                .simpDestMatchers("/pub/**").authenticated()

                // 에러 큐 메시지 구독 권한 (ERROR)
//                .simpSubscribeDestMatchers("/queue/errors").authenticated()

                // 메시지 구독 권한 (SUBSCRIBE)
                // 해당 경로를 구독하는 요청은 인증된 사용자만 허용
                .simpSubscribeDestMatchers("/sub/chatroom/**", "/sub/signal/**", "/user/**").authenticated()

                // 기본 정책
                // 위 규칙에 해당하지 않는 모든 메시지는 기본적으로 거부
                .anyMessage().denyAll();

        return messages.build();
    }

    @Bean(name = "csrfChannelInterceptor")
    ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                return message;
            }
        };
    }
}
