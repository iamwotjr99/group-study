package com.jaeseok.groupStudy.studyGroup.domain;

import java.util.Optional;

public interface StudyGroupCommandRepository {
    StudyGroup save(StudyGroup studyGroup);
    Optional<StudyGroup> findById(Long id);
    StudyGroup update(StudyGroup studyGroup);
    boolean existsById(Long id);
    void deleteById(Long id);
}
