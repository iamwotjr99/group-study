package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.entity;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.vo.StudyGroupInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Embeddable
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class StudyGroupInfoEntity {
    @Column(name = "title")
    private String title;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy")
    private RecruitingPolicy policy;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private GroupState state;

    protected StudyGroupInfoEntity() {}

    public static StudyGroupInfoEntity fromDomain(StudyGroupInfo info) {
        return new StudyGroupInfoEntity(
                info.getTitle(),
                info.getCapacity(),
                info.getDeadline(),
                info.getPolicy(),
                info.getState()
        );
    }

    public StudyGroupInfo toDomain() {
        return StudyGroupInfo.of(this.title, this.capacity, this.deadline, this.policy, this.state);
    }

}
