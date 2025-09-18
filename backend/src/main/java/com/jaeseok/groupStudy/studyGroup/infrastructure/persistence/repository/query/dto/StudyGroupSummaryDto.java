package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto;


import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import java.time.LocalDateTime;

public record StudyGroupSummaryDto(
        Long studyGroupId,
        String title,
        Integer curMemberCount,
        Integer capacity,
        LocalDateTime deadline,
        RecruitingPolicy policy,
        GroupState state
) {

}
