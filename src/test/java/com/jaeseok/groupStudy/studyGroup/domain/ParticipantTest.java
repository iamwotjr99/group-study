package com.jaeseok.groupStudy.studyGroup.domain;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParticipantTest {

    final Long HOST_ID = 1L;
    final Long USER_ID = 2L;
    final Long STUDY_GROUP_ID = 100L;

    Participant participant;

    @BeforeEach
    void setUp() {
        participant = Participant.of(USER_ID, STUDY_GROUP_ID, ParticipantStatus.PENDING, ParticipantRole.MEMBER);
    }

    @Test
    @DisplayName("참여자는 apply()하면 PENDING(승인 대기) 상태와 MEMBER 역할을 가진다.")
    void givenStudyGroupIdAndUserId_whenApply_thenReturnPendingMemberParticipant() {
        // when
        Participant participant = Participant.apply(USER_ID, STUDY_GROUP_ID);

        // then
        assertThat(participant.userId()).isEqualTo(USER_ID);
        assertThat(participant.studyGroupId()).isEqualTo(STUDY_GROUP_ID);
        assertThat(participant.status()).isEqualTo(ParticipantStatus.PENDING);
        assertThat(participant.role()).isEqualTo(ParticipantRole.MEMBER);
    }

    @Test
    @DisplayName("참여자는 host()하면 APPROVED(승인) 상태와 HOST 역할을 가진다.")
    void givenStudyGroupIdAndUserId_whenHost_thenReturnApproveHostParticipant() {
        // when
        Participant participant = Participant.host(HOST_ID, STUDY_GROUP_ID);

        // then
        assertThat(participant.userId()).isEqualTo(HOST_ID);
        assertThat(participant.studyGroupId()).isEqualTo(STUDY_GROUP_ID);
        assertThat(participant.status()).isEqualTo(ParticipantStatus.APPROVED);
        assertThat(participant.role()).isEqualTo(ParticipantRole.HOST);
    }

    @Test
    @DisplayName("참여자는 approve()하면 APPROVED(승인됨) 상태가 된다.")
    void givenParticipant_whenApprove_thenReturnApprovedParticipant() {
        // when
        Participant approved = participant.approve();

        // then
        assertThat(approved.status()).isEqualTo(ParticipantStatus.APPROVED);
    }

    @Test
    @DisplayName("참여자는 reject()하면 REJECTED(거절됨) 상태가 된다.")
    void givenParticipant_whenReject_thenReturnApprovedParticipant() {
        // when
        Participant rejected = participant.reject();

        // then
        assertThat(rejected.status()).isEqualTo(ParticipantStatus.REJECTED);
    }

    @Test
    @DisplayName("참여자는 kick()하면 kicked(강퇴됨) 상태가 된다.")
    void givenParticipant_whenKick_thenReturnApprovedParticipant() {
        // when
        Participant kicked = participant.kick();

        // then
        assertThat(kicked.status()).isEqualTo(ParticipantStatus.KICKED);
    }

    @Test
    @DisplayName("참여자는 cancel()하면 CANCELED(신청 취소) 상태가 된다.")
    void givenPendingParticipant_whenCancel_thenReturnCancelledParticipant() {

        // when
        Participant canceled = participant.cancel();

        // then
        assertThat(canceled.status()).isEqualTo(ParticipantStatus.CANCELED);
    }

    @Test
    @DisplayName("참여자는 leave()하면 LEAVED(탈퇴) 상태가 된다.")
    void givenApprovedParticipant_whenLeave_thenReturnLeftParticipant() {
        // given
        Participant approved = Participant.host(USER_ID, STUDY_GROUP_ID);

        // when
        Participant left = approved.leave();

        // then
        assertThat(left.status()).isEqualTo(ParticipantStatus.LEAVE);

    }

    @Test
    @DisplayName("참여자의 권한이 방장인지 체크")
    void givenHostRoleParticipant_whenIsHost_thenReturnTrue() {
        // given
        Participant host = Participant.host(USER_ID, STUDY_GROUP_ID);

        // when
        boolean isHost = host.isHost();

        // then
        assertThat(isHost).isTrue();
    }

    @Test
    @DisplayName("참여자의 권한이 멤버인지 체크")
    void givenMemberRoleParticipant_whenIsMember_thenReturnTrue() {
        // given
        Participant member = Participant.apply(USER_ID, STUDY_GROUP_ID);

        // when
        boolean isMember = member.isMember();

        // then
        assertThat(isMember).isTrue();
    }

    @Test
    @DisplayName("userId, studyGroupId가 같으면 동등한 Participant로 판단")
    void givenSameParticipant_whenEqualsAndHashCode_thenReturnTrue() {
        // given
        Participant participant1 = Participant.apply(USER_ID, STUDY_GROUP_ID);
        Participant participant2 = Participant.apply(USER_ID, STUDY_GROUP_ID);

        // when
        boolean equals = participant1.equals(participant2);
        boolean hashCode = participant1.hashCode() == participant2.hashCode();

        // then
        assertThat(equals).isTrue();
        assertThat(hashCode).isTrue();

    }
}