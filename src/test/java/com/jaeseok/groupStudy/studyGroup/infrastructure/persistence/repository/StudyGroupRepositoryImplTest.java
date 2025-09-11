package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.ParticipantEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupInfoEntity;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("StudyGroupRepository 테스트")
class StudyGroupRepositoryImplTest {

    @Autowired
    StudyGroupRepository studyGroupRepository;

    final Long HOST_ID = 1L;
    final Long USER_1_ID = 2L;
    final Long USER_2_ID = 3L;

    @Test
    @DisplayName("StudyGroup을 DB에 저장할 수 있다.")
    void givenStudyGroup_whenSave_thenSaveInDB() {
        // given
        StudyGroupEntity studyGroupEntity = createDefaultStudyGroupEntityWithHost(
                HOST_ID, "스터디 그룹 테스트 001");

        // when
        StudyGroupEntity savedEntity = studyGroupRepository.save(studyGroupEntity);

        // then
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getInfoEntity()).isEqualTo(studyGroupEntity.getInfoEntity());

        assertThat(savedEntity.getParticipantEntitySet())
                .hasSize(1)
                .extracting(ParticipantEntity::getUserId)
                .containsExactlyInAnyOrder(HOST_ID);
    }

    @Test
    @DisplayName("StudyGroup을 id로 조회할 때 내부에서 Participant를 조합해서 조회할 수 있다. fetch join")
    void givenStudyGroupId_whenFindByIdWithParticipants_thenReturnStudyGroup() {
        // given
        StudyGroupEntity studyGroupEntity = createDefaultStudyGroupEntityWithHost(HOST_ID,
                "스터디 그룹 테스트 001");

        ParticipantEntity user1Entity = ParticipantEntity.builder()
                .userId(USER_1_ID)
                .studyGroupEntity(studyGroupEntity)
                .role(ParticipantRole.HOST)
                .status(ParticipantStatus.APPROVED)
                .build();

        ParticipantEntity user2Entity = ParticipantEntity.builder()
                .userId(USER_2_ID)
                .studyGroupEntity(studyGroupEntity)
                .role(ParticipantRole.HOST)
                .status(ParticipantStatus.APPROVED)
                .build();

        studyGroupEntity.getParticipantEntitySet().add(user1Entity);
        studyGroupEntity.getParticipantEntitySet().add(user2Entity);

        StudyGroupEntity saved = studyGroupRepository.save(studyGroupEntity);

        Long studyGroupId = saved.getId();

        // when
        StudyGroupEntity foundStudyGroup = studyGroupRepository.findByIdWithParticipants(
                studyGroupId).get(); // 내부에서 participant를 fetch join 하여 불러옴

        // then
        assertThat(foundStudyGroup).isNotNull();
        assertThat(foundStudyGroup.getId()).isEqualTo(studyGroupId);
        assertThat(foundStudyGroup.getInfoEntity()).isEqualTo(saved.getInfoEntity());

        assertThat(foundStudyGroup.getParticipantEntitySet())
                .hasSize(3)
                .extracting(ParticipantEntity::getUserId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    @DisplayName("GroupState.Recruiting인 StudyGroup을 조회할 수 있다.")
    void givenRecruitingState_whenFindByState_thenReturnRecruitingStudyGroup() {
        // given
        StudyGroupEntity studyGroupEntity1 = createDefaultStudyGroupEntityWithHost(HOST_ID, "스터디 그룹 테스트 001");
        StudyGroupEntity studyGroupEntity2 = createDefaultStudyGroupEntityWithHost(USER_1_ID, "스터디 그룹 테스트 002");

        studyGroupRepository.save(studyGroupEntity1);
        studyGroupRepository.save(studyGroupEntity2);

        GroupState state = GroupState.RECRUITING;

        // when
        List<StudyGroupEntity> recruitingGroup = studyGroupRepository.findByStatus(state);

        // then
        assertThat(recruitingGroup)
                .isNotNull()
                .hasSize(2)
                .extracting(StudyGroupEntity::getInfoEntity)
                .containsExactlyInAnyOrder(studyGroupEntity1.getInfoEntity(), studyGroupEntity2.getInfoEntity());
    }

    private StudyGroupEntity createDefaultStudyGroupEntityWithHost(Long hostId, String title) {
        StudyGroupInfoEntity infoEntity = StudyGroupInfoEntity.builder()
                .title(title)
                .capacity(3)
                .policy(RecruitingPolicy.APPROVAL)
                .state(GroupState.RECRUITING)
                .build();

        StudyGroupEntity studyGroupEntity = StudyGroupEntity.builder()
                .infoEntity(infoEntity)
                .participantEntitySet(new HashSet<>())
                .build();

        ParticipantEntity hostEntity = ParticipantEntity.builder()
                .userId(hostId)
                .studyGroupEntity(studyGroupEntity)
                .role(ParticipantRole.HOST)
                .status(ParticipantStatus.APPROVED)
                .build();

        studyGroupEntity.getParticipantEntitySet().add(hostEntity);

        return studyGroupEntity;
    }
}