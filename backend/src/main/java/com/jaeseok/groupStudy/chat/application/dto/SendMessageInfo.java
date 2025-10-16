package com.jaeseok.groupStudy.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record SendMessageInfo(
        @JsonProperty("senderId") Long senderId,
        @JsonProperty("nickname") String nickname,
        @JsonProperty("content") String content,
        @JsonProperty("timestamp") LocalDateTime timestamp
) {

}
