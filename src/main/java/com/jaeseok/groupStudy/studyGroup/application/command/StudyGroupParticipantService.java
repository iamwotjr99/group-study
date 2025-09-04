package com.jaeseok.groupStudy.studyGroup.application.command;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApplyStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CancelStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.LeaveStudyGroupCommand;

public interface StudyGroupParticipantService {

    /**
     * 유저가 스터디 그룹에 참여 신청을 한다.
     * 유저의 ID로 된 Participant가 "대기중" 상태로 스터디 그룹 SET에 등록된다.
     * @param cmd - ApplyStudyGroupCommand(studyGroupId, applicantId)
     */
    void applyForStudyGroup(ApplyStudyGroupCommand cmd);

    /**
     * 참여자가 신청한 스터디 그룹에 신청을 철회한다.
     * 참여자의 상태가 "대기중"에서 "취소됨"으로 변경된다.
     * @param cmd - CancelStudyGroupCommand(studyGroupId, applicantId)
     */
    void cancelApplication(CancelStudyGroupCommand cmd);

    /**
     * 참여중인 스터디 그룹에서 퇴장한다.
     * 참여자의 상태가 "승인됨"에서 "떠남"상태로 변경된다.
     * @param cmd - LeaveStudyGroupCommand(studyGroupId, participantId)
     */
    void leaveStudyGroup(LeaveStudyGroupCommand cmd);
}
