package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto;

import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;

public record ParticipantDto(
        Long id,
        String email,
        String nickname,
        ParticipantRole role,
        ParticipantStatus status
) {

}
