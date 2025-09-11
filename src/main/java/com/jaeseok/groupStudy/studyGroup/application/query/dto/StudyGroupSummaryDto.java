package com.jaeseok.groupStudy.studyGroup.application.query.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record StudyGroupSummaryDto(
        Long studyGroupId,
        String title,
        Integer currentMembers,
        Integer maxMembers,
        String groupState,
        LocalDateTime deadline,
        LocalDateTime createdAt
) {

}
