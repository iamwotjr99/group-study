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
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.ParticipantEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupInfoEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.StudyGroupRepository;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
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
@DisplayName("StudyGroupLifecycle 테스트")
class StudyGroupLifecycleServiceImplTest {

    @InjectMocks
    StudyGroupLifecycleServiceImpl studyGroupLifecycleService;

    @Mock
    StudyGroupRepository studyGroupRepository;

    final Long HOST_ID = 1L;
    final Long USER_ID = 2L;
    final Long STUDY_GROUP_ID = 100L;
    StudyGroupEntity studyGroupEntity;

    @BeforeEach
    void setUp() {
        StudyGroupInfoEntity infoEntity = StudyGroupInfoEntity.builder()
                .title("테스트 스터디 그룹 001")
                .capacity(3)
                .deadline(LocalDateTime.now().plusDays(1))
                .policy(RecruitingPolicy.APPROVAL)
                .state(GroupState.RECRUITING)
                .build();
        this.studyGroupEntity = StudyGroupEntity.builder()
                .id(STUDY_GROUP_ID)
                .infoEntity(infoEntity)
                .participantEntitySet(new HashSet<>())
                .build();

        ParticipantEntity hostEntity = ParticipantEntity.builder()
                .userId(HOST_ID)
                .studyGroupEntity(studyGroupEntity)
                .status(ParticipantStatus.APPROVED)
                .role(ParticipantRole.HOST)
                .build();

        ParticipantEntity userEntity = ParticipantEntity.builder()
                .userId(USER_ID)
                .studyGroupEntity(studyGroupEntity)
                .status(ParticipantStatus.APPROVED)
                .role(ParticipantRole.MEMBER)
                .build();

        this.studyGroupEntity.getParticipantEntitySet().add(hostEntity);
        this.studyGroupEntity.getParticipantEntitySet().add(userEntity);
    }

    @Test
    @DisplayName("스터디 그룹 생성 명령이 주어지면 스터디 그룹을 생성한다.")
    void givenCreateCommand_whenCreateStudyGroup_thenReturnStudyGroup() {
        // given
        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL, GroupState.RECRUITING);

        CreateStudyGroupCommand cmd = new CreateStudyGroupCommand(HOST_ID, studyGroupInfo);

        Long fakeStudyGroupId = 100L;
        StudyGroupEntity willCreateStudyGroupEntity = StudyGroupEntity.builder()
                .id(fakeStudyGroupId)
                .infoEntity(StudyGroupInfoEntity.fromDomain(studyGroupInfo))
                .participantEntitySet(new HashSet<>())
                .build();
        given(studyGroupRepository.save(any(StudyGroupEntity.class))).willReturn(willCreateStudyGroupEntity);

        // when
        CreateStudyGroupInfo createStudyGroupInfo = studyGroupLifecycleService.createStudyGroup(cmd);

        // then
        assertThat(createStudyGroupInfo.studyGroupId()).isNotNull();
        assertThat(createStudyGroupInfo.studyGroupId()).isEqualTo(willCreateStudyGroupEntity.getId());

        ArgumentCaptor<StudyGroupEntity> studyGroupEntityCaptor = ArgumentCaptor.forClass(StudyGroupEntity.class);
        verify(studyGroupRepository, times(1)).save(studyGroupEntityCaptor.capture());

        StudyGroupEntity capturedStudyGroupEntity = studyGroupEntityCaptor.getValue();
        assertThat(capturedStudyGroupEntity.getInfoEntity()).isEqualTo(willCreateStudyGroupEntity.getInfoEntity());
        assertThat(capturedStudyGroupEntity.getParticipantEntitySet())
                .hasSize(1)
                .extracting(ParticipantEntity::getUserId, ParticipantEntity::getRole)
                .containsExactly(tuple(HOST_ID, ParticipantRole.HOST));
    }

    @Test
    @DisplayName("스터디 그룹 시작 명령이 주어지면 스터디 그룹의 상태를 '진행중' 상태로 변경한다.")
    void givenStartCommand_whenStartStudyGroup_thenChangeToStart() {
        // given
        Long fakeStudyGroupId = 100L;
        Long hostId = HOST_ID;

        StartStudyGroupCommand cmd = new StartStudyGroupCommand(fakeStudyGroupId, hostId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));


        // when
        studyGroupLifecycleService.startStudyGroup(cmd);

        // then
        verify(studyGroupRepository, times(1)).findById(fakeStudyGroupId);

        assertThat(studyGroupEntity.getInfoEntity().getState()).isEqualTo(GroupState.START);
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
                .hasMessageContaining("존재하지 않는 스터디그룹 입니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }

    @Test
    @DisplayName("방장이 아닌 사용자가 스터디 시작을 시도하면 예외를 발생시킨다.")
    void givenNotHostUser_whenStartStudyGroup_thenThrowException() {
        // given
        Long notHostUserId = USER_ID;
        Long fakeStudyGroupId = 100L;

        StartStudyGroupCommand cmd = new StartStudyGroupCommand(fakeStudyGroupId, notHostUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.startStudyGroup(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }

    @Test
    @DisplayName("스터디 그룹 종료 명령이 주어지면 스터디 그룹의 상태를 '종료됨' 상태로 변경한다.")
    void givenCloseCommand_whenCloseStudyGroup_thenChangeToClose() {
        // given
        Long fakeStudyGroupId = 100L;
        Long hostId = HOST_ID;

        CloseStudyGroupCommand cmd = new CloseStudyGroupCommand(fakeStudyGroupId, hostId);

        StudyGroupInfo studyGroupInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL, GroupState.RECRUITING);

        StudyGroupInfo startInfo = studyGroupInfo.start();

        StudyGroupEntity startStatusStudyGroupEntity = StudyGroupEntity.builder()
                .id(fakeStudyGroupId)
                .infoEntity(StudyGroupInfoEntity.fromDomain(startInfo))
                .participantEntitySet(studyGroupEntity.getParticipantEntitySet())
                .build();


        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(startStatusStudyGroupEntity));

        // when
        studyGroupLifecycleService.closeStudyGroup(cmd);

        // then
        verify(studyGroupRepository, times(1)).findById(fakeStudyGroupId);

        assertThat(startStatusStudyGroupEntity.getInfoEntity().getState()).isEqualTo(GroupState.CLOSE);
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
                .hasMessageContaining("존재하지 않는 스터디그룹 입니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }

    @Test
    @DisplayName("방장이 아닌 사용자가 스터디 종료를 시도하면 예외를 발생시킨다.")
    void givenNotHostUser_whenCloseStudyGroup_thenThrowException() {
        // given
        Long notHostUserId = USER_ID;
        Long fakeStudyGroupId = 100L;


        StudyGroupInfo recruitingInfo = StudyGroupInfo.of("테스트 스터디 그룹 001", 3,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL, GroupState.RECRUITING);
        StudyGroupInfo startGroupInfo = recruitingInfo.start();

        StudyGroupEntity startStatusStudyGroupEntity = StudyGroupEntity.builder()
                .id(fakeStudyGroupId)
                .infoEntity(StudyGroupInfoEntity.fromDomain(startGroupInfo))
                .participantEntitySet(studyGroupEntity.getParticipantEntitySet())
                .build();

        CloseStudyGroupCommand cmd = new CloseStudyGroupCommand(fakeStudyGroupId, notHostUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(startStatusStudyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupLifecycleService.closeStudyGroup(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }
}