package com.jaeseok.groupStudy.studyGroup.application.command;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupCloseCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupCreateCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StudyGroupStartCommand;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyGroupLifecycleServiceImpl implements StudyGroupLifecycleService {

    private final StudyGroupRepository studyGroupRepository;

    // 스터디 그룹을 생성
    @Override
    public Long createStudyGroup(StudyGroupCreateCommand cmd) {
        StudyGroup studyGroup = StudyGroup.createWithHost(cmd.hostId(), cmd.info());

        StudyGroup saved = studyGroupRepository.save(studyGroup);
        return saved.getId();
    }

    // 스터디 그룹을 시작: 모집중 -> 진행중
    @Transactional
    @Override
    public void startStudyGroup(StudyGroupStartCommand cmd) {
        StudyGroup studyGroup = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디 그룹입니다."));

        studyGroup.start(cmd.hostId());
        studyGroupRepository.update(studyGroup);
    }

    // 스터디 그룹을 종료: 진행중 -> 종료
    @Transactional
    @Override
    public void closeStudyGroup(StudyGroupCloseCommand cmd) {
        StudyGroup studyGroup = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디 그룹입니다."));

        studyGroup.close(cmd.hostId());
        studyGroupRepository.update(studyGroup);
    }
}
