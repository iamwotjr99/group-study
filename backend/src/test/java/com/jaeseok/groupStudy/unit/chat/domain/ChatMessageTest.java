package com.jaeseok.groupStudy.chat.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ChatMessage 도메인 단위 테스트")
class ChatMessageTest {

    @Test
    @DisplayName("정적 메서드 of()를 통해서 ChatMessage를 생성할 수 있다.")
    void givenMessageInfo_whenOf_thenCreateMessage() {
        // given
        Long chatRoomId = 1L;
        Long senderId = 10L;
        String content = "안녕하세요 여러분";
        LocalDateTime timestamp = LocalDateTime.now();
        MessageType type = MessageType.CHAT;

        // when
        ChatMessage chatMessage = ChatMessage.of(chatRoomId, senderId, content, timestamp, type);

        // then
        assertThat(chatMessage).isNotNull();
        assertThat(chatMessage.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(chatMessage.getSenderId()).isEqualTo(senderId);
        assertThat(chatMessage.getContent()).isEqualTo(content);
        assertThat(chatMessage.getTimestamp()).isEqualTo(timestamp);
        assertThat(chatMessage.getType()).isEqualTo(type);
    }
}