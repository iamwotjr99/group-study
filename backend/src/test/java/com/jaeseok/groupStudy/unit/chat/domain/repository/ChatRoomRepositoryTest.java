package com.jaeseok.groupStudy.unit.chat.domain.repository;


import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import com.jaeseok.groupStudy.chat.domain.repository.ChatRoomRepository;
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

    @Test
    @DisplayName("id를 통해서 ChatRoom 엔티티가 존재하는지 확인할 수 있다.")
    void givenChatRoomId_whenExistById_thenReturnBoolean() {
        // given
        ChatRoom chatRoom = ChatRoom.of(1L);
        ChatRoom saved = chatRoomRepository.save(chatRoom);

        Long chatRoomId = saved.getId();
        Long notExistChatRoomId = 100L;

        // when
        boolean expectedTrue = chatRoomRepository.existsById(chatRoomId);
        boolean expectedFalse = chatRoomRepository.existsById(notExistChatRoomId);

        // then
        assertThat(expectedTrue).isTrue();
        assertThat(expectedFalse).isFalse();
    }
}