package com.jaeseok.groupStudy.chat.application.dto;

import java.time.LocalDateTime;

public record GetMessageInfo(
        String nickname,
        String content,
        LocalDateTime timestamp
) {
}
