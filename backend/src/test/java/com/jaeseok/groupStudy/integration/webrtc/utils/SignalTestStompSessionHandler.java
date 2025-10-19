package com.jaeseok.groupStudy.integration.webrtc.utils;

import com.jaeseok.groupStudy.webrtc.dto.SignalMessage;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class SignalTestStompSessionHandler extends StompSessionHandlerAdapter {

    private final BlockingQueue<SignalMessage> messages;

    public SignalTestStompSessionHandler(BlockingQueue<SignalMessage> messages) {
        this.messages = messages;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return SignalMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        if (payload instanceof SignalMessage) {
            messages.add((SignalMessage) payload);
        }
    }

    public BlockingQueue<SignalMessage> getMessages() {
        return messages;
    }
}
