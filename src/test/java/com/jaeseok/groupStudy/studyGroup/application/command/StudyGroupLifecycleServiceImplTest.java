package com.jaeseok.groupStudy.studyGroup.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.CloseStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StartStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupRepository;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import jakarta.persistence.EntityExistsException;
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
    StudyGroupRepository studyGroupRepository;

    final Long HOST_ID = 1L;

    @Test
    @DisplayName("스터디 그룹 생성 명령이 주어지면 스터디 그룹을 생성한다.")
    void givenCreateCommand_whenCreateStudyGroup_thenReturnStudyGroup() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
        CreateStudyGroupCommand cmd = new CreateStudyGroupCommand(HOST_ID,
                studyGroupInfo);

        Long fakeStudyGroupId = 100L;
        StudyGroup willCreatedStudyGroup = StudyGroup.of(fakeStudyGroupId, cmd.info(), Collections.EMPTY_SET);
        given(studyGroupRepository.save(any(StudyGroup.class))).willReturn(willCreatedStudyGroup);

        // when
        CreateStudyGroupInfo createStudyGroupInfo = studyGroupLifecycleService.createStudyGroup(cmd);

        // then
        assertThat(createStudyGroupInfo.studyGroupId()).isNotNull();
        assertThat(createStudyGroupInfo.studyGroupId()).isEqualTo(willCreatedStudyGroup.getId());

        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository, times(1)).save(studyGroupCaptor.capture());

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
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
        StudyGroup studyGroup = StudyGroup.of(fakeStudyGroupId, studyGroupInfo, participants);

        StartStudyGroupCommand cmd = new StartStudyGroupCommand(fakeStudyGroupId,
                hostId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));


        // when
        studyGroupLifecycleService.startStudyGroup(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository, times(1)).findById(fakeStudyGroupId);
        verify(studyGroupRepository, times(1)).update(studyGroupCaptor.capture());

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

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.startStudyGroup(cmd))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 스터디 그룹");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupRepository, never()).update(any(StudyGroup.class));
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
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
        StudyGroup studyGroup = StudyGroup.of(fakeStudyGroupId, studyGroupInfo, participants);

        StartStudyGroupCommand cmd = new StartStudyGroupCommand(fakeStudyGroupId, notHostUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.startStudyGroup(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupRepository, never()).update(any(StudyGroup.class));
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

        StudyGroupInfo recruitingInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
        StudyGroupInfo studyGroupInfo = recruitingInfo.start();
        StudyGroup studyGroup = StudyGroup.of(fakeStudyGroupId, studyGroupInfo, participants);

        given(studyGroupRepository.findById(fakeStudyGroupId)).willReturn(Optional.of(studyGroup));

        CloseStudyGroupCommand cmd = new CloseStudyGroupCommand(fakeStudyGroupId,
                hostId);

        // when
        studyGroupLifecycleService.closeStudyGroup(cmd);

        // then
        ArgumentCaptor<StudyGroup> studyGroupCaptor = ArgumentCaptor.forClass(StudyGroup.class);
        verify(studyGroupRepository, times(1)).findById(fakeStudyGroupId);
        verify(studyGroupRepository, times(1)).update(studyGroupCaptor.capture());

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

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.closeStudyGroup(cmd))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("존재하지 않는 스터디 그룹");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupRepository, never()).update(any(StudyGroup.class));
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

        StudyGroupInfo recruitingInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
        StudyGroupInfo startGroupInfo = recruitingInfo.start();
        StudyGroup studyGroup = StudyGroup.of(fakeStudyGroupId, startGroupInfo, participants);

        CloseStudyGroupCommand cmd = new CloseStudyGroupCommand(fakeStudyGroupId, notHostUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroup));

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.closeStudyGroup(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verify(studyGroupRepository, never()).update(studyGroup);
    }
}