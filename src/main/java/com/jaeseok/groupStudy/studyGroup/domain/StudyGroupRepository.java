package com.jaeseok.groupStudy.studyGroup.domain;

import java.util.List;
import java.util.Optional;

public interface StudyGroupRepository {
    StudyGroup save(StudyGroup studyGroup);
    Optional<StudyGroup> findById(Long id);
    List<StudyGroup> findByState(GroupState state);
    StudyGroup update(StudyGroup studyGroup);
}
