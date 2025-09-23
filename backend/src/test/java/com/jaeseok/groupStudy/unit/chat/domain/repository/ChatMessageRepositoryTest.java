package com.jaeseok.groupStudy.unit.chat.domain.repository;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import com.jaeseok.groupStudy.chat.domain.repository.ChatMessageRepository;
import com.jaeseok.groupStudy.chat.domain.repository.ChatRoomRepository;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = MemberRepository.class))
@DisplayName("ChatMessage JPA Repository 테스트")
class ChatMessageRepositoryTest {

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("ChatMessage 엔티티를 저장하고 조회할 수 있다.")
    void givenChatMessage_whenSaveAndFind_thenReturnEqual() {
        // given
        ChatRoom chatRoom = ChatRoom.of(1L);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        ChatMessage chatMessage = ChatMessage.of(savedChatRoom.getId(), 10L, "테스트 메세지",
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
        assertThat(savedMessage.getCreatedAt()).isEqualTo(foundMessage.getCreatedAt());
        assertThat(savedMessage.getType()).isEqualTo(foundMessage.getType());
    }

    @Test
    @DisplayName("ChatMessage를 닉네임과 함께 페이징 조회를 할 수 있다.")
    void givenChatMessagesWithUser_whenFindChatMessageHistoryWithNickname_thenReturnPagingObject() {
        // given
        ChatRoom chatRoom = ChatRoom.of(1L);
        Long chatRoomId = chatRoomRepository.save(chatRoom).getId();

        Member member1 = Member.createMember("테스트 유저001", "test001@test.com", "password1234");
        Member member2 = Member.createMember("테스트 유저002", "test002@test.com", "password1234");

        member1 = memberRepository.save(member1);
        member2 = memberRepository.save(member1);
        for (int i = 0; i < 40; i++) {
            Long senderId = i % 2 == 0 ? member1.getId() : member2.getId();
            ChatMessage chatMessage = ChatMessage.of(chatRoomId, senderId, "테스트 메세지 " + i,
                    MessageType.CHAT);
            chatMessageRepository.save(chatMessage);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Object[]> chatMessageHistoryWithNickname = chatMessageRepository.findChatMessageHistoryWithNickname(
                chatRoomId, pageable);

        // then
        assertThat(chatMessageHistoryWithNickname).isNotNull();
        assertThat(chatMessageHistoryWithNickname.getTotalElements()).isEqualTo(40);
        assertThat(chatMessageHistoryWithNickname.getTotalPages()).isEqualTo(4);
        assertThat(chatMessageHistoryWithNickname.getNumberOfElements()).isEqualTo(10);
        assertThat(chatMessageHistoryWithNickname.getContent()).hasSize(10);

        for (int i = 0; i < chatMessageHistoryWithNickname.getContent().size(); i++) {
            Object[] chatMessageHistory = chatMessageHistoryWithNickname.getContent().get(i);
            ChatMessage chatMessage = (ChatMessage) chatMessageHistory[0];
            String nickname = (String) chatMessageHistory[1];
            if (i % 2 == 0) {
                assertThat(chatMessage.getSenderId()).isEqualTo(member1.getId());
                assertThat(nickname).isEqualTo(member1.getUserInfoNickname());
            } else {
                assertThat(chatMessage.getSenderId()).isEqualTo(member2.getId());
                assertThat(nickname).isEqualTo(member2.getUserInfoNickname());
            }

        }

    }
}