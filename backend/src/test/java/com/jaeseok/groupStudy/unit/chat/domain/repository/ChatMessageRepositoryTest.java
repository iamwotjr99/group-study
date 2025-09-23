package com.jaeseok.groupStudy.chat.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("ChatMessage JPA Repository 테스트")
class ChatMessageRepositoryTest {

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("ChatMessage 엔티티를 저장하고 조회할 수 있다.")
    void givenChatMessage_whenSaveAndFind_thenReturnEqual() {
        // given
        ChatRoom chatRoom = ChatRoom.of(1L);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        LocalDateTime timestamp = LocalDateTime.now();
        ChatMessage chatMessage = ChatMessage.of(savedChatRoom.getId(), 10L, "테스트 메세지", timestamp,
                MessageType.CHAT);

        // when
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        ChatMessage foundMessage = chatMessageRepository.findById(savedMessage.getId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메세지 입니다."));

        // then
        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getId()).isNotNull();
        assertThat(foundMessage).isNotNull();
        assertThat(foundMessage.getId()).isNotNull();
        assertThat(savedMessage.getChatRoomId()).isEqualTo(foundMessage.getChatRoomId());
        assertThat(savedMessage.getSenderId()).isEqualTo(foundMessage.getSenderId());
        assertThat(savedMessage.getContent()).isEqualTo(foundMessage.getContent());
        assertThat(savedMessage.getTimestamp()).isEqualTo(foundMessage.getTimestamp());
        assertThat(savedMessage.getType()).isEqualTo(foundMessage.getType());
    }
}