package com.jaeseok.groupStudy.studyGroup.application.query.dto;

import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

@Builder
public record StudyGroupDetailDto(
        Long studyGroupId,
        String title,
        Integer capacity,
        LocalDateTime deadline,
        String groupPolicy,
        String groupState,
        LocalDateTime createdAt,
        Set<Participant> participants
) {

}
