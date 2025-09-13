package com.jaeseok.groupStudy.studyGroup.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApplyStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CancelStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.LeaveStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupRepository;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyGroup Participant Service 테스트")
class StudyGroupParticipantServiceImplTest {

    @InjectMocks
    StudyGroupParticipantServiceImpl studyGroupParticipantService;

    @Mock
    StudyGroupRepository studyGroupRepository;

    final Long STUDY_GROUP_ID = 100L;
    final Long HOST_ID = 1L;
    final Long USER_ID = 2L;

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
    @DisplayName("유저는 스터디 그룹에 참여 신청할 수 있다.")
    void givenApplyCommand_whenApplyForStudyGroup_thenChangeParticipantSetAndStatus() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long applicantId = 3L;

        ApplyStudyGroupCommand cmd = new ApplyStudyGroupCommand(studyGroupId, applicantId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when
        studyGroupParticipantService.applyForStudyGroup(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupRepository, times(1)).update(studyGroupCaptor.capture());

        StudyGroup studyGroupCaptorValue = studyGroupCaptor.getValue();
        assertThat(studyGroupCaptorValue.getParticipantSet())
                .hasSize(3)
                .extracting(Participant::userId, Participant::status)
                .contains(
                        tuple(HOST_ID, ParticipantStatus.APPROVED),
                        tuple(USER_ID, ParticipantStatus.APPROVED),
                        tuple(applicantId, ParticipantStatus.PENDING)
                );
    }

    @Test
    @DisplayName("이미 신청했거나 신청중인 스터디 그룹에 신청 시도를 하면 예외를 던진다.")
    void givenAlreadyExistInGroup_whenApplyForStudyGroup_thenThrowException() {
        // given
        Long alreadyExistUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        ApplyStudyGroupCommand cmd = new ApplyStudyGroupCommand(studyGroupId,
                alreadyExistUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupParticipantService.applyForStudyGroup(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 신청중이거나 참여중인 스터디 그룹입니다.");

        verify(studyGroupRepository, never()).update(studyGroup);
    }


    @Test
    @DisplayName("유저는 스터디 그룹에 참여 신청을 취소할 수 있다.")
    void givenCancelCommand_whenCancelApplication_thenChangeToCancel() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long applicantId = 3L;

        studyGroup.apply(applicantId);

        CancelStudyGroupCommand cmd = new CancelStudyGroupCommand(studyGroupId, applicantId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when
        studyGroupParticipantService.cancelApplication(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupRepository, times(1)).update(studyGroupCaptor.capture());

        assertParticipantStatus(studyGroupCaptor, applicantId, ParticipantStatus.CANCELED);
    }

    @Test
    @DisplayName("'대기중' 상태가 아닌 유저가 참여 신청을 취소하면 예외를 던진다.")
    void givenNotPendingUser_whenCancelApplication_theThrowException() {
        // given
        Long alreadyExistUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        CancelStudyGroupCommand cmd = new CancelStudyGroupCommand(studyGroupId,
                alreadyExistUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupParticipantService.cancelApplication(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대기중인 유저가 아닙니다.");

        verify(studyGroupRepository, never()).update(studyGroup);
    }

    @Test
    @DisplayName("스터디 그룹 참여자는 스터디 그룹을 떠날 수 있다.")
    void givenLeaveCommand_whenLeaveStudyGroup_thenChangeToLeave() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long leaveId = USER_ID;

        LeaveStudyGroupCommand cmd = new LeaveStudyGroupCommand(studyGroupId, leaveId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when
        studyGroupParticipantService.leaveStudyGroup(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupRepository, times(1)).update(studyGroupCaptor.capture());

        StudyGroup studyGroupCaptorValue = studyGroupCaptor.getValue();
        assertThat(studyGroupCaptorValue.getParticipantSet())
                .hasSize(2)
                .extracting(Participant::userId, Participant::status)
                .contains(
                        tuple(HOST_ID, ParticipantStatus.APPROVED),
                        tuple(leaveId, ParticipantStatus.LEAVE)
                );
    }

    @Test
    @DisplayName("방장이 스터디 그룹 퇴장 시도를 하면 예외를 던진다.")
    void givenHost_whenLeaveStudyGroup_thenThrowReturn() {
        // given
        Long hostId = HOST_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        LeaveStudyGroupCommand cmd = new LeaveStudyGroupCommand(studyGroupId,
                hostId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupParticipantService.leaveStudyGroup(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장은 퇴장할 수 없습니다.");

        verify(studyGroupRepository, never()).update(studyGroup);
    }

    private void assertParticipantStatus(ArgumentCaptor<StudyGroup> captor, Long assertTargetId, ParticipantStatus status) {
        StudyGroup studyGroupCaptorValue = captor.getValue();
        assertThat(studyGroupCaptorValue.getParticipantSet())
                .hasSize(3)
                .extracting(Participant::userId, Participant::status)
                .contains(
                        tuple(HOST_ID, ParticipantStatus.APPROVED),
                        tuple(USER_ID, ParticipantStatus.APPROVED),
                        tuple(assertTargetId, status)
                );
    }
}