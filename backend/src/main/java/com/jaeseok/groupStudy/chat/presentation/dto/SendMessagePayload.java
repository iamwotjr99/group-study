package com.jaeseok.groupStudy.chat.presentation.dto;

import com.jaeseok.groupStudy.chat.application.dto.SendMessageCommand;
import com.jaeseok.groupStudy.chat.domain.MessageType;

public record SendMessagePayload(
        String message,
        MessageType type
) {

    public SendMessageCommand toCommand(Long roomId, Long senderId) {
        return new SendMessageCommand(roomId, senderId, this.message, this.type);
    }
}
