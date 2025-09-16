package com.jaeseok.groupStudy.studyGroup.application.command.dto;

import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import java.time.LocalDateTime;

public record CreateStudyGroupCommand(
        Long hostId,
        String title,
        Integer capacity,
        LocalDateTime deadline,
        RecruitingPolicy policy
) {

}
