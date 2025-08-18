package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
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

    @OneToMany(mappedBy = "studyGroupEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ParticipantEntity> participantEntitySet = new HashSet<>();

    protected StudyGroupEntity() {}

    protected StudyGroupEntity(Long id, StudyGroupInfoEntity studyGroupInfoEntity) {
        this.id = id;
        this.infoEntity = studyGroupInfoEntity;
    }

    public static StudyGroupEntity fromDomain(StudyGroup studyGroup) {
        StudyGroupEntity studyGroupEntity = new StudyGroupEntity(
                studyGroup.getId(),
                StudyGroupInfoEntity.fromDomain(studyGroup.getStudyGroupInfo())
        );

        Set<ParticipantEntity> participantEntitySet = studyGroup.getParticipantSet().stream()
                .map(p -> ParticipantEntity.fromDomain(p, studyGroupEntity))
                .collect(Collectors.toSet());
        studyGroupEntity.setParticipantEntitySet(participantEntitySet);

        return studyGroupEntity;
    }

    // 도메인 변환 (참여자 리스트를 외부에서 주입받음)
    public StudyGroup toDomain() {
        Set<Participant> participantSet = participantEntitySet.stream()
                .map(ParticipantEntity::toDomain)
                .collect(Collectors.toSet());

        return StudyGroup.of(this.id, this.infoEntity.toDomain(), participantSet);
    }

    private void setParticipantEntitySet(Set<ParticipantEntity> participantEntitySet) {
        this.participantEntitySet = participantEntitySet;
    }
}
