package com.jaeseok.groupStudy.participant.domain;

import java.util.Objects;

public record Participant(Long userId, Long studyGroupId, ParticipantState state) {

    // 참여 신청
    public static Participant apply(Long participantId, Long studyGroupId) {
        return new Participant(participantId, studyGroupId, ParticipantState.PENDING);
    }

    /**
     * approve(), reject(), kick() 함수는 participant의 상태를 바꾸는 기능만 제공
     * 상태를 바꾸는 책임은 애그리거트인 StudyGroup 에게 있음
     * @return
     */
    public Participant approve() {
        return withState(ParticipantState.APPROVED);
    }

    public Participant reject() {
        return withState(ParticipantState.REJECTED);
    }

    public Participant kick() {
        return withState(ParticipantState.KICKED);
    }

    // 신청 취소
    public Participant cancel() {
        if (this.state != ParticipantState.PENDING) throw new IllegalStateException("대기 상태에서만 취소할 수 있습니다.");
        return withState(ParticipantState.CANCELED);
    }

    // 스터디 나가기
    public Participant leave() {
        if (this.state != ParticipantState.APPROVED) throw new IllegalStateException("승인된 상태에서만 탈퇴할 수 있습니다.");
        return withState(ParticipantState.LEAVE);
    }

    private Participant withState(ParticipantState state) {
        return new Participant(this.userId, this.studyGroupId, state);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Participant that)) {
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
