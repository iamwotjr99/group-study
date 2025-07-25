package com.jaeseok.groupStudy.participant.domain;

public enum ParticipantState {
    PENDING,    // 대기중
    APPROVED,   // 승인됨
    REJECTED,   // 거절됨
    CANCELED,   // 취소됨
    LEAVE,      // 그룹을 떠남
    KICKED      // 그룹에서 강퇴당함
}
