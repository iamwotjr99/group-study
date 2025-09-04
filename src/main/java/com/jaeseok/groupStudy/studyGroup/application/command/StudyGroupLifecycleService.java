package com.jaeseok.groupStudy.studyGroup.application.command;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupCloseCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupCreateCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupStartCommand;

public interface StudyGroupLifecycleService {

    /**
     * 스터디 그룹을 생성
     * @param cmd - StudyGroupCreateCommand(hostId, info)
     * @return 생성된 스터디 그룹 ID
     */
    Long createStudyGroup(StudyGroupCreateCommand cmd);

    /**
     * 스터디 그룹의 상태를 "진행중"으로 변경
     * @param cmd - StudyGroupStartCommand(studyGroupId, hostId)
     */
    void startStudyGroup(StudyGroupStartCommand cmd);

    /**
     * 스터디 그룹의 상태를 "종료"로 변경
     * @param cmd - StudyGroupCloseCommand(StudyGroupId, hostId)
     */
    void closeStudyGroup(StudyGroupCloseCommand cmd);
}
