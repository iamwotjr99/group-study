package com.jaeseok.groupStudy.chat.presentation.dto;

import com.jaeseok.groupStudy.chat.application.dto.SendMessageCommand;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessagePayload(
        @Size(max = 500, message = "메시지 내용은 500자 이하여야 합니다.")
        String message,

        @NotNull(message = "메시지 타입은 필수 입력 항목입니다.")
        MessageType type
) {

    public SendMessageCommand toCommand(Long roomId, Long senderId) {
        return new SendMessageCommand(roomId, senderId, this.message, this.type);
    }
}
