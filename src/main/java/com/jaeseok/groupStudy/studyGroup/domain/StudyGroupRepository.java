package com.jaeseok.groupStudy.studyGroup.domain;

import java.util.Optional;

public interface StudyGroupRepository {
    StudyGroup save(StudyGroup studyGroup);
    Optional<StudyGroup> findById(Long id);
    StudyGroup update(StudyGroup studyGroup);
}
