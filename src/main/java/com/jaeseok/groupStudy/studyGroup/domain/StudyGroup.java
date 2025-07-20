package com.jaeseok.groupStudy.studyGroup.domain;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;

/**
 * 역할 - 스터디 그룹 방 자체
 * 책임 - 모집 방식 설정/변경, 그룹 상태 설정/변경, 참여자 승인/거절/강퇴, 참여자 정원 관리
 * 협력 - 방장, 참여자
 */
@RequiredArgsConstructor
public class StudyGroup {
    private final Long id;
    private final Long hostId;
    private final String title;
    private final int capacity;
    private final LocalDateTime deadline;
    private final RecruitingPolicy policy;
    private final GroupState state;

    public StudyGroup autoPolicy() {
        return withPolicy(RecruitingPolicy.AUTO);
    }

    public StudyGroup approvePolicy() {
        return withPolicy(RecruitingPolicy.APPROVAL);
    }

    public StudyGroup recruit() {
        return withState(GroupState.RECRUITING);
    }

    public StudyGroup close() {
        return withState(GroupState.CLOSE);
    }

    public StudyGroup start() {
        return withState(GroupState.START);
    }

    private StudyGroup withPolicy(RecruitingPolicy policy) {
        return new StudyGroup(this.id, this.hostId, this.title, this.capacity, this.deadline, policy, this.state);
    }

    private StudyGroup withState(GroupState state) {
        return new StudyGroup(this.id, this.hostId, this.title, this.capacity, this.deadline, this.policy, state);
    }
}
