package com.jaeseok.groupStudy.integration.chat.utils;

import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class TestStompSessionHandler extends StompSessionHandlerAdapter {
    private final BlockingQueue<SendMessageInfo> messages;
    private final BlockingQueue<StompHeaders> errors;

    public TestStompSessionHandler(BlockingQueue<SendMessageInfo> messages, BlockingQueue<StompHeaders> errors) {
        this.messages = messages;
        this.errors = errors;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("Received: " + headers);
        // 수신된 성공 메시지 처리
        if (payload instanceof SendMessageInfo) {
            messages.add((SendMessageInfo) payload);
        }

        // 수신된 에러 메시지 처리
        else if (payload instanceof String && headers.getDestination() != null) {
            headers.set("error-message", (String) payload);
            errors.add(headers);
        }
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers,
            byte[] payload, Throwable exception) {
        System.out.println("Error: " + exception.getMessage());

        // **STOMP ERROR 프레임 수신 시, headers를 errors 큐에 추가합니다.**
        // StompCommand.ERROR는 ERROR 프레임을 받을 때의 command입니다.
        if (StompCommand.ERROR.equals(command)) {
            this.errors.add(headers);
        }

        // 연결 끊김(Transport Error)을 여기서 처리해야 할 수도 있습니다.
        // 하지만 현재 테스트의 초점은 ERROR 프레임 수신이므로 위 코드만 추가해봅니다.
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {

        if (headers.getDestination() != null && headers.getDestination().contains("/queue/errors")) {
            return String.class;
        }
        return SendMessageInfo.class;
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        System.out.println("Transport Error: " + exception.getMessage() + " " + exception.getClass());
        super.handleTransportError(session, exception);
    }
}
