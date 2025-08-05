package com.jaeseok.groupStudy.studyGroup.domain;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParticipantTest {

    private static final Long HOST_ID = 1L;
    private static final Long USER_ID = 2L;


    StudyGroup studyGroup;

    @BeforeEach
    void setUp() {
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.defaultInfo("Test Study", 5, LocalDateTime.now().plusDays(1));
        studyGroup = StudyGroup.createWithHost(1L, HOST_ID, studyGroupInfo);
    }

    @Test
    @DisplayName("참여자는 apply()하면 PENDING(승인 대기) 상태가 된다.")
    void givenStudyGroupIdAndUserId_whenApply_thenReturnPendingParticipant() {
        // given
        Long userId = USER_ID;
        Long studyGroupId = studyGroup.getId();

        // when
        Participant participant = Participant.apply(userId, studyGroupId);

        // then
        assertThat(participant.userId()).isEqualTo(userId);
        assertThat(participant.studyGroupId()).isEqualTo(studyGroupId);
        assertThat(participant.status()).isEqualTo(ParticipantStatus.PENDING);
        assertThat(participant.role()).isEqualTo(ParticipantRole.MEMBER);
    }

    @Test
    @DisplayName("참여자는 PENDING(승인 대기)일 때 cancel()하면 CANCELED(신청 취소) 상태가 된다.")
    void givenPendingParticipant_whenCancel_thenReturnCancelledParticipant() {
        // given
        Participant participant = Participant.apply(USER_ID, studyGroup.getId());

        // when
        Participant canceled = participant.cancel();

        // then
        assertThat(canceled.status()).isEqualTo(ParticipantStatus.CANCELED);
    }

    @Test
    @DisplayName("참여자는 PENDING(승인 대기)이 아닐 땐 cancel() 할 수 없다.")
    void givenNotPendingParticipant_whenCancel_thenThrowException() {
        // given
        Participant notPending = Participant.host(USER_ID, studyGroup.getId());

        // when
        // then
        assertThatThrownBy(notPending::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("대기 상태에서만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("참여자는 APPROVED(승인) 상태일 때 leave()하면 LEAVED(탈퇴) 상태가 된다.")
    void givenApprovedParticipant_whenLeave_thenReturnLeftParticipant() {
        // given
        Participant approved = Participant.host(USER_ID, studyGroup.getId());

        // when
        Participant left = approved.leave();

        // then
        assertThat(left.status()).isEqualTo(ParticipantStatus.LEAVE);

    }

    @Test
    @DisplayName("참여자는 APPROVED(승인) 상태가 아닐 때 leave()할 수 없다.")
    void givenNotApprovedParticipant_whenLeave_thenThrowException() {
        // given
        Participant notApproved = Participant.apply(USER_ID, studyGroup.getId());

        // when
        // then
        assertThatThrownBy(notApproved::leave)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("승인된 상태에서만 탈퇴할 수 있습니다.");
    }

    @Test
    @DisplayName("참여자의 권한이 방장인지 체크")
    void givenHostRoleParticipant_whenIsHost_thenReturnTrue() {
        // given
        Participant host = Participant.host(USER_ID, studyGroup.getId());

        // when
        boolean isHost = host.isHost();

        // then
        assertThat(isHost).isTrue();
    }

    @Test
    @DisplayName("참여자의 권한이 멤버인지 체크")
    void givenMemberRoleParticipant_whenIsMember_thenReturnTrue() {
        // given
        Participant member = Participant.apply(USER_ID, studyGroup.getId());

        // when
        boolean isMember = member.isMember();

        // then
        assertThat(isMember).isTrue();
    }

    @Test
    @DisplayName("userId, studyGroupId가 같으면 동등한 Participant로 판단")
    void givenSameParticipant_whenEqualsAndHashCode_thenReturnTrue() {
        // given
        Participant participant1 = Participant.apply(USER_ID, studyGroup.getId());
        Participant participant2 = Participant.apply(USER_ID, studyGroup.getId());

        // when
        boolean equals = participant1.equals(participant2);
        boolean hashCode = participant1.hashCode() == participant2.hashCode();

        // then
        assertThat(equals).isTrue();
        assertThat(hashCode).isTrue();

    }
}