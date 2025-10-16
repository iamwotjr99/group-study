package com.jaeseok.groupStudy.chat.infrastructure.dto;

public record ParticipantInfo(Long roomId, Long userId, String nickname, String sessionId) {
    public static ParticipantInfo of(Long roomId, Long userId, String nickname, String sessionId) {
        return new ParticipantInfo(roomId, userId, nickname, sessionId);
    }
}
