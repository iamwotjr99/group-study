package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.ParticipantEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupInfoEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("StudyGroup Entity 테스트")
public class StudyGroupEntityTest {

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("StudyGroupEntity 저장 및 조회 테스트")
    void givenStudyGroupEntity_whenSaveAndFind_thenReturnEqual() {
        // given
        StudyGroupInfo info = StudyGroupInfo.defaultInfo("테스트 방 제목 001", 5,
                LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(100L, info);

        // when
        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);
        em.persist(studyGroupEntity);
        em.flush();
        em.clear();

        StudyGroupEntity found = em.find(StudyGroupEntity.class,
                studyGroupEntity.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(studyGroupEntity.getId());
        assertThat(found.getInfoEntity()).isEqualTo(studyGroupEntity.getInfoEntity());
    }

    @Test
    @DisplayName("StudyGroup Domain -> StudyGroup Entity 매핑 테스트")
    void givenStudyGroupDomain_whenFromDomain_thenReturnEntity() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("테스트 제목", 5, LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.of(null, studyGroupInfo, new HashSet<>());

        // when
        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);

        // then
        assertThat(studyGroupEntity.getInfoEntity()).isEqualTo(StudyGroupInfoEntity.fromDomain(studyGroupInfo));
    }

    @Test
    @DisplayName("StudyGroup Entity -> StudyGroup Domain 매핑 테스트")
    void givenStudyGroupEntityAndParticipantEntity_whenToDomain_thenReturnDomain() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("테스트 제목", 5, LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(1L, studyGroupInfo);

        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);
        em.persist(studyGroupEntity);


        Long studyGroupEntityId = studyGroupEntity.getId();

        Participant host = studyGroup.getHost();
        Participant updatedHost = host.withStudyGroupId(studyGroupEntityId);
        Participant participant1 = Participant.apply(2L, studyGroupEntityId);
        Participant participant2 = Participant.apply(3L, studyGroupEntityId);

        ParticipantEntity hostEntity = ParticipantEntity.fromDomain(updatedHost);
        ParticipantEntity participantEntity1 = ParticipantEntity.fromDomain(participant1);
        ParticipantEntity participantEntity2 = ParticipantEntity.fromDomain(participant2);
        em.persist(hostEntity);
        em.persist(participantEntity1);
        em.persist(participantEntity2);

        em.flush();
        em.clear();

        // when
        StudyGroupEntity foundStudyGroupEntity = em.find(StudyGroupEntity.class, studyGroupEntityId);
        Set<Participant> participantSet = em.createQuery(
                        "SELECT p FROM ParticipantEntity p WHERE p.studyGroupId = :studyGroupEntityId",
                        ParticipantEntity.class)
                .setParameter("studyGroupEntityId", studyGroupEntityId)
                .getResultStream()
                .map(ParticipantEntity::toDomain)
                .collect(Collectors.toSet());

        StudyGroup studyGroupDomain = foundStudyGroupEntity.toDomain(participantSet);

        // then
        assertThat(studyGroupDomain).isNotNull();
        assertThat(studyGroupDomain.getId()).isEqualTo(studyGroupEntityId);
        assertThat(studyGroupDomain.getStudyGroupInfo()).isEqualTo(studyGroupInfo);

        assertThat(studyGroupDomain.getParticipantSet())
                .hasSize(3)
                .contains(participant1, participant2);
    }
}
