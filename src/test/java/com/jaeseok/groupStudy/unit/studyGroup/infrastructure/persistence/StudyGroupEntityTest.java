package com.jaeseok.groupStudy.unit.studyGroup.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("StudyGroup Entity 테스트")
public class StudyGroupEntityTest {

    @Autowired
    EntityManager em;

    final Long HOST_ID = 1L;
    final Long USER_ID = 2L;

    @Test
    @DisplayName("StudyGroupEntity 저장 및 조회 테스트")
    void givenStudyGroupEntity_whenSaveAndFind_thenReturnEqual() {
        // given
        StudyGroupInfo info = StudyGroupInfo.defaultInfo("테스트 방 제목 001", 5,
                LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(HOST_ID, info);
        studyGroup.apply(USER_ID);

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

        assertThat(found.getParticipantEntitySet()).hasSize(2);
    }

    @Test
    @DisplayName("StudyGroup Domain -> StudyGroup Entity 매핑 테스트")
    void givenStudyGroupDomain_whenFromDomain_thenReturnEntity() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("테스트 제목", 5, LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(HOST_ID, studyGroupInfo);
        studyGroup.apply(USER_ID);

        // when
        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);

        // then
        assertThat(studyGroupEntity.getId()).isNull();
        assertThat(studyGroupEntity.getInfoEntity().getTitle()).isEqualTo(studyGroup.getInfoTitle());

        assertThat(studyGroupEntity.getParticipantEntitySet()).hasSize(2);

        studyGroupEntity.getParticipantEntitySet()
                .forEach(participantEntity -> {
                    assertThat(participantEntity.getStudyGroupEntity()).isSameAs(studyGroupEntity);
                });
    }

    @Test
    @DisplayName("StudyGroup Entity -> StudyGroup Domain 매핑 테스트")
    void givenStudyGroupEntity_whenToDomain_thenReturnDomain() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("테스트 제목", 5,
                LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(HOST_ID, studyGroupInfo);
        studyGroup.apply(USER_ID);

        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);

        em.persist(studyGroupEntity);
        em.flush();
        em.clear();

        Long studyGroupEntityId = studyGroupEntity.getId();
        StudyGroupEntity foundStudyGroupEntity = em.find(StudyGroupEntity.class,
                studyGroupEntity.getId());

        // when
        StudyGroup domain = foundStudyGroupEntity.toDomain();

        // then
        assertThat(domain.getId()).isEqualTo(studyGroupEntityId);
        assertThat(domain.getStudyGroupInfo()).isEqualTo(studyGroupInfo);

        assertThat(domain.getParticipantSet())
                .hasSize(2)
                .extracting(Participant::userId)
                .containsExactlyInAnyOrder(HOST_ID, USER_ID);
    }
}
