package com.jaeseok.groupStudy.studyGroup.application.command.dto;

public record ApproveStudyGroupCommand(Long studyGroupId, Long hostId, Long applicantId) {

}
