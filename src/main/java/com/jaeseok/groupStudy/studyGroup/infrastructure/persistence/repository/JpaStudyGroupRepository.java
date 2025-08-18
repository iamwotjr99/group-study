package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository;

import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStudyGroupRepository extends JpaRepository<StudyGroupEntity, Long> {

}
