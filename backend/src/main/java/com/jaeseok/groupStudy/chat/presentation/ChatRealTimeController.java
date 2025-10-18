package com.jaeseok.groupStudy.chat.presentation;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.chat.application.ChatService;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageCommand;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import com.jaeseok.groupStudy.chat.infrastructure.OnlineParticipantRepository;
import com.jaeseok.groupStudy.chat.infrastructure.dto.ParticipantInfo;
import com.jaeseok.groupStudy.chat.presentation.dto.SendMessagePayload;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatRealTimeController {

    private final OnlineParticipantRepository onlineParticipantRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    /**
     * 클라이언트로부터 받은 메세지를 다시 해당 채팅방으로 브로드캐스팅한다.
     * @param userPrincipal JWT 인증 객체
     * @param roomId 클라이언트가 전송한 목적지의 채팅방 ID
     * @param payload 메세지의 내용 payload
     */
    @MessageMapping("/chatroom/{roomId}/message")
    public void sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @DestinationVariable Long roomId,
            @Valid SendMessagePayload payload) {
        Long senderId = userPrincipal.userId();

        SendMessageInfo broadcastInfo = null;

        if (payload.type() == MessageType.ENTER) {
            broadcastInfo = handleEnter(roomId, senderId);
        } else if (payload.type() == MessageType.LEAVE) {
            broadcastInfo = handleLeave(roomId, senderId);
        } else {
            broadcastInfo = handleChat(roomId, senderId, payload);
        }

        messagingTemplate.convertAndSend("/sub/chatroom/" + roomId, broadcastInfo);
    }

    /**
     * 클라이언트가 참여자 목록을 요청할 때 호출되는 메서드
     * @param roomId 스터디 룸 ID
     */
    @MessageMapping("/chatroom/{roomId}/request-participants")
    public void requestParticipants(@DestinationVariable Long roomId) {
        // 현재 참여자 목록을 조회하여 해당 방 전체에 방송
        Set<ParticipantInfo> currentParticipants = onlineParticipantRepository.getParticipants(roomId);
        messagingTemplate.convertAndSend(
                "/sub/chatroom/" + roomId + "/participants",
                currentParticipants
        );
    }

    private SendMessageInfo handleChat(Long roomId, Long senderId, SendMessagePayload payload) {
        SendMessageCommand command = payload.toCommand(roomId, senderId);
        return chatService.sendMessage(command);
    }

    private SendMessageInfo handleEnter(Long roomId, Long senderId) {
        return chatService.enterChatRoom(roomId, senderId);
    }

    private SendMessageInfo handleLeave(Long roomId, Long senderId) {
        return chatService.leaveChatRoom(roomId, senderId);
    }
}
