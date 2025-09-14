package com.jaeseok.groupStudy.studyGroup.application.query;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyGroupQueryService {

    /**
     * 스터디 그룹의 상세 정보를 조회
     * @param studyGroupId 조회할 스터디 그룹의 ID
     * @return 스터디 그룹 상세 정보 DTO (1건)
     */
    StudyGroupDetailDto getStudyGroupDetail(Long studyGroupId);

    /**
     * 스터디 그룹 목록을 페이징하여 조회
     * @param state 필터링할 스터디 그룹 상태 (null일 경우 전체 조회)
     * @param pageable (페이지 번호, 크기, 정렬)
     * @return 페이징된 스터디 그룹 요약 정보
     */
    Page<StudyGroupSummaryDto> getStudyGroupSummaries(GroupState state, Pageable pageable);

}
