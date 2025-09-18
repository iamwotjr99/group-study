package com.jaeseok.groupStudy.member.application.dto;

import lombok.Builder;

@Builder
public record MemberInfoDto(
        Long userId,
        String nickname,
        String email
) {

}
