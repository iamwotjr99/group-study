package com.jaeseok.groupStudy.studyGroup.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("스터디 그룹 도메인 테스트")
class StudyGroupTest {

    private static final Long HOST_ID = 1L;
    private static final Long USER1_ID = 2L;
    private static final Long USER2_ID = 3L;


    StudyGroup studyGroup;

    @BeforeEach
    void setUp() {
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of("Test Study", 5, LocalDateTime.now().plusDays(1));
        studyGroup = StudyGroup.createWithHost(1L, HOST_ID, studyGroupInfo);
    }

    @Test
    @DisplayName("유저가 스터디 그룹을 만들 수 있다.")
    void givenStudyGroupInfoWithHostId_whenCreateStudyGroup_thenReturnStudyGroup() {
        // given
        StudyGroupInfo info = StudyGroupInfo.of("Spring Study", 3, LocalDateTime.now().plusDays(1));

        // when
        StudyGroup studyGroup = StudyGroup.createWithHost(1L, HOST_ID, info);

        // then
        assertThat(studyGroup.getHost().userId()).isEqualTo(HOST_ID);
        assertThat(studyGroup.getStudyGroupInfo()).isEqualTo(info);
    }

    @Test
    @DisplayName("스터디 그룹을 만들 때 제목이 없으면 예외를 던진다.")
    void givenGroupTitleIsNull_whenCreateStudyGroup_thenThrowException() {
        // given
        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> StudyGroupInfo.of(null, 3, LocalDateTime.now().plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> StudyGroupInfo.of("", 3, LocalDateTime.now().plusDays(1)));
    }

    @Test
    @DisplayName("스터디 그룹을 만들 때 제목이 20자보다 크면 예외를 던진다.")
    void givenGroupTitleOver20_whenCreateStudyGroup_thenThrowException() {
        // given
        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> StudyGroupInfo.of("aaaabbbbccccddddeeeef", 3, LocalDateTime.now().plusDays(1)));
    }

    @Test
    @DisplayName("스터디 그룹을 만들 때 그룹 인원수 2보다 작다면 예외를 던진다.")
    void givenGroupCapacityUnder2_whenCreateStudyGroup_thenThrowException() {
        // given
        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> StudyGroupInfo.of("SpringStudy", null, LocalDateTime.now().plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> StudyGroupInfo.of("SpringStudy", 0, LocalDateTime.now().plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> StudyGroupInfo.of("SpringStudy", 1, LocalDateTime.now().plusDays(1)));
    }

    @Test
    @DisplayName("유저는 스터디 그룹에 참여 신청을 할 수 있다.")
    void givenStudyGroupIdAndUserId_whenApply_thenReturnPendingParticipant() {
        // given
        Long userId = USER1_ID;
        Long studyGroupId = studyGroup.getId();

        // when
        Participant participant = Participant.apply(userId, studyGroupId);

        // then
        assertEquals(USER1_ID, participant.userId());
        assertEquals(studyGroupId, participant.studyGroupId());
        assertEquals(ParticipantStatus.PENDING, participant.status());
    }

    @Test
    @DisplayName("방장은 승인 대기 중인 참여자를 승인할 수 있다.")
    void givenParticipantApply_whenApproveParticipant_thenReturnApprovedParticipant() {
        // given
        Long studyGroupId = studyGroup.getId();
        Participant participant = Participant.apply(USER1_ID, studyGroupId);

        // when
        Participant approved = studyGroup.approveParticipant(HOST_ID, participant);

        // then
        assertThat(approved.userId()).isEqualTo(participant.userId());
        assertThat(approved.studyGroupId()).isEqualTo(participant.studyGroupId());
        assertThat(studyGroup.getParticipantSet())
                .hasSize(2)
                .contains(approved);
        assertThat(approved.status()).isEqualTo(ParticipantStatus.APPROVED);
    }

    @Test
    @DisplayName("정원이 가득 찬 그룹에 참여자를 승인하려고하면 예외를 던진다.")
    void givenPendingParticipantAndPullGroup_whenApproveParticipant_thenThrowException() {
        // given
        Long studyGroupId = studyGroup.getId();
        for (int i = 0; i < 4; i++) {
            Participant participant = Participant.apply(100L + i, studyGroupId);
            studyGroup.approveParticipant(HOST_ID, participant);
        }
        Participant participant6 = Participant.apply(7L, studyGroupId);

        // when
        // then
        assertEquals(5, studyGroup.getInfoCapacity());
        assertThrows(IllegalArgumentException.class, () -> studyGroup.approveParticipant(HOST_ID, participant6));
    }

    @Test
    @DisplayName("방장이 승인 대기중이 아닌 참여자를 승인하려고 하면 예외를 던진다.")
    void givenNotPendingParticipant_whenApproveParticipant_thenThrowException() {
        // given
        Participant[] notPendingParticipant = {
                new Participant(3L, studyGroup.getId(), ParticipantStatus.APPROVED, ParticipantRole.MEMBER),
                new Participant(4L, studyGroup.getId(), ParticipantStatus.REJECTED, ParticipantRole.MEMBER),
                new Participant(5L, studyGroup.getId(), ParticipantStatus.CANCELED, ParticipantRole.MEMBER),
                new Participant(6L, studyGroup.getId(), ParticipantStatus.LEAVE, ParticipantRole.MEMBER),
                new Participant(7L, studyGroup.getId(), ParticipantStatus.KICKED, ParticipantRole.MEMBER)
        };

        // when
        // then
        for (Participant p : notPendingParticipant) {
            assertThatThrownBy(() -> studyGroup.approveParticipant(HOST_ID, p))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("대기중인 유저가 아닙니다.");
        }
    }

    @Test
    @DisplayName("방장이 아닌 사람은 방장의 권한을 수행할 수 없다.")
    void givenNotHostIdAndParticipant_whenApproveAndRejectAndKick_thenThrowException() {
        // given
        Long notHostId = USER1_ID;
        Participant participant = Participant.apply(USER2_ID, studyGroup.getId());

        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> studyGroup.approveParticipant(notHostId, participant));
        assertThrows(IllegalArgumentException.class, () -> studyGroup.rejectParticipant(notHostId, participant));
        assertThrows(IllegalArgumentException.class, () -> studyGroup.kickParticipant(notHostId, participant));
    }

    @Test
    @DisplayName("방장은 승인 대기 중인 참여자를 거절할 수 있다.")
    void givenParticipantApply_whenRejectParticipant_thenRejectedParticipant() {
        // given
        Long studyGroupId = studyGroup.getId();
        Participant participant = Participant.apply(USER1_ID, studyGroupId);

        // when
        Participant rejected = studyGroup.rejectParticipant(HOST_ID, participant);

        // then
        assertEquals(participant.userId(), rejected.userId());
        assertEquals(participant.studyGroupId(), rejected.studyGroupId());
        assertEquals(ParticipantStatus.REJECTED, rejected.status());
    }

    @Test
    @DisplayName("방장이 승인 대기중이 아닌 참여자를 거절하려고 하면 예외를 던진다.")
    void givenNotPendingParticipant_whenRejectParticipant_thenThrowException() {
        // given
        Participant[] notPendingParticipant = {
                new Participant(3L, studyGroup.getId(), ParticipantStatus.APPROVED, ParticipantRole.MEMBER),
                new Participant(4L, studyGroup.getId(), ParticipantStatus.REJECTED, ParticipantRole.MEMBER),
                new Participant(5L, studyGroup.getId(), ParticipantStatus.CANCELED, ParticipantRole.MEMBER),
                new Participant(6L, studyGroup.getId(), ParticipantStatus.LEAVE, ParticipantRole.MEMBER),
                new Participant(7L, studyGroup.getId(), ParticipantStatus.KICKED, ParticipantRole.MEMBER)
        };

        // when
        // then
        for (Participant p : notPendingParticipant) {
            assertThatThrownBy(() -> studyGroup.rejectParticipant(HOST_ID, p))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("대기중인 유저가 아닙니다.");
        }
    }

    @Test
    @DisplayName("방장은 참여자를 강퇴할 수 있다.")
    void givenApprovedParticipant_whenKickParticipant_thenKickedParticipant() {
        // given
        Participant participant = Participant.apply(USER1_ID, studyGroup.getId());
        Participant approved = studyGroup.approveParticipant(HOST_ID, participant);

        // when
        Participant kicked = studyGroup.kickParticipant(HOST_ID, approved);

        // then
        assertEquals(USER1_ID, kicked.userId());
        assertEquals(studyGroup.getId(), kicked.studyGroupId());
        assertEquals(1, studyGroup.getParticipantSet().size());
        assertFalse(studyGroup.getParticipantSet().contains(kicked));
        assertEquals(ParticipantStatus.KICKED, kicked.status());
    }

    @Test
    @DisplayName("방장이 참여중이 아닌 참여자를 강퇴하려고 하면 예외를 던진다.")
    void givenNotApprovedParticipant_whenKickParticipant_thenThrowException() {
        // given
        Participant notApprovedParticipant = Participant.apply(USER1_ID, studyGroup.getId());

        // when
        // then
        assertThrows(IllegalStateException.class, () -> studyGroup.kickParticipant(HOST_ID, notApprovedParticipant));
    }

    @Test
    @DisplayName("참여자는 신청한 스터디에 신청 취소를 할 수 있다.")
    void givenApplyParticipant_whenParticipantCancel_thenCanceledParticipant() {
        // given
        Participant appliedParticipant = Participant.apply(USER1_ID, studyGroup.getId());

        // when
        Participant canceledParticipant = studyGroup.participantCancel(appliedParticipant);

        // then
        assertThat(appliedParticipant.status()).isEqualTo(ParticipantStatus.PENDING);
        assertThat(canceledParticipant.status()).isEqualTo(ParticipantStatus.CANCELED);
    }

    @Test
    @DisplayName("해당 스터디 그룹에 신청하지 않은 참여자는 신청 취소할 수 없다.")
    void givenAnotherPendingParticipant_whenParticipantCancel_thenThrowException() {
        // given
        Long anotherGroupId = 200L;

        Participant appliedParticipant = Participant.apply(USER1_ID, anotherGroupId);

        // when
        // then
        assertThatThrownBy(() -> studyGroup.participantCancel(appliedParticipant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 참여자는 이 스터디 그룹의 소속이 아닙니다.");
    }

    @Test
    @DisplayName("승인 대기중이 아닌 참여자는 신청 취소할 수 없다.")
    void givenNotPendingParticipant_whenParticipantCancel_thenThrowException() {
        // given
        Participant[] notPendingParticipant = {
                new Participant(3L, studyGroup.getId(), ParticipantStatus.APPROVED, ParticipantRole.MEMBER),
                new Participant(4L, studyGroup.getId(), ParticipantStatus.REJECTED, ParticipantRole.MEMBER),
                new Participant(5L, studyGroup.getId(), ParticipantStatus.CANCELED, ParticipantRole.MEMBER),
                new Participant(6L, studyGroup.getId(), ParticipantStatus.LEAVE, ParticipantRole.MEMBER),
                new Participant(7L, studyGroup.getId(), ParticipantStatus.KICKED, ParticipantRole.MEMBER)
        };

        // when
        // then
        for (Participant p : notPendingParticipant) {
            assertThatThrownBy(() -> studyGroup.participantCancel(p))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("대기중인 유저가 아닙니다.");
        }
    }

    @Test
    @DisplayName("참여자는 참여하고 있는 스터디를 나갈 수 있다.")
    void givenApprovedParticipant_whenParticipantLeave_thenLeaveParticipant() {
        // given
        Participant appliedParticipant = Participant.apply(USER1_ID, studyGroup.getId());
        Participant approvedParticipant = studyGroup.approveParticipant(HOST_ID, appliedParticipant);

        // when
        Participant leftParticipant = studyGroup.participantLeave(approvedParticipant);

        // then
        assertThat(appliedParticipant.status()).isEqualTo(ParticipantStatus.PENDING);
        assertThat(approvedParticipant.status()).isEqualTo(ParticipantStatus.APPROVED);
        assertThat(leftParticipant.status()).isEqualTo(ParticipantStatus.LEAVE);
    }

    @Test
    @DisplayName("해당 스터디 그룹에 참여중이 아닌 참여자는 그룹을 나갈 수 없다.")
    void givenNotApprovedParticipant_whenParticipantLeave_thenThrowException() {
        // given
        Participant[] notApprovedParticipant = {
                new Participant(3L, studyGroup.getId(), ParticipantStatus.PENDING, ParticipantRole.MEMBER),
                new Participant(4L, studyGroup.getId(), ParticipantStatus.REJECTED, ParticipantRole.MEMBER),
                new Participant(5L, studyGroup.getId(), ParticipantStatus.CANCELED, ParticipantRole.MEMBER),
                new Participant(6L, studyGroup.getId(), ParticipantStatus.LEAVE, ParticipantRole.MEMBER),
                new Participant(7L, studyGroup.getId(), ParticipantStatus.KICKED, ParticipantRole.MEMBER)
        };

        // when
        // then
        for (Participant p : notApprovedParticipant) {
            assertThatThrownBy(() -> studyGroup.participantLeave(p))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 유저는 이 스터디 그룹의 참여자가 아닙니다.");
        }
    }

    @Test
    @DisplayName("해당 스터디 그룹의 방장은 그룹을 나갈 수 없다.")
    void givenHost_whenParticipantLeave_thenThrowException() {
        // given
        Participant host = studyGroup.getHost();

        // when
        // then
        assertThatThrownBy(() -> studyGroup.participantLeave(host))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("방장은 퇴장할 수 없습니다.");
    }
}