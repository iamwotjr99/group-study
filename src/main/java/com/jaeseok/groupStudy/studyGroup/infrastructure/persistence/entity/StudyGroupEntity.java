package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.common.BaseTimeEntity;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Entity
@Table(name = "study_group")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyGroupEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private StudyGroupInfoEntity infoEntity;

    @OneToMany(mappedBy = "studyGroupEntity", cascade = CascadeType.ALL)
    private Set<ParticipantEntity> participantEntitySet = new HashSet<>();

    protected StudyGroupEntity() {}

    protected StudyGroupEntity(Long id, StudyGroupInfoEntity studyGroupInfoEntity) {
        this.id = id;
        this.infoEntity = studyGroupInfoEntity;
    }

    // 입력 받은 도메인으로부터 Entity 생성
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

    // 입력 받은 도메인으로부터 Entity 업데이트
    // 그냥 fromDomain로 변경된 StudyGroupEntity를 반환해서 StudyGroupEntity를 save()한다하더라도
    // 내부에서는 새 Participant를 리턴하기 때문에 JPA의 더티체킹은 변경으로 보지않고 추가로 본다.
    // 따라서 같은 값을 insert 하게 됨, 해당 메서드는 새로운걸 리턴하는게 아닌 엔티티 상태를 변경함
    public void updateFromDomain(StudyGroup studyGroup) {
        // infoEntity 업데이트
        this.infoEntity.updateFromDomain(studyGroup.getStudyGroupInfo());

        // participant 컬렉션 업데이트 (참여자 상태 동기화)
        this.syncParticipantStatus(studyGroup.getParticipantSet());
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

    private void syncParticipantStatus(Set<Participant> updatedStatusParticipants) {
        // 변경된 참여자 리스트를 순회
        for (Participant updatedParticipant : updatedStatusParticipants) {

            // 변경된 참여자가 기존의 참여자인지 탐색
            Optional<ParticipantEntity> existingOptionalEntity = this.participantEntitySet.stream()
                    .filter(pEntity -> pEntity.getUserId().equals(updatedParticipant.userId()))
                    .findFirst();

            // 기존의 참여자라면 상태만 업데이트
            if (existingOptionalEntity.isPresent()) {
                ParticipantEntity existingEntity = existingOptionalEntity.get();
                existingEntity.updateStatus(updatedParticipant.status());
            } else {
                // 기존의 참여자가 아니라 완전히 새로운 참여자라면
                // 새로운 엔티티 생성
                ParticipantEntity newEntity = ParticipantEntity.fromDomain(
                        updatedParticipant, this);
                this.participantEntitySet.add(newEntity);
            }
        }
    }
}
