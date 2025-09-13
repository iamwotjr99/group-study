package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import java.time.LocalDateTime;
import java.util.Set;

public record StudyGroupDetailDto(
        Long studyGroupId,
        String title,
        Integer curMemberCount,
        Integer capacity,
        LocalDateTime deadline,
        RecruitingPolicy policy,
        GroupState state,
        Set<ParticipantDto> participants
) {

    public StudyGroupDetailDto withParticipants(Set<ParticipantDto> participants) {
        return new StudyGroupDetailDto(
                this.studyGroupId,
                this.title,
                this.curMemberCount,
                this.capacity,
                this.deadline,
                this.policy,
                this.state,
                participants
        );
    }
}
