package com.jaeseok.groupStudy.studyGroup.application.query;

import com.jaeseok.groupStudy.studyGroup.application.query.dto.ParticipantDto;
import com.jaeseok.groupStudy.studyGroup.application.query.dto.StudyGroupDetailDto;
import com.jaeseok.groupStudy.studyGroup.application.query.dto.StudyGroupSummaryDto;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.StudyGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyGroupQueryServiceImpl implements StudyGroupQueryService {

    private final StudyGroupRepository studyGroupRepository;

    @Override
    public StudyGroupDetailDto findStudyGroupById(Long studyGroupId) {

        return null;
    }

    @Override
    public List<StudyGroupSummaryDto> findAllStudyGroup() {
        return List.of();
    }

    @Override
    public List<ParticipantDto> findApprovedParticipantInStudyGroup(Long studyGroupId) {
        return List.of();
    }

    @Override
    public List<StudyGroupSummaryDto> findApprovedStudyGroup(Long userId) {
        return List.of();
    }
}
