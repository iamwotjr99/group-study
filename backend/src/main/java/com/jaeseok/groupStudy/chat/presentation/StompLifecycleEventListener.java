package com.jaeseok.groupStudy.chat.presentation;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.chat.infrastructure.OnlineParticipantRepository;
import com.jaeseok.groupStudy.chat.infrastructure.dto.ParticipantInfo;
import com.jaeseok.groupStudy.member.application.MemberService;
import com.jaeseok.groupStudy.member.application.dto.MemberInfoDto;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * 웹소켓 연결/해제 즉, 채팅방의 입장과 퇴장 이벤트를 감지하는 이벤트리스너
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StompLifecycleEventListener {
    private final OnlineParticipantRepository onlineParticipantRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MemberService memberService;

    // 사용자가 특정 방을 구독할 때 발생하는 이벤트 -> [입장]
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(event.getMessage(),
                StompHeaderAccessor.class);
        String destination = headerAccessor.getDestination();

        if (destination != null && destination.matches("/sub/chatroom/\\d+$")) {
            Long roomId = Long.parseLong(destination.substring("/sub/chatroom/".length()));

            // 새로운 참여자 정보 생성 및 저장
            // StompJwtInterceptor 에서 검증 후 설정해준 사용자 정보
            Authentication authentication = (Authentication) headerAccessor.getUser();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.userId();

            MemberInfoDto memberInfo = memberService.getMemberInfo(userId);

            String sessionId = headerAccessor.getSessionId();
            ParticipantInfo newParticipantInfo = ParticipantInfo.of(roomId, userId, memberInfo.nickname(),
                    sessionId);
            onlineParticipantRepository.add(roomId, newParticipantInfo);
        }
    }

    // 사용자의 웹소켓 연길이 끊어졌을 때 발생하는 이벤트 -> [퇴장]
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        // 세션 ID를 통해 나간 사용자가 누구인지 찾음
        String sessionId = event.getSessionId();
        ParticipantInfo leavingParticipant = onlineParticipantRepository.remove(sessionId);

        if (leavingParticipant != null) {
            // 나간 사람이 적용된 최신 온라인 참여자 명단 전송
            broadcastParticipantList(leavingParticipant.roomId());
        }
    }

    private void broadcastParticipantList(Long roomId) {
        Set<ParticipantInfo> currentParticipants = onlineParticipantRepository.getParticipants(roomId);
        simpMessagingTemplate.convertAndSend(
                "/sub/chatroom/" + roomId + "/participants",
                currentParticipants
        );
    }
}
