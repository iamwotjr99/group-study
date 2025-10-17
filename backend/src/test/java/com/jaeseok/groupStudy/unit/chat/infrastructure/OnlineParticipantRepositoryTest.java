package com.jaeseok.groupStudy.unit.chat.infrastructure;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.chat.infrastructure.OnlineParticipantRepository;
import com.jaeseok.groupStudy.chat.infrastructure.dto.ParticipantInfo;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("온라인 참여자 Repository 테스트")
class OnlineParticipantRepositoryTest {

    OnlineParticipantRepository participantRepository;

    @BeforeEach
    void setUp() {
        this.participantRepository = new OnlineParticipantRepository();
    }

    @Test
    @DisplayName("새로운 참여자를 추가하면 저장소에 정상적으로 등록된다.")
    void givenRoomIdAndParticipantInfo_whenAdd_thenAddParticipantInMap() {
        // given
        Long roomId = 1L;
        ParticipantInfo participantInfo = ParticipantInfo.of(roomId, 10L, "testUser", "session1");

        // when
        participantRepository.add(roomId, participantInfo);

        // then
        assertThat(participantRepository.getParticipants(roomId))
                .hasSize(1)
                .contains(participantInfo);
    }

    @Test
    @DisplayName("같은 유저가 재접속하면 이전 세션 정보는 삭제되고 새 세션 정보로 갱신된다.")
    void givenRoomIdAndSameParticipantInfo_whenAdd_thenAddNewSessionParticipantInMap() {
        // given
        Long roomId = 1L;
        ParticipantInfo oldSession = ParticipantInfo.of(roomId, 10L, "testUser", "session1");
        participantRepository.add(roomId, oldSession);

        // when
        ParticipantInfo newSession = ParticipantInfo.of(roomId, 10L, "testUser", "session2");
        participantRepository.add(roomId, newSession);

        // then
        assertThat(participantRepository.getParticipants(roomId))
                .hasSize(1)
                .doesNotContain(oldSession)
                .contains(newSession);
    }

    @Test
    @DisplayName("참여자를 제거하면 저장소에서 정상적으로 제거된다.")
    void givenSessionId_whenRemove_thenRemoveParticipantInMap() {
        // given
        Long roomId = 1L;
        ParticipantInfo participantInfo = ParticipantInfo.of(roomId, 10L, "testUser", "session1");
        participantRepository.add(roomId, participantInfo);

        // when
        ParticipantInfo removed = participantRepository.remove(participantInfo.sessionId());

        // then
        assertThat(removed).isEqualTo(participantInfo);
        assertThat(participantRepository.getParticipants(roomId)).isEmpty();
    }

    @Test
    @DisplayName("특정 방의 모든 참여자 정보를 가져온다.")
    void givenRoomId_whenGetParticipants_thenReturnParticipantSet() {
        // given
        Long roomId = 1L;
        for (int i = 10; i <= 12; i++) {
            ParticipantInfo participantInfo = ParticipantInfo.of(roomId, (long) i, "testUser " + i,
                    "session" + i);
            participantRepository.add(roomId, participantInfo);
        }

        // when
        Set<ParticipantInfo> participants = participantRepository.getParticipants(roomId);

        // then
        assertThat(participants)
                .hasSize(3)
                .containsExactlyInAnyOrderElementsOf(participants);
    }
}
