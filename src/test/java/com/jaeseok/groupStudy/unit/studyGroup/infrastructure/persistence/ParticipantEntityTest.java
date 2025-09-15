package com.jaeseok.groupStudy.unit.studyGroup.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.ParticipantEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("Participant Entity 테스트")
class ParticipantEntityTest {

    @Autowired
    EntityManager em;

    final Long HOST_ID = 1L;
    final Long USER_ID = 2L;

    StudyGroupEntity studyGroupEntity;

    @BeforeEach
    void setUp() {
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("테스트 제목", 5, LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(HOST_ID, studyGroupInfo);

        studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);

        em.persist(studyGroupEntity);
        em.flush();
        em.clear();

        studyGroupEntity = em.find(StudyGroupEntity.class, studyGroupEntity.getId());
    }

    @Test
    @DisplayName("ParticipantEntity 저장 및 조회 테스트")
    void givenParticipantEntity_whenSaveAndFind_thenEqualToPersistence() {
        // given
        Participant participant = Participant.apply(USER_ID, studyGroupEntity.getId());

        ParticipantEntity participantEntity = ParticipantEntity.fromDomain(participant, studyGroupEntity);

        // when
        em.persist(participantEntity);
        em.flush();
        em.clear();

        ParticipantEntity found = em.find(ParticipantEntity.class,
                participantEntity.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(participantEntity.getId());
        assertThat(found.getStudyGroupEntity().getId()).isEqualTo(participantEntity.getStudyGroupEntity().getId());
        assertThat(found.getStatus()).isEqualTo(participantEntity.getStatus());
        assertThat(found.getRole()).isEqualTo(participantEntity.getRole());
    }

    @Test
    @DisplayName("Participant Domain -> Participant Entity 매핑 테스트")
    void givenParticipant_whenFromDomain_thenReturnEntity() {
        // given
        Participant participant = Participant.apply(USER_ID, studyGroupEntity.getId());

        // when
        ParticipantEntity participantEntity = ParticipantEntity.fromDomain(participant, studyGroupEntity);

        // then
        assertThat(participantEntity.getId()).isNull();
        assertThat(participantEntity.getUserId()).isEqualTo(participant.userId());
        assertThat(participantEntity.getStudyGroupEntity().getId()).isEqualTo(participant.studyGroupId());
        assertThat(participantEntity.getStatus()).isEqualTo(participant.status());
        assertThat(participantEntity.getRole()).isEqualTo(participant.role());
    }

    @Test
    @DisplayName("Participant Entity -> Participant Domain 매핑 테스트")
    void givenParticipantEntity_whenToDomain_thenReturnDomain() {
        // given
        Participant participant = Participant.apply(USER_ID, studyGroupEntity.getId());

        ParticipantEntity participantEntity = ParticipantEntity.fromDomain(participant, studyGroupEntity);

        em.persist(participantEntity);
        em.flush();
        em.clear();

        ParticipantEntity foundParticipantEntity = em.find(ParticipantEntity.class,
                participantEntity.getId());

        // when
        Participant domain = foundParticipantEntity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.userId()).isEqualTo(participantEntity.getUserId());
        assertThat(domain.studyGroupId()).isEqualTo(participantEntity.getStudyGroupEntity().getId());
        assertThat(domain.status()).isEqualTo(participantEntity.getStatus());
        assertThat(domain.role()).isEqualTo(participantEntity.getRole());
    }

    @Test
    @DisplayName("userId, studyGroupId가 같으면 동일한 Participant Entity로 판단")
    void givenSameParticipant_whenEqualsAndHashCode_thenReturnTrue() {
        // given
        Participant participant1 = Participant.of(USER_ID, studyGroupEntity.getId(),
                ParticipantStatus.APPROVED, ParticipantRole.MEMBER);
        Participant participant2 = Participant.of(USER_ID, studyGroupEntity.getId(),
                ParticipantStatus.APPROVED, ParticipantRole.MEMBER);

        ParticipantEntity participantEntity1 = ParticipantEntity.fromDomain(participant1, studyGroupEntity);
        ParticipantEntity participantEntity2 = ParticipantEntity.fromDomain(participant2, studyGroupEntity);

        // when
        boolean equals = participantEntity1.equals(participantEntity2);
        boolean hashCode = participantEntity1.hashCode() == participantEntity2.hashCode();

        // then
        assertThat(equals).isTrue();
        assertThat(hashCode).isTrue();

    }
}