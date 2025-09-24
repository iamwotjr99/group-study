package com.jaeseok.groupStudy.chat.application.dto;

import java.time.LocalDateTime;

public record SendMessageInfo(
        String nickname,
        String content,
        LocalDateTime timestamp
) {
}
