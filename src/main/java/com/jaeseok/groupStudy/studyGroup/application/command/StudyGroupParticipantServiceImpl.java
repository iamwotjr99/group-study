package com.jaeseok.groupStudy.studyGroup.application.command;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.ApplyStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CancelStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.LeaveStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyGroupParticipantServiceImpl implements StudyGroupParticipantService {

    private final StudyGroupRepository studyGroupRepository;

    @Transactional
    @Override
    public void applyForStudyGroup(ApplyStudyGroupCommand cmd) {
        StudyGroup studyGroup = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹 입니다."));

        studyGroup.apply(cmd.applicantId());

        studyGroupRepository.update(studyGroup);
    }

    @Transactional
    @Override
    public void cancelApplication(CancelStudyGroupCommand cmd) {
        StudyGroup studyGroup = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹 입니다."));

        studyGroup.participantApplyCancel(cmd.applicantId());

        studyGroupRepository.update(studyGroup);
    }

    @Transactional
    @Override
    public void leaveStudyGroup(LeaveStudyGroupCommand cmd) {
        StudyGroup studyGroup = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹 입니다."));

        studyGroup.participantLeave(cmd.participantId());

        studyGroupRepository.update(studyGroup);
    }
}
