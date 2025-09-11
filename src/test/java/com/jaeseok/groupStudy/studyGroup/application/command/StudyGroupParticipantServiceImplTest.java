package com.jaeseok.groupStudy.studyGroup.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApplyStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CancelStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.LeaveStudyGroupCommand;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    @DisplayName("유저는 스터디 그룹에 참여 신청할 수 있다.")
    void givenApplyCommand_whenApplyForStudyGroup_thenChangeParticipantSetAndStatus() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long applicantId = 3L;

        ApplyStudyGroupCommand cmd = new ApplyStudyGroupCommand(studyGroupId, applicantId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when
        studyGroupParticipantService.applyForStudyGroup(cmd);

        // then
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());

        assertThat(studyGroupEntity.getParticipantEntitySet())
                .hasSize(3)
                .extracting(ParticipantEntity::getUserId, ParticipantEntity::getStatus)
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

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupParticipantService.applyForStudyGroup(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 신청중이거나 참여중인 스터디 그룹입니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }


    @Test
    @DisplayName("유저는 스터디 그룹에 참여 신청을 취소할 수 있다.")
    void givenCancelCommand_whenCancelApplication_thenChangeToCancel() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long applicantId = 3L;


        applicantParticipantBuild(applicantId);

        CancelStudyGroupCommand cmd = new CancelStudyGroupCommand(studyGroupId, applicantId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when
        studyGroupParticipantService.cancelApplication(cmd);

        // then
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());

        assertParticipantStatus(applicantId, ParticipantStatus.CANCELED, 3);
    }

    @Test
    @DisplayName("'대기중' 상태가 아닌 유저가 참여 신청을 취소하면 예외를 던진다.")
    void givenNotPendingUser_whenCancelApplication_theThrowException() {
        // given
        Long alreadyExistUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        CancelStudyGroupCommand cmd = new CancelStudyGroupCommand(studyGroupId,
                alreadyExistUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupParticipantService.cancelApplication(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대기중인 유저가 아닙니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }

    @Test
    @DisplayName("스터디 그룹 참여자는 스터디 그룹을 떠날 수 있다.")
    void givenLeaveCommand_whenLeaveStudyGroup_thenChangeToLeave() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long leaveId = USER_ID;

        LeaveStudyGroupCommand cmd = new LeaveStudyGroupCommand(studyGroupId, leaveId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when
        studyGroupParticipantService.leaveStudyGroup(cmd);

        // then
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());

        assertThat(studyGroupEntity.getParticipantEntitySet())
                .hasSize(2)
                .filteredOn(p -> p.getUserId().equals(leaveId))
                .first()
                .extracting(ParticipantEntity::getStatus)
                .isEqualTo(ParticipantStatus.LEAVE);
    }

    @Test
    @DisplayName("방장이 스터디 그룹 퇴장 시도를 하면 예외를 던진다.")
    void givenHost_whenLeaveStudyGroup_thenThrowReturn() {
        // given
        Long hostId = HOST_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        LeaveStudyGroupCommand cmd = new LeaveStudyGroupCommand(studyGroupId,
                hostId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupParticipantService.leaveStudyGroup(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장은 퇴장할 수 없습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }

    private void assertParticipantStatus(Long assertTargetId, ParticipantStatus status, int size) {
        assertThat(studyGroupEntity.getParticipantEntitySet())
                .hasSize(size)
                .extracting(ParticipantEntity::getUserId, ParticipantEntity::getStatus)
                .contains(
                        tuple(HOST_ID, ParticipantStatus.APPROVED),
                        tuple(USER_ID, ParticipantStatus.APPROVED),
                        tuple(assertTargetId, status));

        assertThat(studyGroupEntity.getParticipantEntitySet())
                .filteredOn(p -> p.getUserId().equals(assertTargetId))
                .first()
                .extracting(ParticipantEntity::getStatus)
                .isEqualTo(status);
    }

    private void applicantParticipantBuild(Long applicantId) {
        ParticipantEntity applicantEntity = ParticipantEntity.builder()
                .userId(applicantId)
                .studyGroupEntity(studyGroupEntity)
                .status(ParticipantStatus.PENDING)
                .role(ParticipantRole.MEMBER)
                .build();
        studyGroupEntity.getParticipantEntitySet().add(applicantEntity);
    }
}