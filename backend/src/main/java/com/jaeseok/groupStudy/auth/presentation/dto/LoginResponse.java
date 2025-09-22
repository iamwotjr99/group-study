package com.jaeseok.groupStudy.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeseok.groupStudy.auth.application.dto.LoginInfo;

public record LoginResponse(
        @JsonProperty("access_token")
        String token,
        Long memberId,
        String nickname,
        String message
) {
        public static LoginResponse from(LoginInfo info) {
                return new LoginResponse(info.accessToken(), info.memberId(), info.nickname(), "로그인 성공");
        }
}
