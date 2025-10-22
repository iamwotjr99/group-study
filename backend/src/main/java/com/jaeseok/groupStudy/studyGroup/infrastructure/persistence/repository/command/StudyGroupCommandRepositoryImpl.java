package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.command;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyGroupCommandRepositoryImpl implements StudyGroupCommandRepository {


    private final JpaStudyGroupCommandRepository jpaStudyGroupCommandRepository;

    @Override
    public StudyGroup save(StudyGroup studyGroup) {
        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);
        StudyGroupEntity savedEntity = jpaStudyGroupCommandRepository.save(studyGroupEntity);

        return savedEntity.toDomain();
    }

    @Override
    public Optional<StudyGroup> findById(Long id) {
        return jpaStudyGroupCommandRepository.findByIdWithParticipants(id)
                .stream()
                .map(StudyGroupEntity::toDomain)
                .findFirst();
    }

    @Override
    public StudyGroup update(StudyGroup studyGroup) {
        StudyGroupEntity studyGroupEntity = jpaStudyGroupCommandRepository.findById(studyGroup.getId())
                .orElseThrow(() -> new IllegalArgumentException("업데이트할 스터디 그룹을 찾을 수 없습니다."));

        studyGroupEntity.updateFromDomain(studyGroup);

        return studyGroupEntity.toDomain();
    }

    @Override
    public boolean existsById(Long id) {
        return jpaStudyGroupCommandRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaStudyGroupCommandRepository.deleteById(id);
    }
}
