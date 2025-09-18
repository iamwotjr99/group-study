package com.jaeseok.groupStudy.unit.studyGroup.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.studyGroup.application.command.StudyGroupLifecycleServiceImpl;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CloseStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StartStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.exception.NoHostAuthorityException;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyGroupLifecycle 테스트")
class StudyGroupLifecycleServiceImplTest {

    @InjectMocks
    StudyGroupLifecycleServiceImpl studyGroupLifecycleService;

    @Mock
    StudyGroupCommandRepository studyGroupCommandRepository;

    final Long HOST_ID = 1L;

    @Test
    @DisplayName("스터디 그룹 생성 명령이 주어지면 스터디 그룹을 생성한다.")
    void givenCreateCommand_whenCreateStudyGroup_thenReturnStudyGroup() {
        // given
        CreateStudyGroupCommand cmd = new CreateStudyGroupCommand(HOST_ID,
                "테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);

        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of(cmd.title(), cmd.capacity(),
                cmd.deadline(), cmd.policy(), GroupState.RECRUITING);

        Long fakeStudyGroupId = 100L;
        StudyGroup willCreatedStudyGroup = StudyGroup.of(fakeStudyGroupId, studyGroupInfo, Collections.EMPTY_SET);
        given(studyGroupCommandRepository.save(any(StudyGroup.class))).willReturn(willCreatedStudyGroup);

        // when
        CreateStudyGroupInfo createStudyGroupInfo = studyGroupLifecycleService.createStudyGroup(cmd);

        // then
        assertThat(createStudyGroupInfo.studyGroupId()).isNotNull();
        assertThat(createStudyGroupInfo.studyGroupId()).isEqualTo(willCreatedStudyGroup.getId());

        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupCommandRepository, times(1)).save(studyGroupCaptor.capture());

        StudyGroup capturedStudyGroup = studyGroupCaptor.getValue();
        assertThat(capturedStudyGroup.getStudyGroupInfo()).isEqualTo(studyGroupInfo);
    }

    @Test
    @DisplayName("스터디 그룹 시작 명령이 주어지면 스터디 그룹의 상태를 '진행중' 상태로 변경한다.")
    void givenStartCommand_whenStartStudyGroup_thenChangeToStart() {
        // given
        Long fakeStudyGroupId = 100L;
        Long hostId = HOST_ID;

        Participant host = Participant.host(hostId, fakeStudyGroupId);
        Set<Participant> participants = new HashSet<>();
        participants.add(host);

        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL, GroupState.RECRUITING);
        StudyGroup studyGroup = StudyGroup.of(fakeStudyGroupId, studyGroupInfo, participants);

        StartStudyGroupCommand cmd = new StartStudyGroupCommand(fakeStudyGroupId,
                hostId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));


        // when
        studyGroupLifecycleService.startStudyGroup(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupCommandRepository, times(1)).findById(fakeStudyGroupId);
        verify(studyGroupCommandRepository, times(1)).update(studyGroupCaptor.capture());

        StudyGroup studyGroupCaptorValue = studyGroupCaptor.getValue();
        assertThat(studyGroupCaptorValue.getInfoState()).isEqualTo(GroupState.START);
    }

    @Test
    @DisplayName("존재하지 않는 스터디 시작을 시도하면 예외를 발생시킨다.")
    void givenNotExistStudyGroup_whenStartStudyGroup_thenThrowException() {
        // given
        Long notExistStudyGroupId = 404L;
        Long hostId = HOST_ID;
        StartStudyGroupCommand cmd = new StartStudyGroupCommand(hostId,
                notExistStudyGroupId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.startStudyGroup(cmd))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 스터디 그룹");

        verify(studyGroupCommandRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupCommandRepository, never()).update(any(StudyGroup.class));
    }

    @Test
    @DisplayName("방장이 아닌 사용자가 스터디 시작을 시도하면 예외를 발생시킨다.")
    void givenNotHostUser_whenStartStudyGroup_thenThrowException() {
        // given
        Long notHostUserId = 404L;
        Long hostId = HOST_ID;
        Long fakeStudyGroupId = 100L;

        Participant host = Participant.host(hostId, fakeStudyGroupId);
        Participant member = Participant.of(notHostUserId, fakeStudyGroupId, ParticipantStatus.APPROVED, ParticipantRole.MEMBER);
        Set<Participant> participants = new HashSet<>();
        participants.add(host);
        participants.add(member);

        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL, GroupState.RECRUITING);
        StudyGroup studyGroup = StudyGroup.of(fakeStudyGroupId, studyGroupInfo, participants);

        StartStudyGroupCommand cmd = new StartStudyGroupCommand(fakeStudyGroupId, notHostUserId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.startStudyGroup(cmd))
                .isInstanceOf(NoHostAuthorityException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupCommandRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupCommandRepository, never()).update(any(StudyGroup.class));
    }

    @Test
    @DisplayName("스터디 그룹 종료 명령이 주어지면 스터디 그룹의 상태를 '종료됨' 상태로 변경한다.")
    void givenCloseCommand_whenCloseStudyGroup_thenChangeToClose() {
        // given
        Long fakeStudyGroupId = 100L;
        Long hostId = HOST_ID;

        Participant host = Participant.host(hostId, fakeStudyGroupId);
        Set<Participant> participants = new HashSet<>();
        participants.add(host);

        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL, GroupState.START);
        StudyGroup studyGroup = StudyGroup.of(fakeStudyGroupId, studyGroupInfo, participants);

        given(studyGroupCommandRepository.findById(fakeStudyGroupId)).willReturn(Optional.of(studyGroup));

        CloseStudyGroupCommand cmd = new CloseStudyGroupCommand(fakeStudyGroupId,
                hostId);

        // when
        studyGroupLifecycleService.closeStudyGroup(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupCommandRepository, times(1)).findById(fakeStudyGroupId);
        verify(studyGroupCommandRepository, times(1)).update(studyGroupCaptor.capture());

        StudyGroup studyGroupCaptorValue = studyGroupCaptor.getValue();
        assertThat(studyGroupCaptorValue.getInfoState()).isEqualTo(GroupState.CLOSE);
    }

    @Test
    @DisplayName("존재하지 않는 스터디 종료를 시도하면 예외를 발생시킨다.")
    void givenNotExistStudyGroup_whenCloseStudyGroup_thenThrowException() {
        // given
        Long notExistStudyGroupId = 404L;
        Long hostId = HOST_ID;
        CloseStudyGroupCommand cmd = new CloseStudyGroupCommand(hostId,
                notExistStudyGroupId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.closeStudyGroup(cmd))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 스터디 그룹");

        verify(studyGroupCommandRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupCommandRepository, never()).update(any(StudyGroup.class));
    }

    @Test
    @DisplayName("방장이 아닌 사용자가 스터디 종료를 시도하면 예외를 발생시킨다.")
    void givenNotHostUser_whenCloseStudyGroup_thenThrowException() {
        // given
        Long notHostUserId = 404L;
        Long hostId = HOST_ID;
        Long fakeStudyGroupId = 100L;

        Participant host = Participant.host(hostId, fakeStudyGroupId);
        Participant participant = Participant.of(notHostUserId, fakeStudyGroupId,
                ParticipantStatus.APPROVED, ParticipantRole.MEMBER);
        Set<Participant> participants = new HashSet<>();
        participants.add(host);
        participants.add(participant);

        StudyGroupInfo startGroupInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL, GroupState.START);
        StudyGroup studyGroup = StudyGroup.of(fakeStudyGroupId, startGroupInfo, participants);

        CloseStudyGroupCommand cmd = new CloseStudyGroupCommand(fakeStudyGroupId, notHostUserId);

        given(studyGroupCommandRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.closeStudyGroup(cmd))
                .isInstanceOf(NoHostAuthorityException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupCommandRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupCommandRepository, never()).update(studyGroup);
    }
}