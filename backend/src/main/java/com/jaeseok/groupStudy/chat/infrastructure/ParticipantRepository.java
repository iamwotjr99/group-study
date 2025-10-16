package com.jaeseok.groupStudy.chat.infrastructure;

import com.jaeseok.groupStudy.chat.infrastructure.dto.ParticipantInfo;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 스터디 그룹 실시간 기능에 온라인 참여자가 누가 있는지 접속자 정보를 메모리로 관리하는 싱글톤 Bean
 */
@Component
public class ParticipantRepository {
    // <스터디 방 ID, 참여자 정보 Set>
    private final Map<Long, Set<ParticipantInfo>> onlineParticipantByRoom  = new ConcurrentHashMap<>();

    // <웹소켓 세션 ID, 참여자 정보> -> 퇴장 시 타켓 사용자를 세션 ID로 빠르게 찾을 수 있음
    private final Map<String, ParticipantInfo> sessionToParticipantMap = new ConcurrentHashMap<>();

    // 새로운 참여자 추가
    public void add(Long roomId, ParticipantInfo participantInfo) {
        // 데이터 정합성을 위해서 같은 userId를 가진 기존 정보가 있는지 확인후 제거
        onlineParticipantByRoom.getOrDefault(roomId, ConcurrentHashMap.newKeySet())
                .stream()
                .filter(p -> p.userId().equals(participantInfo.userId()))
                .findFirst()
                .ifPresent(oldInfo -> {
                    onlineParticipantByRoom.get(roomId).remove(oldInfo);
                    sessionToParticipantMap.remove(oldInfo.sessionId());
                });

        // 방이 없으면 새로 만들고, 방이 존재하면 참여자 추가
        onlineParticipantByRoom.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet())
                .add(participantInfo);
        sessionToParticipantMap.put(participantInfo.sessionId(), participantInfo);
    }

    // 참여자 제거 (세션 ID 기반)
    public ParticipantInfo remove(String sessionId) {
        ParticipantInfo participantInfo = sessionToParticipantMap.remove(sessionId);
        if (participantInfo != null) {
            onlineParticipantByRoom.getOrDefault(participantInfo.roomId(), ConcurrentHashMap.newKeySet()).remove(participantInfo);
        }
        return participantInfo;
    }

    // 특정 방의 모든 참여자 정보 가져오기
    public Set<ParticipantInfo> getParticipants(Long roomId) {
        return onlineParticipantByRoom.getOrDefault(roomId, ConcurrentHashMap.newKeySet());
    }


}
