package com.jaeseok.groupStudy.chat.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ChatRoom 도메인 단위 테스트")
class ChatRoomTest {

    @Test
    @DisplayName("정적 메서드 of()를 통해서 ChatRoom을 생성할 수 있다.")
    void givenStudyGroupId_whenOf_thenCreateChatRoom() {
        // given
        Long studyGroupId = 1L;

        // when
        ChatRoom chatRoom = ChatRoom.of(studyGroupId);

        // then
        assertThat(chatRoom).isNotNull();
        assertThat(chatRoom.getStudyGroupId()).isEqualTo(studyGroupId);
    }
}