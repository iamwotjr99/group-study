package com.jaeseok.groupStudy.studyGroup.domain;

import java.util.Optional;

public interface StudyGroupRepository {
    void save(StudyGroup studyGroup);
    Optional<StudyGroup> findById(Long id);
}
