package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Entity
@Table(name = "study_group")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private StudyGroupInfoEntity infoEntity;

    protected StudyGroupEntity() {}

    public static StudyGroupEntity fromDomain(StudyGroup studyGroup) {
        return new StudyGroupEntity(
                studyGroup.getId(),
                StudyGroupInfoEntity.fromDomain(studyGroup.getStudyGroupInfo())
        );
    }

    // 도메인 변환 (참여자 리스트를 외부에서 주입받음)
    public StudyGroup toDomain(Set<Participant> participants) {
        return StudyGroup.of(this.id, this.infoEntity.toDomain(), Collections.unmodifiableSet(participants));
    }
}
