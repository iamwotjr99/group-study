package com.jaeseok.groupStudy.studyGroup.application.query;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.StudyGroupQueryRepository;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto.StudyGroupSummaryDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyGroupQueryServiceImpl implements StudyGroupQueryService {

    private final StudyGroupQueryRepository studyGroupQueryRepository;

    @Override
    public StudyGroupDetailDto getStudyGroupDetail(Long studyGroupId) {
        return studyGroupQueryRepository.findStudyGroupDetailById(studyGroupId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디 그룹입니다."));
    }

    @Override
    public Page<StudyGroupSummaryDto> getStudyGroupSummaries(GroupState state, Pageable pageable) {
        return studyGroupQueryRepository.findStudyGroupSummaries(state, pageable);
    }
}
