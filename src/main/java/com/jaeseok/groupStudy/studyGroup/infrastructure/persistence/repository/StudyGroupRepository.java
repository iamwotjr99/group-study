package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyGroupRepository extends JpaRepository<StudyGroupEntity, Long> {
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
    List<StudyGroupEntity> findByStatus(@Param("state") GroupState state);
}
