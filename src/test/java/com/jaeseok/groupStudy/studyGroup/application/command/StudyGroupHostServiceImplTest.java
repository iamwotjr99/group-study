package com.jaeseok.groupStudy.studyGroup.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApproveStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.KickStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.RejectStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyGroup Host Service 테스트")
class StudyGroupHostServiceImplTest {

    @InjectMocks
    StudyGroupHostServiceImpl studyGroupHostService;

    @Mock
    StudyGroupCommandRepository studyGroupCommandRepository;

    final Long HOST_ID = 1L;
    final Long USER_ID = 2L;
    final Long STUDY_GROUP_ID = 100L;
    StudyGroup studyGroup;

    @BeforeEach
    void setUp() {
        Participant host = Participant.host(HOST_ID, STUDY_GROUP_ID);
        Participant participant = Participant.of(USER_ID, STUDY_GROUP_ID,
                ParticipantStatus.APPROVED, ParticipantRole.MEMBER);
        Set<Participant> participants = new HashSet<>();
        participants.add(host);
        participants.add(participant);

        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
        studyGroup = StudyGroup.of(STUDY_GROUP_ID, studyGroupInfo, participants);
    }

    @Test
    @DisplayName("방장은 참여 신청자를 승인할 수 있다.")
    void givenApproveCommand_whenApproveApplication_thenChangeToApproved() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long hostId = HOST_ID;

        Long applicantUserId = 3L;
        studyGroup.apply(applicantUserId);

        ApproveStudyGroupCommand cmd = new ApproveStudyGroupCommand(
                studyGroupId, hostId, applicantUserId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when
        studyGroupHostService.approveApplication(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupCommandRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupCommandRepository, times(1)).update(studyGroupCaptor.capture());

        assertParticipantStatus(studyGroupCaptor, applicantUserId, ParticipantStatus.APPROVED);
    }

    @Test
    @DisplayName("방장이 아닌 유저가 승인을 시도하면 예외를 던진다.")
    void givenNotHostUser_whenApproveApplication_thenThrowException() {
        // given
        Long notHostUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long applicantUserId = 3L;
        studyGroup.apply(applicantUserId);

        ApproveStudyGroupCommand cmd = new ApproveStudyGroupCommand(studyGroupId, notHostUserId,
                applicantUserId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.approveApplication(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupCommandRepository, never()).update(studyGroup);
    }

    @Test
    @DisplayName("'대기중' 상태가 아닌 유저를 승인 시도하면 예외를 던진다.")
    void givenNotPendingUser_whenApproveApplication_thenThrowException() {
        // given
        Long notPendingUserId = USER_ID;
        Long hostId = HOST_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        ApproveStudyGroupCommand cmd = new ApproveStudyGroupCommand(studyGroupId, hostId,
                notPendingUserId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.approveApplication(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대기중인 유저가 아닙니다.");

        verify(studyGroupCommandRepository, never()).update(studyGroup);
    }

    @Test
    @DisplayName("방 인원이 가득찼을 때, 참여 신청자를 승인 시도하면 예외를 던진다.")
    void givenFullStudyGroup_whenApproveApplication_thenThrowException() {
        // given
        Long hostId = HOST_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long approvedId = 3L;
        studyGroup.apply(approvedId);
        studyGroup.approveParticipant(hostId, approvedId); // 방 인원 제한 3명으로 꽉차게 설정

        Long applicantId = 4L;
        studyGroup.apply(applicantId);

        ApproveStudyGroupCommand cmd = new ApproveStudyGroupCommand(studyGroupId, hostId, applicantId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.approveApplication(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가득 찼습니다.");

        verify(studyGroupCommandRepository, never()).update(studyGroup);
    }

    @Test
    @DisplayName("방장은 참여 신청자를 거절할 수 있다.")
    void givenRejectCommand_whenRejectApplication_thenChangeToRejected() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long hostId = HOST_ID;

        Long applicantUserId = 3L;
        studyGroup.apply(applicantUserId);

        RejectStudyGroupCommand cmd = new RejectStudyGroupCommand(studyGroupId,
                hostId, applicantUserId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when
        studyGroupHostService.rejectApplication(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupCommandRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupCommandRepository, times(1)).update(studyGroupCaptor.capture());

        assertParticipantStatus(studyGroupCaptor, applicantUserId, ParticipantStatus.REJECTED);
    }

    @Test
    @DisplayName("방장이 아닌 유저가 신청자를 거절하려고 하면 예외를 던진다.")
    void givenNotHostUser_whenRejectApplication_thenThrowException() {
        // given
        Long notHostUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long applicantId = 3L;
        studyGroup.apply(applicantId);

        RejectStudyGroupCommand cmd = new RejectStudyGroupCommand(studyGroupId, notHostUserId, applicantId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.rejectApplication(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupCommandRepository, never()).update(studyGroup);
    }

    @Test
    @DisplayName("'대기중'이 아닌 유저를 거절하려고하면 예외를 던진다.")
    void givenNotPendingUser_whenRejectApplication_thenThrowException() {
        // given
        Long notPendingUserId = USER_ID;
        Long hostId = HOST_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        RejectStudyGroupCommand cmd = new RejectStudyGroupCommand(studyGroupId,
                hostId, notPendingUserId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.rejectApplication(cmd))
                .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("대기중인 유저가 아닙니다.");
        verify(studyGroupCommandRepository, never()).update(studyGroup);
    }

    @Test
    @DisplayName("방장은 스터디그룹 참여자를 강퇴할 수 있다.")
    void givenKickCommand_whenKickParticipation_thenChangeToKicked() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long hostId = HOST_ID;

        Long participantId = 3L;
        studyGroup.apply(participantId);
        studyGroup.approveParticipant(hostId, participantId);

        KickStudyGroupCommand cmd = new KickStudyGroupCommand(studyGroupId,
                hostId, participantId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when
        studyGroupHostService.kickParticipation(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupCommandRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupCommandRepository, times(1)).update(studyGroupCaptor.capture());

        assertParticipantStatus(studyGroupCaptor, participantId, ParticipantStatus.KICKED);
    }

    @Test
    @DisplayName("방장이 아닌 유저가 스터디그룹 참여자를 강퇴 시도를 하면 예외를 던진다.")
    void givenNotHostUser_whenKickParticipation_thenThrowException() {
        // given
        Long notHostUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long approvedId = 3L;
        studyGroup.apply(approvedId);
        studyGroup.approveParticipant(HOST_ID, approvedId);

        KickStudyGroupCommand cmd = new KickStudyGroupCommand(studyGroupId,
                notHostUserId, approvedId);

        given(studyGroupCommandRepository.findById(studyGroupId)).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.kickParticipation(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupCommandRepository, never()).update(studyGroup);
    }

    @Test
    @DisplayName("'승인됨'이 아닌 유저를 강퇴 시도를 하면 예외를 던진다.")
    void givenNotApprovedUser_whenKickParticipation_thenThrowException() {
        // given
        Long hostId = HOST_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long applicantId = 3L;
        studyGroup.apply(applicantId);

        KickStudyGroupCommand cmd = new KickStudyGroupCommand(studyGroupId,
                hostId, applicantId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.kickParticipation(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("참여중인 유저가 아닙니다.");

        verify(studyGroupCommandRepository, never()).update(studyGroup);
    }

    private void assertParticipantStatus(ArgumentCaptor<StudyGroup> captor, Long assertTargetId, ParticipantStatus status) {
        StudyGroup studyGroupCaptorValue = captor.getValue();
        assertThat(studyGroupCaptorValue.getParticipantSet())
                .hasSize(3)
                .extracting(Participant::userId, Participant::status)
                .contains(
                        tuple(HOST_ID, ParticipantStatus.APPROVED),
                        tuple(USER_ID, ParticipantStatus.APPROVED),
                        tuple(assertTargetId, status));

        assertThat(studyGroupCaptorValue.getParticipantSet())
                .filteredOn(p -> p.userId().equals(assertTargetId))
                .hasSize(1)
                .first()
                .extracting(Participant::status)
                .isEqualTo(status);
    }
}