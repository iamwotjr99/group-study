package com.jaeseok.groupStudy.studyGroup.application.command;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupApproveCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupKickCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupRejectCommand;

public interface StudyGroupHostService {

    /**
     * 방장이 스터디그룹 참여 신청자를 승인
     * Participant의 상태가 "대기중"에서 "승인됨"으로 변경
     * @param cmd - StudyGroupApproveCommand
     */
    void approveApplication(StudyGroupApproveCommand cmd);

    /**
     * 방장이 스터디그룹 참여 신청자를 거절
     * Participant의 상태가 "대기중"에서 "거절됨"으로 변경
     * @param cmd - StudyGroupRejectCommand
     */
    void rejectApplication(StudyGroupRejectCommand cmd);

    /**
     * 방장이 스터디그룹 참여자를 강퇴
     * Participant의 상태가 "승인됨"에서 "강퇴됨"으로 변경
     * @param cmd - StudyGroupKickCommand
     */
    void kickParticipation(StudyGroupKickCommand cmd);
}
