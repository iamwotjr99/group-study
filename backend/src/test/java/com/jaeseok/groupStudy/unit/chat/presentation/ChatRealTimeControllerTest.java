package com.jaeseok.groupStudy.unit.chat.presentation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.chat.application.ChatService;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageCommand;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import com.jaeseok.groupStudy.chat.exception.ChatRoomNotFoundException;
import com.jaeseok.groupStudy.chat.presentation.ChatRealTimeController;
import com.jaeseok.groupStudy.chat.presentation.dto.SendMessagePayload;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupMemberAccessException;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupNotFoundException;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ChatRealTimeControllerTest {

    @Mock
    SimpMessageSendingOperations messagingTemplate;

    @Mock
    ChatService chatService;

    @InjectMocks
    ChatRealTimeController chatRealTimeController;

    @Mock
    UserPrincipal userPrincipal;

    Long SENDER_ID = 1L;
    Long ROOM_ID = 5L;

    @BeforeEach
    void setUp() {
        setUserPrincipal(SENDER_ID);
    }

    @Test
    @DisplayName("일반 메세지가 수신되면 메세지를 브로드캐스팅 한다.")
    void givenValidChatMessage_whenSendMessage_thenBroadcastChatMessage() {
        // given
        Long senderId = SENDER_ID;
        Long roomId = ROOM_ID;

        SendMessagePayload payload = new SendMessagePayload("안녕하세요 여러분", MessageType.CHAT);
        SendMessageCommand command = payload.toCommand(roomId, senderId);
        SendMessageInfo willBroadcastInfo = new SendMessageInfo(senderId, "nickname1", "테스트 메세지",
                LocalDateTime.now());

        given(chatService.sendMessage(command)).willReturn(willBroadcastInfo);

        // when
        chatRealTimeController.sendMessage(userPrincipal, roomId, payload);

        // then
        verify(chatService).sendMessage(eq(command));
        verify(messagingTemplate).convertAndSend(eq("/sub/chatroom/" + roomId), eq(willBroadcastInfo));
    }

    @Test
    @DisplayName("입장 메세지가 수신되면 입장 메세지를 브로드캐스팅한다.")
    void givenValidEnterMessage_whenSendMessage_thenBroadcastEnterMessage() {
        // given
        Long senderId = SENDER_ID;
        Long roomId = ROOM_ID;

        SendMessagePayload payload = new SendMessagePayload(null, MessageType.ENTER);
        SendMessageInfo willBroadcastInfo = new SendMessageInfo(senderId, "nickname1",
                "nickname1 님이 입장하셨습니다.", LocalDateTime.now());

        given(chatService.enterChatRoom(roomId,senderId)).willReturn(willBroadcastInfo);

        // when
        chatRealTimeController.sendMessage(userPrincipal, roomId, payload);

        // then
        verify(chatService).enterChatRoom(eq(roomId), eq(senderId));
        verify(messagingTemplate).convertAndSend(eq("/sub/chatroom/" + roomId), eq(willBroadcastInfo));
    }

    @Test
    @DisplayName("퇴장 메세지가 수신되면 퇴장 메세지를 브로드캐스팅한다.")
    void givenValidLeaveMessage_whenSendMessage_thenBroadcastLeaveMessage() {
        // given
        Long senderId = SENDER_ID;
        Long roomId = ROOM_ID;

        SendMessagePayload payload = new SendMessagePayload(null, MessageType.LEAVE);
        SendMessageInfo willBroadcastInfo = new SendMessageInfo(senderId, "nickname1", "nickname1 님이 퇴장하셨습니다.",
                LocalDateTime.now());

        given(chatService.leaveChatRoom(roomId, senderId)).willReturn(willBroadcastInfo);

        // when
        chatRealTimeController.sendMessage(userPrincipal, roomId, payload);

        // then
        verify(chatService).leaveChatRoom(eq(roomId), eq(senderId));
        verify(messagingTemplate).convertAndSend(eq("/sub/chatroom/" + roomId), eq(willBroadcastInfo));
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 ID로 메세지 전송 시, 예외를 발생시킨다.")
    void givenNotExistRoomId_whenSendMessage_thenThrowException() {
        // given
        Long notExistRoomId = 404L;
        Long senderId = SENDER_ID;

        SendMessagePayload payload = new SendMessagePayload("테스트 메세지", MessageType.CHAT);
        SendMessageCommand cmd = payload.toCommand(notExistRoomId, senderId);

        given(chatService.sendMessage(cmd)).willThrow(new ChatRoomNotFoundException("존재하지 않는 채팅방 입니다."));

        // when & then
        assertThatThrownBy(() -> chatRealTimeController.sendMessage(userPrincipal, notExistRoomId, payload))
                .isInstanceOf(ChatRoomNotFoundException.class)
                .hasMessageContaining("존재하지 않는");

        verify(chatService).sendMessage(eq(cmd));
        verify(messagingTemplate, never()).convertAndSend(any(), (Object) any());
    }

    @Test
    @DisplayName("스터디 그룹 멤버가 아닌 유저가 메세지 전송 시, 예외를 발생시킨다.")
    void givenNotStudyGroupMember_whenSendMessage_thenThrowException() {
        // given
        Long roomId = ROOM_ID;
        Long notStudyGroupMemberId = 400L;

        UserPrincipal noMemberPrincipal = new UserPrincipal(notStudyGroupMemberId, "no@test.com",
                "password1234");

        SendMessagePayload payload = new SendMessagePayload("테스트 메세지", MessageType.CHAT);
        SendMessageCommand cmd = payload.toCommand(roomId, notStudyGroupMemberId);

        given(chatService.sendMessage(cmd)).willThrow(new StudyGroupMemberAccessException("해당 유저는 승인된 참여자가 아닙니다."));

        // when & then
        assertThatThrownBy(() -> chatRealTimeController.sendMessage(noMemberPrincipal, roomId, payload))
                .isInstanceOf(StudyGroupMemberAccessException.class)
                .hasMessageContaining("해당 유저는 승인된 참여자가 아닙니다.");
    }

    private void setUserPrincipal(Long memberId) {
        userPrincipal = new UserPrincipal(memberId, "test@test.com", "password1234");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())
        );
    }
}