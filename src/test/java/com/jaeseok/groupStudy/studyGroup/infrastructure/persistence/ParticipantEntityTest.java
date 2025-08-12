package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ParticipantEntityTest {

    @Autowired
    private EntityManager em;

    private final Long USER_ID = 1L;
    private final Long STUDY_GROUP_ID = 1L;


    @Test
    @DisplayName("ParticipantEntity 저장 및 조회 테스트")
    void givenParticipantEntity_whenSaveAndFind_thenEqualToPersistence() {
        // given
        Participant participant = Participant.apply(USER_ID, STUDY_GROUP_ID);

        ParticipantEntity participantEntity = ParticipantEntity.fromDomain(participant);

        // when
        em.persist(participantEntity);
        em.flush();
        em.close();

        ParticipantEntity found = em.find(ParticipantEntity.class,
                participantEntity.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(participantEntity.getId());
        assertThat(found.getStudyGroupId()).isEqualTo(participantEntity.getStudyGroupId());
        assertThat(found.getStatus()).isEqualTo(participantEntity.getStatus());
        assertThat(found.getRole()).isEqualTo(participantEntity.getRole());
    }

    @Test
    @DisplayName("Participant Domain -> Participant Entity 매핑 테스트")
    void givenParticipant_whenFromDomain_thenReturnEntity() {
        // given
        Participant participant = Participant.apply(USER_ID, STUDY_GROUP_ID);

        // when
        ParticipantEntity participantEntity = ParticipantEntity.fromDomain(participant);

        // then
        assertThat(participantEntity.getId()).isNull();
        assertThat(participantEntity.getUserId()).isEqualTo(participant.userId());
        assertThat(participantEntity.getStudyGroupId()).isEqualTo(participant.studyGroupId());
        assertThat(participantEntity.getStatus()).isEqualTo(participant.status());
        assertThat(participantEntity.getRole()).isEqualTo(participant.role());
    }

    @Test
    @DisplayName("Participant Entity -> Participant Domain 매핑 테스트")
    void givenParticipantEntity_whenToDomain_thenReturnDomain() {
        // given
        Participant participant = Participant.apply(USER_ID, STUDY_GROUP_ID);

        ParticipantEntity participantEntity = ParticipantEntity.fromDomain(participant);

        // when
        Participant domain = participantEntity.toDomain();

        // then
        assertThat(domain).isNotNull();
        assertThat(domain.userId()).isEqualTo(participantEntity.getUserId());
        assertThat(domain.studyGroupId()).isEqualTo(participantEntity.getStudyGroupId());
        assertThat(domain.status()).isEqualTo(participantEntity.getStatus());
        assertThat(domain.role()).isEqualTo(participantEntity.getRole());
    }

    @Test
    @DisplayName("userId, studyGroupId가 같으면 동일한 Participant Entity로 판단")
    void givenSameParticipant_whenEqualsAndHashCode_thenReturnTrue() {

    }
}