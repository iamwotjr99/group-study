package com.jaeseok.groupStudy.studyGroup.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.jaeseok.groupStudy.participant.domain.Participant;
import com.jaeseok.groupStudy.participant.domain.ParticipantStatus;
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
        studyGroup = StudyGroup.createForTest(1L, HOST_ID, studyGroupInfo);
    }

    @Test
    @DisplayName("유저가 스터디 그룹을 만들 수 있다.")
    void givenStudyGroupInfoWithHostId_whenCreateStudyGroup_thenReturnStudyGroup() {
        // given
        StudyGroupInfo info = StudyGroupInfo.of("Spring Study", 3, LocalDateTime.now().plusDays(1));
        // when
        StudyGroup studyGroup = StudyGroup.create(HOST_ID, info);

        // then
        assertEquals(HOST_ID, studyGroup.getHostId());
        assertEquals(info.getTitle(), studyGroup.getInfoTitle());
        assertEquals(info.getCapacity(), studyGroup.getInfoCapacity());
        assertEquals(info.getDeadline(), studyGroup.getInfoDeadline());
        assertEquals(RecruitingPolicy.APPROVAL, studyGroup.getInfoPolicy());
        assertEquals(GroupStatus.RECRUITING, studyGroup.getInfoState());
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
        assertEquals(ParticipantStatus.PENDING, participant.state());
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
        assertEquals(participant.userId(), approved.userId());
        assertEquals(participant.studyGroupId(), approved.studyGroupId());
        assertEquals(1, studyGroup.getParticipantSet().size());
        assertTrue(studyGroup.getParticipantSet().contains(approved));
        assertEquals(ParticipantStatus.APPROVED, approved.state());
    }

    @Test
    @DisplayName("정원이 가득 찬 그룹에 참여자를 승인하려고하면 예외를 던진다.")
    void givenPendingParticipantAndPullGroup_whenApproveParticipant_thenThrowException() {
        // given
        Long studyGroupId = studyGroup.getId();
        for (int i = 0; i < 5; i++) {
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
        Participant approved = new Participant(3L, studyGroup.getId(), ParticipantStatus.APPROVED);
        Participant rejected = new Participant(4L, studyGroup.getId(), ParticipantStatus.REJECTED);
        Participant canceled = new Participant(5L, studyGroup.getId(), ParticipantStatus.CANCELED);
        Participant leaved = new Participant(6L, studyGroup.getId(), ParticipantStatus.LEAVE);
        Participant kicked = new Participant(7L, studyGroup.getId(), ParticipantStatus.KICKED);

        // when
        // then
        assertThrows(IllegalStateException.class, () -> studyGroup.approveParticipant(HOST_ID, approved));
        assertThrows(IllegalStateException.class, () -> studyGroup.approveParticipant(HOST_ID, rejected));
        assertThrows(IllegalStateException.class, () -> studyGroup.approveParticipant(HOST_ID, canceled));
        assertThrows(IllegalStateException.class, () -> studyGroup.approveParticipant(HOST_ID, leaved));
        assertThrows(IllegalStateException.class, () -> studyGroup.approveParticipant(HOST_ID, kicked));
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
        assertEquals(ParticipantStatus.REJECTED, rejected.state());
    }

    @Test
    @DisplayName("방장이 승인 대기중이 아닌 참여자를 거절하려고 하면 예외를 던진다.")
    void givenNotPendingParticipant_whenRejectParticipant_thenThrowException() {
        // given
        Participant approved = new Participant(3L, studyGroup.getId(), ParticipantStatus.APPROVED);
        Participant rejected = new Participant(4L, studyGroup.getId(), ParticipantStatus.REJECTED);
        Participant canceled = new Participant(5L, studyGroup.getId(), ParticipantStatus.CANCELED);
        Participant leaved = new Participant(6L, studyGroup.getId(), ParticipantStatus.LEAVE);
        Participant kicked = new Participant(7L, studyGroup.getId(), ParticipantStatus.KICKED);

        // when
        // then
        assertThrows(IllegalStateException.class, () -> studyGroup.rejectParticipant(HOST_ID, approved));
        assertThrows(IllegalStateException.class, () -> studyGroup.rejectParticipant(HOST_ID, rejected));
        assertThrows(IllegalStateException.class, () -> studyGroup.rejectParticipant(HOST_ID, canceled));
        assertThrows(IllegalStateException.class, () -> studyGroup.rejectParticipant(HOST_ID, leaved));
        assertThrows(IllegalStateException.class, () -> studyGroup.rejectParticipant(HOST_ID, kicked));
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
        assertEquals(0, studyGroup.getParticipantSet().size());
        assertFalse(studyGroup.getParticipantSet().contains(kicked));
        assertEquals(ParticipantStatus.KICKED, kicked.state());
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
}