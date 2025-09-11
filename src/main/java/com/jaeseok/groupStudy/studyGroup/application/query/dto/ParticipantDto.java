package com.jaeseok.groupStudy.studyGroup.application.query.dto;

import lombok.Builder;

@Builder
public record ParticipantDto(
        Long userId,
        String nickname,
        String role
) {

}
