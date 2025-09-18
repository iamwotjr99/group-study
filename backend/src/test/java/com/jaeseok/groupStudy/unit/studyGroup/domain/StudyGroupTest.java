package com.jaeseok.groupStudy.unit.studyGroup.domain;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.exception.NoHostAuthorityException;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("스터디 그룹 도메인 테스트")
class StudyGroupTest {

    final Long HOST_ID = 1L;
    final Long USER1_ID = 2L;
    final Long USER2_ID = 3L;


    StudyGroup studyGroup;

    @BeforeEach
    void setUp() {
        studyGroup = StudyGroup.createWithHost(HOST_ID, "테스트 방 제목 001", 5,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
    }

    @Test
    @DisplayName("유저가 스터디 그룹을 만들 수 있고 해당 유저가 방장이된다.")
    void givenStudyGroupInfoWithHostId_whenCreateStudyGroup_thenReturnStudyGroup() {
        // given
        LocalDateTime deadline = LocalDateTime.now().plusDays(1);
        StudyGroupInfo info = StudyGroupInfo.defaultInfo("Spring Study", 3, deadline);

        // when
        StudyGroup studyGroup = StudyGroup.createWithHost(HOST_ID, "Spring Study", 3, deadline, RecruitingPolicy.APPROVAL);

        // then
        Participant host = studyGroup.getHost();
        Set<Participant> participantSet = studyGroup.getParticipantSet();

        assertThat(host.userId()).isEqualTo(HOST_ID);
        assertThat(host.status()).isEqualTo(ParticipantStatus.APPROVED);
        assertThat(host.role()).isEqualTo(ParticipantRole.HOST);

        assertThat(studyGroup.getStudyGroupInfo()).isEqualTo(info);
        assertThat(participantSet)
                .contains(host)
                .hasSize(1);
    }


    @Test
    @DisplayName("유저는 스터디 그룹에 참여 신청을 하면 PENDING 상태의 참여자가 추가 된다.")
    void givenUserId_whenApply_thenReturnPendingParticipant() {
        // given
        Long applicantUserId = USER1_ID;

        // when
        studyGroup.apply(USER1_ID);

        // then
        Set<Participant> participantSet = studyGroup.getParticipantSet();
        Participant applicant = participantSet.stream()
                .filter(p -> p.userId().equals(applicantUserId))
                .findFirst()
                .get();


        assertThat(applicant).isNotNull();
        assertThat(applicant.userId()).isEqualTo(applicantUserId);
        assertThat(applicant.status()).isEqualTo(ParticipantStatus.PENDING);
        assertThat(applicant.role()).isEqualTo(ParticipantRole.MEMBER);

        assertThat(participantSet)
                .contains(applicant)
                .hasSize(2);
    }

    @Test
    @DisplayName("이미 스터디그룹에 속한 참여자가 신청을하면 예외를 던진다.")
    void givenAlreadyExistsParticipant_whenApply_thenReturnException() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);

        // when
        // then
        assertThatThrownBy(() -> studyGroup.apply(applicantUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 신청중이거나 참여중인 스터디 그룹입니다.");
    }

    @Test
    @DisplayName("방장은 승인 대기 중인 참여자를 승인할 수 있다.")
    void givenParticipantApply_whenApproveParticipant_thenReturnApprovedParticipant() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);

        // when
        studyGroup.approveParticipant(HOST_ID, applicantUserId);

        // then
        Set<Participant> participantSet = studyGroup.getParticipantSet();
        Participant participant = participantSet.stream()
                .filter(p -> p.userId().equals(applicantUserId))
                .findFirst()
                .get();

        assertThat(participant.userId()).isEqualTo(applicantUserId);
        assertThat(participant.status()).isEqualTo(ParticipantStatus.APPROVED);
        assertThat(participant.role()).isEqualTo(ParticipantRole.MEMBER);

        assertThat(participantSet).hasSize(2);
    }

    @Test
    @DisplayName("방장이 승인 대기중이 아닌 참여자를 승인하려고 하면 예외를 던진다.")
    void givenNotPendingParticipant_whenApproveParticipant_thenThrowException() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);
        studyGroup.approveParticipant(HOST_ID, applicantUserId);

        // when
        // then

        assertThatThrownBy(() -> studyGroup.approveParticipant(HOST_ID, applicantUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기중인 유저가 아닙니다.");

    }

    @Test
    @DisplayName("정원이 가득 찬 그룹에 참여자를 승인하려고하면 예외를 던진다.")
    void givenPendingParticipantAndPullGroup_whenApproveParticipant_thenThrowException() {
        // given
        Long applicantUserId = 100L;
        for (int i = 0; i < 4; i++) {
            applicantUserId = 100L + i;

            studyGroup.apply(applicantUserId);
            studyGroup.approveParticipant(HOST_ID, applicantUserId);
        }

        studyGroup.apply(applicantUserId + 1);

        // when
        // then
        Set<Participant> participantSet = studyGroup.getParticipantSet();
        assertThat(participantSet).hasSize(6);
        Long finalApplicantUserId = applicantUserId + 1;
        assertThatThrownBy(() -> studyGroup.approveParticipant(HOST_ID, finalApplicantUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 방 인원이 가득 찼습니다.");
    }

    @Test
    @DisplayName("방장이 아닌 사람이 참여자를 승인, 거절하면 예외를 던진다.")
    void givenNotHostIdAndApplicantId_whenApproveAndRejectAndKick_thenThrowException() {
        // given
        Long notHostId = USER1_ID;
        Long applicantUserId = USER2_ID;

        studyGroup.apply(notHostId);
        studyGroup.approveParticipant(HOST_ID, notHostId);

        studyGroup.apply(applicantUserId);

        // when
        // then
        assertThatThrownBy(() -> studyGroup.approveParticipant(notHostId, applicantUserId))
                .isInstanceOf(NoHostAuthorityException.class)
                .hasMessage("해당 유저는 방장 권한이 없습니다.");
        assertThatThrownBy(() -> studyGroup.rejectParticipant(notHostId, applicantUserId))
                .isInstanceOf(NoHostAuthorityException.class)
                .hasMessage("해당 유저는 방장 권한이 없습니다.");
        assertThatThrownBy(() -> studyGroup.kickParticipant(notHostId, applicantUserId))
                .isInstanceOf(NoHostAuthorityException.class)
                .hasMessage("해당 유저는 방장 권한이 없습니다.");
    }

    @Test
    @DisplayName("방장은 승인 대기 중인 참여자를 거절할 수 있다.")
    void givenParticipantApply_whenRejectParticipant_thenRejectedParticipant() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);

        // when
        studyGroup.rejectParticipant(HOST_ID, applicantUserId);

        // then
        Set<Participant> participantSet = studyGroup.getParticipantSet();
        Participant participant = participantSet.stream()
                .filter(p -> p.userId().equals(applicantUserId))
                .findFirst()
                .get();

        assertThat(participant.userId()).isEqualTo(applicantUserId);
        assertThat(participant.status()).isEqualTo(ParticipantStatus.REJECTED);

        assertThat(participantSet)
                .contains(participant)
                .hasSize(2);
    }

    @Test
    @DisplayName("방장이 승인 대기중이 아닌 참여자를 거절하려고 하면 예외를 던진다.")
    void givenNotPendingParticipant_whenRejectParticipant_thenThrowException() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);
        studyGroup.approveParticipant(HOST_ID, applicantUserId);

        // when
        // then
        assertThatThrownBy(() -> studyGroup.rejectParticipant(HOST_ID, applicantUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기중인 유저가 아닙니다.");

    }

    @Test
    @DisplayName("방장은 참여자를 강퇴할 수 있다.")
    void givenApprovedParticipant_whenKickParticipant_thenKickedParticipant() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);
        studyGroup.approveParticipant(HOST_ID, applicantUserId);

        // when
        studyGroup.kickParticipant(HOST_ID, applicantUserId);

        // then
        Set<Participant> participantSet = studyGroup.getParticipantSet();
        Participant participant = participantSet.stream()
                .filter(p -> p.userId().equals(applicantUserId))
                .findFirst()
                .get();

        assertThat(participant.userId()).isEqualTo(applicantUserId);
        assertThat(participant.status()).isEqualTo(ParticipantStatus.KICKED);

        assertThat(participantSet)
                .contains(participant)
                .hasSize(2);
    }

    @Test
    @DisplayName("방장이 참여중이 아닌 참여자를 강퇴하려고 하면 예외를 던진다.")
    void givenNotApprovedParticipant_whenKickParticipant_thenThrowException() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);
        studyGroup.approveParticipant(HOST_ID, applicantUserId);

        // when
        // then
        assertThatThrownBy(() -> studyGroup.rejectParticipant(HOST_ID, applicantUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기중인 유저가 아닙니다.");

    }

    @Test
    @DisplayName("참여자는 신청한 스터디에 신청 취소를 할 수 있다.")
    void givenApplyParticipant_whenParticipantApplyCancel_thenCanceledParticipant() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);

        // when
        studyGroup.participantApplyCancel(applicantUserId);

        // then
        Set<Participant> participantSet = studyGroup.getParticipantSet();
        Participant participant = participantSet.stream()
                .filter(p -> p.userId().equals(applicantUserId))
                .findFirst()
                .get();

        assertThat(participant.userId()).isEqualTo(applicantUserId);
        assertThat(participant.status()).isEqualTo(ParticipantStatus.CANCELED);

        assertThat(participantSet)
                .contains(participant)
                .hasSize(2);
    }

    @Test
    @DisplayName("승인 대기중이 아닌 참여자는 신청 취소할 수 없다.")
    void givenNotPendingParticipant_whenParticipantApplyCancel_thenThrowException() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);
        studyGroup.approveParticipant(HOST_ID, applicantUserId);

        // when
        // then
        assertThatThrownBy(() -> studyGroup.participantApplyCancel(applicantUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기중인 유저가 아닙니다.");

    }

    @Test
    @DisplayName("참여자는 참여하고 있는 스터디를 나갈 수 있다.")
    void givenApprovedParticipant_whenParticipantLeave_thenLeaveParticipant() {
        // given
        Long applicantUserId = USER1_ID;
        studyGroup.apply(applicantUserId);
        studyGroup.approveParticipant(HOST_ID, applicantUserId);

        // when
        studyGroup.participantLeave(applicantUserId);

        // then
        Set<Participant> participantSet = studyGroup.getParticipantSet();
        Participant participant = participantSet.stream()
                .filter(p -> p.userId().equals(applicantUserId))
                .findFirst()
                .get();

        assertThat(participant.userId()).isEqualTo(applicantUserId);
        assertThat(participant.status()).isEqualTo(ParticipantStatus.LEAVE);

        assertThat(participantSet)
                .contains(participant)
                .hasSize(2);
    }

    @Test
    @DisplayName("해당 스터디 그룹의 방장은 그룹을 나갈 수 없다.")
    void givenHost_whenParticipantLeave_thenThrowException() {
        // given
        Participant host = studyGroup.getHost();

        // when
        // then
        assertThatThrownBy(() -> studyGroup.participantLeave(host.userId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("방장은 퇴장할 수 없습니다.");
    }
}