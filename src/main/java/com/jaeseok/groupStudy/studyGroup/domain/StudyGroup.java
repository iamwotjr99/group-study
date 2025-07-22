package com.jaeseok.groupStudy.studyGroup.domain;

import com.jaeseok.groupStudy.participant.domain.Participant;
import com.jaeseok.groupStudy.participant.domain.ParticipantState;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 역할 - 스터디 그룹 방 자체
 * 책임 - 모집 방식 설정/변경, 그룹 상태 설정/변경, 참여자 승인/거절/강퇴, 참여자 정원 관리
 * 협력 - 방장, 참여자
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class StudyGroup {
    private final Long id;
    private final Long hostId;
    private final String title;
    private final int capacity;
    private Set<Participant> participantSet;
    private final LocalDateTime deadline;
    private final RecruitingPolicy policy;
    private final GroupState state;

    static public StudyGroup create(Long hostId, String title, int capacity, LocalDateTime deadline) {
        return new StudyGroup(
                null,
                hostId,
                title,
                capacity,
                new HashSet<>(),
                deadline,
                RecruitingPolicy.APPROVAL,
                GroupState.RECRUITING
        );
    }

    // 참여자 승인
    public Participant approveParticipant(Long hostId, Participant participant) {
        if (this.hostId != hostId) throw new IllegalArgumentException("해당 유저는 방장 권한이 없습니다.");
        if (participant.state() != ParticipantState.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");
        if (isPull()) throw new IllegalArgumentException("현재 방 인원이 가득 찼습니다.");

        participantSet.add(participant);
        return participant.approve();
    }

    // 참여자 거절
    public Participant rejectParticipant(Long hostId, Participant participant) {
        if (this.hostId != hostId) throw new IllegalArgumentException("해당 유저는 방장 권한이 없습니다.");
        if (participant.state() != ParticipantState.PENDING) throw new IllegalStateException("대기중인 유저가 아닙니다.");

        return participant.reject();
    }

    // 참여자 강퇴
    public Participant kickParticipant(Long hostId, Participant participant) {
        if (this.hostId != hostId) throw new IllegalArgumentException("해당 유저는 방장 권한이 없습니다.");
        if (participant.state() != ParticipantState.APPROVED) throw new IllegalStateException("참여중인 유저가 아닙니다.");

        participantSet.remove(participant);
        return participant.kick();
    }

    // 자동 승인제
    public StudyGroup autoPolicy() {
        return withPolicy(RecruitingPolicy.AUTO);
    }

    // 방장 승인제
    public StudyGroup approvePolicy() {
        return withPolicy(RecruitingPolicy.APPROVAL);
    }

    // 모집중
    public StudyGroup recruit() {
        return withState(GroupState.RECRUITING);
    }

    // 모집 마감
    public StudyGroup close() {
        return withState(GroupState.CLOSE);
    }

    // 스터디 진행중
    public StudyGroup start() {
        return withState(GroupState.START);
    }

    public boolean isPull() {
        return participantSet.size() == capacity;
    }

    private StudyGroup(Long id, Long hostId, String title, int capacity, Set<Participant> participantSet,
                        LocalDateTime deadline, RecruitingPolicy policy, GroupState state) {
        this.id = id;
        this.hostId = hostId;
        this.title = title;
        this.capacity = capacity;
        this.participantSet = participantSet;
        this.deadline = deadline;
        this.policy = policy;
        this.state = state;
    }

    private StudyGroup withPolicy(RecruitingPolicy policy) {
        return new StudyGroup(this.id, this.hostId, this.title, this.capacity, this.participantSet, this.deadline, policy, this.state);
    }

    private StudyGroup withState(GroupState state) {
        return new StudyGroup(this.id, this.hostId, this.title, this.capacity, this.participantSet, this.deadline, this.policy, state);
    }
}
