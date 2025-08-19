package com.jaeseok.groupStudy.studyGroup.domain;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StudyGroupInfo 테스트")
class StudyGroupInfoTest {

    StudyGroupInfo info;

    @BeforeEach
    void setUp() {
        info = StudyGroupInfo.of("테스트 방 제목",
                5,
                LocalDateTime.now().plusDays(10),
                RecruitingPolicy.APPROVAL,
                GroupState.RECRUITING
        );
    }

    @Test
    @DisplayName("defaultInfo()로 기본값 스터디 정보를 생성할 수 있다.")
    void whenDefaultInfo_thenReturnDefaultStudyGroupInfo() {
        // 기본값 상태 -> 가입 방법: 승인제, 스터디 모집 상태: 모집중
        // when
        LocalDateTime deadLine = LocalDateTime.now().plusDays(1);
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("테스트 그룹 방제목", 2,
                deadLine);

        // then
        assertThat(studyGroupInfo.getTitle()).isEqualTo("테스트 그룹 방제목");
        assertThat(studyGroupInfo.getCapacity()).isEqualTo(2);
        assertThat(studyGroupInfo.getDeadline()).isEqualTo(deadLine);
    }

    @Test
    @DisplayName("스터디 그룹을 만들 때 제목이 없으면 예외를 던진다.")
    void givenGroupTitleIsNull_whenCreateStudyGroup_thenThrowException() {
        // given
        // when
        // then
        assertThatThrownBy(() -> StudyGroupInfo.defaultInfo(null, 3, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다.");

        assertThatThrownBy(() -> StudyGroupInfo.defaultInfo("", 3, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다.");
    }

    @Test
    @DisplayName("스터디 그룹을 만들 때 제목이 20자보다 크면 예외를 던진다.")
    void givenGroupTitleOver20_whenCreateStudyGroup_thenThrowException() {
        // given
        // when
        // then
        assertThatThrownBy(() -> StudyGroupInfo.defaultInfo("aaaabbbbccccddddeeeef", 3, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 20자 이하로 설정해주세요.");
    }

    @Test
    @DisplayName("스터디 그룹을 만들 때 그룹 인원수 2보다 작다면 예외를 던진다.")
    void givenGroupCapacityUnder2_whenCreateStudyGroup_thenThrowException() {
        // given
        // when
        // then
        assertThatThrownBy(() -> StudyGroupInfo.defaultInfo("SpringStudy", null, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("방 인원은 2명 이상입니다.");

        assertThatThrownBy(() -> StudyGroupInfo.defaultInfo("SpringStudy", 0, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("방 인원은 2명 이상입니다.");

        assertThatThrownBy(() -> StudyGroupInfo.defaultInfo("SpringStudy", 1, LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("방 인원은 2명 이상입니다.");
    }

    @Test
    @DisplayName("autoPolicy()로 모집 정책을 AUTO(자동 승인제)로 바꿀 수 있다.")
    void givenStudyGroupInfo_whenAutoPolicy_thenReturnAutoPolicy() {
        // given
        // when
        StudyGroupInfo autoPolicy = info.autoPolicy();

        // then
        assertThat(autoPolicy.getPolicy()).isEqualTo(
                RecruitingPolicy.AUTO);
    }

    @Test
    @DisplayName("approvePolicy()로 모집 정책을 APPROVAL(승인제)로 바꿀 수 있다.")
    void givenStudyGroupInfo_whenApprovePolicy_thenReturnApprovePolicy() {
        // given
        // when
        StudyGroupInfo approvePolicy = info.approvePolicy();

        // then
        assertThat(approvePolicy.getPolicy()).isEqualTo(
                RecruitingPolicy.APPROVAL);
    }

    @Test
    @DisplayName("recruit()으로 그룹 상태를 RECRUITING(모집중)으로 바꿀 수 있다.")
    void givenStudyGroupInfo_whenRecruit_thenReturnRecruitingState() {
        // given
        // when
        StudyGroupInfo recruit = info.recruit();

        // then
        assertThat(recruit.getState()).isEqualTo(
                GroupState.RECRUITING);
    }

    @Test
    @DisplayName("close()으로 그룹 상태를 CLOSE(모집 마감)으로 바꿀 수 있다.")
    void givenStudyGroupInfo_whenClose_thenReturnCloseState() {
        // given
        // when
        StudyGroupInfo close = info.close();

        // then
        assertThat(close.getState()).isEqualTo(GroupState.CLOSE);
    }

    @Test
    @DisplayName("start()으로 그룹 상태를 START(진행중)으로 바꿀 수 있다.")
    void givenStudyGroupInfo_whenStart_thenReturnStartState() {
        // given
        // when
        StudyGroupInfo start = info.start();

        // then
        assertThat(start.getState()).isEqualTo(GroupState.START);
    }

    @Test
    @DisplayName("모든 속성이 같으면 동등한 StudyGroupInfo로 판단한다.")
    void givenSameInfo_whenEqualsAndHashCode_thenReturnTrue() {
        // given
        LocalDateTime deadLine = LocalDateTime.now().plusDays(1);
        StudyGroupInfo info1 = StudyGroupInfo.of("테스트 제목", 2, deadLine, RecruitingPolicy.APPROVAL, GroupState.RECRUITING);
        StudyGroupInfo info2 = StudyGroupInfo.of("테스트 제목", 2, deadLine, RecruitingPolicy.APPROVAL, GroupState.RECRUITING);

        // when
        boolean equals = info1.equals(info2);
        boolean hashCode = info1.hashCode() == info2.hashCode();

        // then
        assertThat(equals).isTrue();
        assertThat(hashCode).isTrue();
    }
}

