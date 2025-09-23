package com.jaeseok.groupStudy.chat.domain.repository;


import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@DisplayName("ChatRoom JPA Repository 테스트")
class ChatRoomRepositoryTest {

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("ChatRoom 엔티티를 저장하고 조회할 수 있다.")
    void givenChatRoom_whenSaveAndFind_thenReturnEqual() {
        // given
        ChatRoom chatRoom = ChatRoom.of(1L);

        // when
        ChatRoom saved = chatRoomRepository.save(chatRoom);
        ChatRoom founded = chatRoomRepository.findById(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다."));

        // then
        assertThat(saved).isNotNull();
        assertThat(founded).isNotNull();
        assertThat(saved.getId()).isEqualTo(founded.getId());
        assertThat(saved.getStudyGroupId()).isEqualTo(founded.getStudyGroupId());
    }
}