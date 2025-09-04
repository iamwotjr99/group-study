package com.jaeseok.groupStudy.studyGroup.domain;

import java.util.List;
import java.util.Optional;

public interface StudyGroupRepository {
    StudyGroup save(StudyGroup studyGroup);
    Optional<StudyGroup> findById(Long id);
    List<StudyGroup> findByRecruiting();
    StudyGroup update(StudyGroup studyGroup);
}
