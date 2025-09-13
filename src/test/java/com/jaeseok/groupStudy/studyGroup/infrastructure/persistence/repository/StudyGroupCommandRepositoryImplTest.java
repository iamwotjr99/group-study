package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.command.StudyGroupCommandRepositoryImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({StudyGroupCommandRepositoryImpl.class})
@DisplayName("StudyGroupRepository 구현체 테스트")
class StudyGroupCommandRepositoryImplTest {

    @Autowired
    StudyGroupCommandRepository studyGroupCommandRepository;

    final Long HOST_ID = 1L;
    final Long USER_1_ID = 2L;
    final Long USER_2_ID = 3L;

    @Test
    @DisplayName("StudyGroup을 DB에 저장할 수 있다.")
    void givenStudyGroup_whenSave_thenSaveInDB() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("스터디 그룹 테스트 001", 3,
                LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(HOST_ID, studyGroupInfo);

        // when
        StudyGroup savedStudyGroup = studyGroupCommandRepository.save(studyGroup);

        // then
        assertThat(savedStudyGroup).isNotNull();
        assertThat(savedStudyGroup.getId()).isNotNull();
        assertThat(savedStudyGroup.getStudyGroupInfo()).isEqualTo(studyGroupInfo);

        assertThat(savedStudyGroup.getParticipantSet())
                .hasSize(1)
                .extracting(Participant::userId)
                .containsExactlyInAnyOrder(1L);
    }

    @Test
    @DisplayName("StudyGroup에 대한 변경을 DB에 저장할 수 있다.")
    void givenUpdatedStudyGroup_whenUpdate_thenSaveInDB() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("스터디 그룹 테스트 001", 3,
                LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(HOST_ID, studyGroupInfo);

        StudyGroup saved = studyGroupCommandRepository.save(studyGroup);

        saved.apply(USER_1_ID);
        saved.apply(USER_2_ID);

        // when
        StudyGroup updated = studyGroupCommandRepository.update(saved);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getStudyGroupInfo()).isEqualTo(saved.getStudyGroupInfo());

        assertThat(updated.getParticipantSet())
                .hasSize(3)
                .extracting(Participant::userId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    @DisplayName("StudyGroup을 id로 조회할 때 내부에서 Participant를 조합해서 조회할 수 있다.")
    void givenStudyGroupId_whenFindById_thenReturnStudyGroup() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("스터디 그룹 테스트 001", 3,
                LocalDateTime.now().plusDays(1));
        StudyGroup studyGroup = StudyGroup.createWithHost(HOST_ID, studyGroupInfo);

        studyGroup.apply(USER_1_ID);
        studyGroup.apply(USER_2_ID);

        StudyGroup saved = studyGroupCommandRepository.save(studyGroup);

        Long studyGroupId = saved.getId();

        // when
        StudyGroup foundStudyGroup = studyGroupCommandRepository.findById(
                studyGroupId).get(); // 내부에서 participant를 fetch join 하여 불러옴

        // then
        assertThat(foundStudyGroup).isNotNull();
        assertThat(foundStudyGroup.getId()).isEqualTo(studyGroupId);
        assertThat(foundStudyGroup.getStudyGroupInfo()).isEqualTo(studyGroupInfo);

        assertThat(foundStudyGroup.getParticipantSet())
                .hasSize(3)
                .extracting(Participant::userId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }
}