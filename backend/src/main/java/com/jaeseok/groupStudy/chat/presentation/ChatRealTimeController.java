package com.jaeseok.groupStudy.chat.presentation;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.chat.application.ChatService;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageCommand;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import com.jaeseok.groupStudy.chat.presentation.dto.SendMessagePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatRealTimeController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    /**
     * 클라이언트로부터 받은 메세지를 다시 해당 채팅방으로 브로드캐스팅한다.
     * @param userPrincipal JWT 인증 객체
     * @param roomId 클라이언트가 전송한 목적지의 채팅방 ID
     * @param payload 메세지의 내용
     */
    @MessageMapping("/chatroom/{roomId}/message")
    public void sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @DestinationVariable Long roomId,
            SendMessagePayload payload) {
        Long senderId = userPrincipal.userId();

        SendMessageCommand command = payload.toCommand(roomId, senderId);
        SendMessageInfo broadcastInfo = chatService.sendMessage(command);

        messagingTemplate.convertAndSend("/sub/chatroom/" + roomId, broadcastInfo);
    }
}
