package com.jaeseok.groupStudy.auth.application.dto;

public record LoginInfo(
        String accessToken,
        Long memberId,
        String nickname
) {
}
