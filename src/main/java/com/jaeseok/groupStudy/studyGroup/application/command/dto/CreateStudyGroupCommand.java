package com.jaeseok.groupStudy.studyGroup.application.command.dto;

import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;

public record CreateStudyGroupCommand(
        Long hostId,
        StudyGroupInfo info
) {

}
