package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository;

import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaStudyGroupRepository extends JpaRepository<StudyGroupEntity, Long> {
    @Query("SELECT "
            + "sg"
            + " FROM StudyGroupEntity sg"
            + " JOIN FETCH "
            + "sg.participantEntitySet"
            + " WHERE sg.id = :id")
    Optional<StudyGroupEntity> findByIdWithParticipants(@Param("id") Long studyGroupId);
}
