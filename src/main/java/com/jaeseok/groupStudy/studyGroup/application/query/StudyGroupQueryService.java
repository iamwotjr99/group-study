package com.jaeseok.groupStudy.studyGroup.application.query;

import com.jaeseok.groupStudy.studyGroup.application.query.dto.ParticipantDto;
import com.jaeseok.groupStudy.studyGroup.application.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.application.query.dto.StudyGroupSummaryDto;
import java.util.List;

public interface StudyGroupQueryService {

    // 스터디 그룹 단건 조회
    StudyGroupDetailDto findStudyGroupById(Long studyGroupId);

    // 모든 스터디 그룹 조회 (추후 페이징 예정)
    List<StudyGroupSummaryDto> findAllStudyGroup();

    // 스터디 그룹의 승인된 참여자 조회
    List<ParticipantDto> findApprovedParticipantInStudyGroup(Long studyGroupId);

    // 내가 참여한 스터디 그룹 조회
    List<StudyGroupSummaryDto> findApprovedStudyGroup(Long userId);
}
