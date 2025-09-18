package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.ParticipantDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyGroupQueryRepository {

    /**
     * 스터디 그룹의 id로 스터디 그룹 단건 조회
     * @param studyGroupId
     * @return StudyGroupDetailDto
     */
    Optional<StudyGroupDetailDto> findStudyGroupDetailById(Long studyGroupId);

    /**
     * 스터디 그룹 10개씩 페이징 조회
     * state가 null이면 모든 상태의 스터디 그룹 10개씩 페이징
     * state가 null이 아니면 해당 상태의 스터디 그룹 10개씩 페이징
     * @return StudyGroupSummaryDto List
     */
    Page<StudyGroupSummaryDto> findStudyGroupSummaries(GroupState state, Pageable pageable);

    /**
     * 해당 스터디 그룹에 존재하는 참여자 목록 조회
     * @param studyGroupId
     * @return ParticipantDto Set
     */
    Set<ParticipantDto> findParticipantsByStudyGroupId(Long studyGroupId);
}
