package com.jaeseok.groupStudy.member.presentation.dto;

import com.jaeseok.groupStudy.member.application.dto.MemberInfoDto;

public record MemberInfoResponse(
        Long userId,
        String nickname,
        String email
) {
    public static MemberInfoResponse of(MemberInfoDto dto) {
        return new MemberInfoResponse(dto.userId(), dto.nickname(), dto.email());
    }
}
