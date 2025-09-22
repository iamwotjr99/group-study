package com.jaeseok.groupStudy.studyGroup.infrastructure.persistence.repository.query.dto;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import java.time.LocalDateTime;
import java.util.Set;

public record StudyGroupDetailDto(
        Long studyGroupId,
        Long hostId,
        String title,
        Integer curMemberCount,
        Integer capacity,
        LocalDateTime deadline,
        RecruitingPolicy policy,
        GroupState state,
        Set<ParticipantDto> participants
) {

    // QueryDSL을 위한 추가 생성자
    public StudyGroupDetailDto(
            Long studyGroupId,
            Long hostId,
            String title,
            Integer curMemberCount,
            Integer capacity,
            LocalDateTime deadline,
            RecruitingPolicy policy,
            GroupState state) {
        this(studyGroupId, hostId, title, curMemberCount, capacity, deadline, policy, state, Set.of());
    }

    public StudyGroupDetailDto withParticipants(Set<ParticipantDto> participants) {
        return new StudyGroupDetailDto(
                this.studyGroupId,
                this.hostId,
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
