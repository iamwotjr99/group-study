package com.jaeseok.groupStudy.studyGroup.domain.vo;

import com.jaeseok.groupStudy.studyGroup.domain.GroupState;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

@Getter
public class StudyGroupInfo {
    private final String title;
    private final Integer capacity;
    private final LocalDateTime deadline;
    private final RecruitingPolicy policy;
    private final GroupState state;

    private StudyGroupInfo(String title, Integer capacity, LocalDateTime deadline, RecruitingPolicy policy, GroupState state) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (title.length() > 20) {
            throw new IllegalArgumentException("제목은 20자 이하로 설정해주세요.");
        }
        if (capacity == null || capacity < 2) {
            throw new IllegalArgumentException("방 인원은 2명 이상입니다.");
        }

        this.title = title;
        this.capacity = capacity;
        this.deadline = deadline;
        this.policy = policy;
        this.state = state;
    }

    public static StudyGroupInfo of(String title, Integer capacity, LocalDateTime deadline, RecruitingPolicy policy, GroupState state) {
        return new StudyGroupInfo(title, capacity, deadline, policy, state);
    }

    // 기본값 스터디 정보 생성(가입 방법: 승인제, 스터디 모집 상태: 모집중)
    public static StudyGroupInfo defaultInfo(String title, Integer capacity, LocalDateTime deadline) {
        return new StudyGroupInfo(title, capacity, deadline, RecruitingPolicy.APPROVAL, GroupState.RECRUITING);
    }

    // 자동 승인제
    public StudyGroupInfo autoPolicy() {
        return withPolicy(RecruitingPolicy.AUTO);
    }

    // 방장 승인제
    public StudyGroupInfo approvePolicy() {
        return withPolicy(RecruitingPolicy.APPROVAL);
    }

    // 모집중
    public StudyGroupInfo recruit() {
        return withState(GroupState.RECRUITING);
    }

    // 스터디 종료
    public StudyGroupInfo close() {
        if (this.state != GroupState.START) {
            throw new IllegalStateException("진행중인 스터디만 종료할 수 있습니다.");
        }
        return withState(GroupState.CLOSE);
    }

    // 스터디 진행중
    public StudyGroupInfo start() {
        if (this.state != GroupState.RECRUITING) {
            throw new IllegalStateException("모집중인 스터디만 시작할 수 있습니다.");
        }
        return withState(GroupState.START);
    }

    private StudyGroupInfo withPolicy(RecruitingPolicy policy) {
        return new StudyGroupInfo(this.title, this.capacity, this.deadline, policy, this.state);
    }

    private StudyGroupInfo withState(GroupState state) {
        return new StudyGroupInfo(this.title, this.capacity, this.deadline, this.policy, state);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StudyGroupInfo that)) {
            return false;
        }
        return capacity == that.capacity && Objects.equals(title, that.title)
                && Objects.equals(deadline, that.deadline) && policy == that.policy
                && state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, capacity, deadline, policy, state);
    }
}
