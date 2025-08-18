package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository;

import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.ParticipantEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaParticipantRepository extends JpaRepository<ParticipantEntity, Long> {

}
