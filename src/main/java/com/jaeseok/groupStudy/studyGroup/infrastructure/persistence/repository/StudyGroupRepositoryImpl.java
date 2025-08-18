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
    public void save(StudyGroup studyGroup) {
        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);
        jpaStudyGroupRepository.save(studyGroupEntity);
    }

    @Override
    public Optional<StudyGroup> findById(Long id) {
        return jpaStudyGroupRepository.findById(id).stream()
                .map(StudyGroupEntity::toDomain)
                .findFirst();
    }
}
