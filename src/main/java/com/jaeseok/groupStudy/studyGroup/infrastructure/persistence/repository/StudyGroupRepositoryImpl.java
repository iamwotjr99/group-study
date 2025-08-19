package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupRepository;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyGroupRepositoryImpl implements StudyGroupRepository {

    private final JpaStudyGroupRepository jpaStudyGroupRepository;

    @Override
    public StudyGroup save(StudyGroup studyGroup) {
        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);
        StudyGroupEntity savedEntity = jpaStudyGroupRepository.save(studyGroupEntity);

        return savedEntity.toDomain();
    }

    @Override
    public Optional<StudyGroup> findById(Long id) {
        return jpaStudyGroupRepository.findByIdWithParticipants(id)
                .stream()
                .map(StudyGroupEntity::toDomain)
                .findFirst();
    }
}
