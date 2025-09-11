package com.jaeseok.groupStudy.studyGroup.application.command;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApproveStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.KickStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.RejectStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.StudyGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyGroupHostServiceImpl implements StudyGroupHostService {

    private final StudyGroupRepository studyGroupRepository;

    @Transactional
    @Override
    public void approveApplication(ApproveStudyGroupCommand cmd) {
        StudyGroupEntity studyGroupEntity = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹 입니다."));

        StudyGroup studyGroup = studyGroupEntity.toDomain();

        studyGroup.approveParticipant(cmd.hostId(), cmd.applicantId());

        studyGroupEntity.updateFromDomain(studyGroup);
    }

    @Transactional
    @Override
    public void rejectApplication(RejectStudyGroupCommand cmd) {
        StudyGroupEntity studyGroupEntity = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹 입니다."));

        StudyGroup studyGroup = studyGroupEntity.toDomain();

        studyGroup.rejectParticipant(cmd.hostId(), cmd.applicantId());

        studyGroupEntity.updateFromDomain(studyGroup);
    }

    @Transactional
    @Override
    public void kickParticipation(KickStudyGroupCommand cmd) {
        StudyGroupEntity studyGroupEntity = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹 입니다."));

        StudyGroup studyGroup = studyGroupEntity.toDomain();

        studyGroup.kickParticipant(cmd.hostId(), cmd.participantId());

        studyGroupEntity.updateFromDomain(studyGroup);
    }
}
