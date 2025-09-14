package com.jaeseok.groupStudy.studyGroup.presentation.command.dto;

import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;

public record CreateStudyGroupRequest(
        Long hostId,
        String title,
        Integer capacity,
        LocalDateTime deadline,
        RecruitingPolicy policy
) {
    public StudyGroupInfo toStudyGroupInfo() {
        return StudyGroupInfo.of(title, capacity, deadline, policy);
    }
}
