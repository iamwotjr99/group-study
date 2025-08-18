package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Entity
@Table(
        name = "participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "study_group_id"})
)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroupEntity studyGroupEntity;

    @Enumerated(value = EnumType.STRING)
    private ParticipantStatus status;

    @Enumerated(value = EnumType.STRING)
    private ParticipantRole role;

    protected ParticipantEntity() {}

    public static ParticipantEntity fromDomain(Participant participant, StudyGroupEntity studyGroupEntity) {
        return ParticipantEntity.builder()
                .id(null)
                .userId(participant.userId())
                .studyGroupEntity(studyGroupEntity)
                .status(participant.status())
                .role(participant.role())
                .build();
    }

    public Participant toDomain() {
        return Participant.of(this.userId, this.studyGroupEntity.getId(), this.status, this.role);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParticipantEntity that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(this.studyGroupEntity.getId(),
                that.studyGroupEntity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, studyGroupEntity.getId());
    }
}
