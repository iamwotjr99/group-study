package com.jaeseok.groupStudy.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeseok.groupStudy.auth.application.dto.SignUpInfo;
import lombok.Builder;

@Builder
public record SignUpResponse(
        @JsonProperty("userId")
        Long id,

        @JsonProperty("message")
        String message)
{
        public static SignUpResponse from(SignUpInfo data) {
                return new SignUpResponse(data.userId(), "회원가입에 성공했습니다.");
        }
}
