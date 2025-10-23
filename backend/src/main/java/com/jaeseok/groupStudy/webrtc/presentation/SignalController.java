package com.jaeseok.groupStudy.webrtc.presentation;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.webrtc.application.SignalService;
import com.jaeseok.groupStudy.webrtc.dto.SignalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SignalController {

    private final SignalService signalService;

    /**
     * 클라이언트로부터 WebRTC 시그널링 메시지를 수신
     * (예: Offer, Answer, ICE Candidate)
     *
     * 경로: /pub/signal/{roomId}
     *
     * @param roomId  메시지가 속한 스터디 방 ID
     * @param message 시그널링 메세지 DTO (type, payload, senderId, receiverId)
     */
    @MessageMapping("/signal/{roomId}")
    public void handleSignalMessage(
            @DestinationVariable Long roomId,
            SignalMessage message
    ) {
        signalService.relaySignal(roomId, message);
    }
}
