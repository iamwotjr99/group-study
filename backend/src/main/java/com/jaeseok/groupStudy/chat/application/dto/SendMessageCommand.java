package com.jaeseok.groupStudy.chat.application.dto;

import com.jaeseok.groupStudy.chat.domain.MessageType;

public record SendMessageCommand(
        Long roomId,
        Long senderId,
        String message,
        MessageType type
) {

}
