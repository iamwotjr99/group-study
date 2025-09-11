package com.jaeseok.groupStudy.studyGroup.application.command;

import com.jaeseok.groupStudy.studyGroup.application.command.dto.CloseStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.CreateStudyGroupInfo;
import com.jaeseok.groupStudy.studyGroup.application.command.dto.StartStudyGroupCommand;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity.StudyGroupEntity;
import com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.StudyGroupRepository;
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
    public CreateStudyGroupInfo createStudyGroup(CreateStudyGroupCommand cmd) {
        StudyGroup studyGroup = StudyGroup.createWithHost(cmd.hostId(), cmd.info());
        StudyGroupEntity studyGroupEntity = StudyGroupEntity.fromDomain(studyGroup);

        StudyGroupEntity saved = studyGroupRepository.save(studyGroupEntity);
        return new CreateStudyGroupInfo(saved.getId());
    }

    // 스터디 그룹을 시작: 모집중 -> 진행중
    @Transactional
    @Override
    public void startStudyGroup(StartStudyGroupCommand cmd) {
        StudyGroupEntity studyGroupEntity = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹 입니다."));

        StudyGroup studyGroup = studyGroupEntity.toDomain();

        studyGroup.start(cmd.hostId());

        studyGroupEntity.updateFromDomain(studyGroup);
    }

    // 스터디 그룹을 종료: 진행중 -> 종료
    @Transactional
    @Override
    public void closeStudyGroup(CloseStudyGroupCommand cmd) {
        StudyGroupEntity studyGroupEntity = studyGroupRepository.findById(cmd.studyGroupId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 스터디그룹 입니다."));

        StudyGroup studyGroup = studyGroupEntity.toDomain();

        studyGroup.close(cmd.hostId());

        studyGroupEntity.updateFromDomain(studyGroup);
    }
}
