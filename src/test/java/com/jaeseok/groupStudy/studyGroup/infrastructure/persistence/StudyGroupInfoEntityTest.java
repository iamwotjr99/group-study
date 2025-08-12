package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StudyGroupInfo Entity 테스트")
class StudyGroupInfoEntityTest {

    @Test
    @DisplayName("fromDomain 하면, Domain의 내용으로 Entity가 생성된다.")
    void givenStudyGroupInfo_whenFromDomain_thenReturnEntity() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("테스트그룹 제목 001", 5,
                LocalDateTime.now().plusDays(1));

        // when
        StudyGroupInfoEntity studyGroupInfoEntity = StudyGroupInfoEntity.fromDomain(studyGroupInfo);

        // then
        assertThat(studyGroupInfoEntity.getTitle()).isEqualTo(studyGroupInfo.getTitle());
        assertThat(studyGroupInfoEntity.getCapacity()).isEqualTo(studyGroupInfo.getCapacity());
        assertThat(studyGroupInfoEntity.getDeadline()).isEqualTo(studyGroupInfo.getDeadline());
        assertThat(studyGroupInfoEntity.getPolicy()).isEqualTo(studyGroupInfo.getPolicy());
        assertThat(studyGroupInfoEntity.getState()).isEqualTo(studyGroupInfo.getState());
    }

    @Test
    @DisplayName("toDomain 하면, Entity의 내용으로 Domain이 생성된다.")
    void givenStudyGroupInfoEntity_whenFromDomain_thenReturnDomainVO() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("테스트그룹 제목 001", 5,
                LocalDateTime.now().plusDays(1));

        StudyGroupInfoEntity studyGroupInfoEntity = StudyGroupInfoEntity.fromDomain(studyGroupInfo);

        // when
        StudyGroupInfo domain = studyGroupInfoEntity.toDomain();

        // then
        assertThat(domain.getTitle()).isEqualTo(studyGroupInfoEntity.getTitle());
        assertThat(domain.getCapacity()).isEqualTo(studyGroupInfoEntity.getCapacity());
        assertThat(domain.getDeadline()).isEqualTo(studyGroupInfoEntity.getDeadline());
        assertThat(domain.getPolicy()).isEqualTo(studyGroupInfoEntity.getPolicy());
        assertThat(domain.getState()).isEqualTo(studyGroupInfoEntity.getState());
    }
}