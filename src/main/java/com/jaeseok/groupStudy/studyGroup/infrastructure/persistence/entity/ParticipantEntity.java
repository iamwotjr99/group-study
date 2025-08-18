package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.studyGroup.domain.participant.Participant;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantRole;
import com.jaeseok.groupStudy.studyGroup.domain.participant.ParticipantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

    @Column(name = "study_group_id")
    private Long studyGroupId;

    @Enumerated(value = EnumType.STRING)
    private ParticipantStatus status;

    @Enumerated(value = EnumType.STRING)
    private ParticipantRole role;

    protected ParticipantEntity() {}

    public static ParticipantEntity fromDomain(Participant participant) {
        // id = null 인 이유는 fromDomain 메서드가 사용될 떄는 participant가 신규 저장될 때 이므로 이때는 JPA가 자동 생성 해준다.
        return new ParticipantEntity(null,
                participant.userId(),
                participant.studyGroupId(),
                participant.status(),
                participant.role());
    }

    public Participant toDomain() {
        return Participant.of(this.userId, this.studyGroupId, this.status, this.role);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParticipantEntity that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(studyGroupId,
                that.studyGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, studyGroupId);
    }
}
