package com.jaeseok.groupStudy.studyGroup.domain.participant;

import java.util.Objects;

public record Participant(Long userId, Long studyGroupId, ParticipantStatus status, ParticipantRole role) {

    public static Participant of(Long userId, Long studyGroupId, ParticipantStatus status, ParticipantRole role) {
        return new Participant(userId, studyGroupId, status, role);
    }

    // 참여 신청 Participant role: MEMBER
    public static Participant apply(Long participantId, Long studyGroupId) {
        return new Participant(participantId, studyGroupId, ParticipantStatus.PENDING, ParticipantRole.MEMBER);
    }

    // 방 생성할 때 방장은 Participant role: HOST
    public static Participant host(Long userId, Long studyGroupId) {
        return new Participant(userId, studyGroupId, ParticipantStatus.APPROVED, ParticipantRole.HOST);
    }

    /**
     * approve(), reject(), kick(), cancel(), leave() 함수는 participant의 상태를 바꾸는 기능만 제공
     * 상태를 바꾸는 책임은 애그리거트인 StudyGroup 에게 있음,
     * 상태에 대한 예외도 상태 일관성 처리를 위해 애그리거트인 StudyGroup 에게 있음
     * @return Participant
     */
    public Participant approve() {
        return withState(ParticipantStatus.APPROVED);
    }

    public Participant reject() {
        return withState(ParticipantStatus.REJECTED);
    }

    public Participant kick() {
        return withState(ParticipantStatus.KICKED);
    }

    // 신청 취소
    public Participant cancel() {
        return withState(ParticipantStatus.CANCELED);
    }

    // 스터디 나가기
    public Participant leave() {
        return withState(ParticipantStatus.LEAVE);
    }

    // 참여자가 방장인지 체크
    public boolean isHost() {
        return this.role == ParticipantRole.HOST;
    }

    // 참여자가 멤버인지 체크
    public boolean isMember() {
        return this.role == ParticipantRole.MEMBER;
    }

    private Participant withState(ParticipantStatus state) {
        return new Participant(this.userId, this.studyGroupId, state, this.role);
    }

    // studyGroupId가 정해졌을 때, 그 ID를 가진 새 Participant 객체를 반환하는 메서드
    public Participant withStudyGroupId(Long studyGroupId) {
        return new Participant(this.userId, studyGroupId, this.status, this.role);
    }

    /**
     * 동일한(참여자의 id 와 스터디의 id)는 동일한 객체로 판단
     * @param o   the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Participant that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(studyGroupId,
                that.studyGroupId);
    }

    /**
     * 동일한(참여자의 id 와 스터디의 id)는 동일한 해시코드를 가진다.
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, studyGroupId);
    }
}
