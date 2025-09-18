package com.jaeseok.groupStudy.studyGroup.application.command.dto;

public record RejectStudyGroupCommand(Long studyGroupId, Long hostId, Long applicantId) {

}
