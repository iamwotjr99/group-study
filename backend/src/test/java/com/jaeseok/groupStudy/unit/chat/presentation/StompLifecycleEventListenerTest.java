package com.jaeseok.groupStudy.unit.chat.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.chat.infrastructure.OnlineParticipantRepository;
import com.jaeseok.groupStudy.chat.infrastructure.dto.ParticipantInfo;
import com.jaeseok.groupStudy.chat.presentation.StompLifecycleEventListener;
import com.jaeseok.groupStudy.member.application.MemberService;
import com.jaeseok.groupStudy.member.application.dto.MemberInfoDto;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("웹소켓 연결/해제 이벤트 리스너 단위 테스트")
class StompLifecycleEventListenerTest {

    @InjectMocks
    StompLifecycleEventListener stompLifecycleEventListener;

    @Mock
    OnlineParticipantRepository onlineParticipantRepository;

    @Mock
    SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    MemberService memberService;

    @Test
    @DisplayName("채팅방 구독 이벤트가 발생하면 참여자 추가 및 메시지 전송 로직이 실행된다.")
    void givenSessionSubscribeEvent_whenHandleSessionSubscribe_thenAddParticipantInMap() {
        // given: 테스트를 위한 가짜 데이터와 이벤트 객체를 준비
        Long roomId = 4L;
        Long userId = 5L;
        String sessionId = "session-abc";
        UserPrincipal userPrincipal = new UserPrincipal(userId, "test@test.com", "password");
        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, null);

        Message<byte[]> message = MessageBuilder
                .withPayload(new byte[0])
                .setHeader(SimpMessageHeaderAccessor.DESTINATION_HEADER, "/sub/chatroom/" + roomId)
                .setHeader(SimpMessageHeaderAccessor.SESSION_ID_HEADER, sessionId)
                .setHeader(SimpMessageHeaderAccessor.USER_HEADER, auth)
                .build();

        SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

        // memberService.getMemberInfo(5L)가 호출되면, 가짜 MemberInfoDto를 반환하도록 설정
        when(memberService.getMemberInfo(userId)).thenReturn(new MemberInfoDto(userId, "testUser", "test@test.com"));

        // when: 실제 테스트할 메서드를 호출합니다.
        stompLifecycleEventListener.handleSessionSubscribe(event);

        // then: onlineParticipantRepository의 add 메서드가 올바른 인자들로 1번 호출되었는지 검증
        verify(onlineParticipantRepository, times(1)).add(eq(roomId), any(ParticipantInfo.class));
    }

    @Test
    @DisplayName("연결 종료 이벤트가 발생하면 참여자가 제거되고, 갱신된 목록이 방송된다")
    void handleSessionDisconnect_RemovesParticipantAndBroadcastsList() {
        // given: 방에 두 명(userA, userB)이 이미 접속해있는 상황을 가정
        Long roomId = 4L;
        ParticipantInfo userA = ParticipantInfo.of(roomId, 5L, "userA", "session-A");
        ParticipantInfo userB = ParticipantInfo.of(roomId, 6L, "userB", "session-B");
        onlineParticipantRepository.add(roomId, userA);
        onlineParticipantRepository.add(roomId, userB);

        // userA가 나가는 상황을 가정
        when(onlineParticipantRepository.remove(userA.sessionId())).thenReturn(userA);

        Message<byte[]> message = MessageBuilder
                .withPayload(new byte[0])
                .setHeader(SimpMessageHeaderAccessor.DESTINATION_HEADER, "/sub/chatroom/" + roomId)
                .setHeader(SimpMessageHeaderAccessor.SESSION_ID_HEADER, userA.sessionId())
                .build();

        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, "session-A", null);

        // when: 실제 테스트할 메서드를 호출
        stompLifecycleEventListener.handleSessionDisconnect(event);

        // then:
        // remove 메서드가 올바른 sessionId로 호출되었는지 검증
        verify(onlineParticipantRepository, times(1)).remove("session-A");

        // simpMessagingTemplate.convertAndSend가 호출되었는지,
        //    그리고 어떤 내용으로 호출되었는지 확인하기 위해 ArgumentCaptor를 사용
        ArgumentCaptor<Set<ParticipantInfo>> captor = ArgumentCaptor.forClass(Set.class);
        verify(simpMessagingTemplate, times(1)).convertAndSend(
                eq("/sub/chatroom/" + roomId + "/participants"),
                captor.capture()
        );
    }
}
