package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.command;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaStudyGroupCommandRepository extends JpaRepository<StudyGroupEntity, Long> {
    @Query("SELECT "
            + "sg"
            + " FROM StudyGroupEntity sg"
            + " JOIN FETCH "
            + "sg.participantEntitySet"
            + " WHERE sg.id = :id")
    Optional<StudyGroupEntity> findByIdWithParticipants(@Param("id") Long studyGroupId);

    @Query("SELECT "
            + "sg"
            + " FROM StudyGroupEntity sg"
            + " WHERE sg.infoEntity.state = :state")
    List<StudyGroupEntity> findByState(@Param("state") GroupState state);

    boolean existsById(Long id);
}
