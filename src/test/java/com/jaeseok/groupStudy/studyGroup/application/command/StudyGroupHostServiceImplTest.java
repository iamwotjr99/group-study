package com.jaeseok.groupStudy.studyGroup.application.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApproveStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.KickStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.RejectStudyGroupCommand;
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
    @DisplayName("방장은 참여 신청자를 승인할 수 있다.")
    void givenApproveCommand_whenApproveApplication_thenChangeToApproved() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long hostId = HOST_ID;

        Long applicantUserId = 3L;
        applicantParticipantBuild(applicantUserId);

        ApproveStudyGroupCommand cmd = new ApproveStudyGroupCommand(
                studyGroupId, hostId, applicantUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when
        studyGroupHostService.approveApplication(cmd);

        // then
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());

        assertParticipantStatus(applicantUserId, ParticipantStatus.APPROVED, 3);
    }

    @Test
    @DisplayName("방장이 아닌 유저가 승인을 시도하면 예외를 던진다.")
    void givenNotHostUser_whenApproveApplication_thenThrowException() {
        // given
        Long notHostUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long applicantUserId = 3L;

        applicantParticipantBuild(applicantUserId);


        ApproveStudyGroupCommand cmd = new ApproveStudyGroupCommand(studyGroupId, notHostUserId,
                applicantUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.approveApplication(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);

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

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.approveApplication(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대기중인 유저가 아닙니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);

    }

    @Test
    @DisplayName("방 인원이 가득찼을 때, 참여 신청자를 승인 시도하면 예외를 던진다.")
    void givenFullStudyGroup_whenApproveApplication_thenThrowException() {
        // given
        Long hostId = HOST_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long approvedId = 3L;
        ParticipantEntity approvedEntity = ParticipantEntity.builder()
                .userId(approvedId)
                .studyGroupEntity(studyGroupEntity)
                .status(ParticipantStatus.APPROVED)
                .role(ParticipantRole.MEMBER)
                .build();
        studyGroupEntity.getParticipantEntitySet().add(approvedEntity); // 방 인원 제한 3명으로 꽉차게 설정

        Long applicantId = 4L;
        applicantParticipantBuild(applicantId);

        ApproveStudyGroupCommand cmd = new ApproveStudyGroupCommand(studyGroupId, hostId, applicantId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.approveApplication(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가득 찼습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }

    @Test
    @DisplayName("방장은 참여 신청자를 거절할 수 있다.")
    void givenRejectCommand_whenRejectApplication_thenChangeToRejected() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long hostId = HOST_ID;

        Long applicantUserId = 3L;
        applicantParticipantBuild(applicantUserId);

        RejectStudyGroupCommand cmd = new RejectStudyGroupCommand(studyGroupId,
                hostId, applicantUserId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when
        studyGroupHostService.rejectApplication(cmd);

        // then
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());

        assertParticipantStatus(applicantUserId, ParticipantStatus.REJECTED, 3);
    }

    @Test
    @DisplayName("방장이 아닌 유저가 신청자를 거절하려고 하면 예외를 던진다.")
    void givenNotHostUser_whenRejectApplication_thenThrowException() {
        // given
        Long notHostUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long applicantId = 3L;
        applicantParticipantBuild(applicantId);

        RejectStudyGroupCommand cmd = new RejectStudyGroupCommand(studyGroupId, notHostUserId, applicantId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.rejectApplication(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);

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

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.rejectApplication(cmd))
                .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("대기중인 유저가 아닙니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);
    }

    @Test
    @DisplayName("방장은 스터디그룹 참여자를 강퇴할 수 있다.")
    void givenKickCommand_whenKickParticipation_thenChangeToKicked() {
        // given
        Long studyGroupId = STUDY_GROUP_ID;
        Long hostId = HOST_ID;

        Long approvedId = 3L;
        ParticipantEntity approvedEntity = ParticipantEntity.builder()
                .userId(approvedId)
                .studyGroupEntity(studyGroupEntity)
                .status(ParticipantStatus.APPROVED)
                .role(ParticipantRole.MEMBER)
                .build();
        studyGroupEntity.getParticipantEntitySet().add(approvedEntity);

        KickStudyGroupCommand cmd = new KickStudyGroupCommand(studyGroupId,
                hostId, approvedId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when
        studyGroupHostService.kickParticipation(cmd);

        // then
        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());

        assertParticipantStatus(approvedId, ParticipantStatus.KICKED, 3);
    }

    @Test
    @DisplayName("방장이 아닌 유저가 스터디그룹 참여자를 강퇴 시도를 하면 예외를 던진다.")
    void givenNotHostUser_whenKickParticipation_thenThrowException() {
        // given
        Long notHostUserId = USER_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long approvedId = 3L;
        ParticipantEntity approvedEntity = ParticipantEntity.builder()
                .userId(approvedId)
                .studyGroupEntity(studyGroupEntity)
                .status(ParticipantStatus.APPROVED)
                .role(ParticipantRole.MEMBER)
                .build();
        studyGroupEntity.getParticipantEntitySet().add(approvedEntity);


        KickStudyGroupCommand cmd = new KickStudyGroupCommand(studyGroupId,
                notHostUserId, approvedId);

        given(studyGroupRepository.findById(studyGroupId)).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.kickParticipation(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("방장 권한이 없습니다.");

        verify(studyGroupRepository, times(1)).findById(cmd.studyGroupId());
        verifyNoMoreInteractions(studyGroupRepository);

    }

    @Test
    @DisplayName("'승인됨'이 아닌 유저를 강퇴 시도를 하면 예외를 던진다.")
    void givenNotApprovedUser_whenKickParticipation_thenThrowException() {
        // given
        Long hostId = HOST_ID;
        Long studyGroupId = STUDY_GROUP_ID;

        Long applicantId = 3L;
        applicantParticipantBuild(applicantId);

        KickStudyGroupCommand cmd = new KickStudyGroupCommand(studyGroupId,
                hostId, applicantId);

        given(studyGroupRepository.findById(cmd.studyGroupId())).willReturn(Optional.of(studyGroupEntity));

        // when & then
        assertThatThrownBy(() -> studyGroupHostService.kickParticipation(cmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("참여중인 유저가 아닙니다.");

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