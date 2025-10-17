package com.jaeseok.groupStudy.unit.chat.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.jaeseok.groupStudy.chat.application.ChatService;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageCommand;
import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import com.jaeseok.groupStudy.chat.domain.repository.ChatMessageRepository;
import com.jaeseok.groupStudy.chat.domain.repository.ChatRoomRepository;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.event.StudyGroupCreatedEvent;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith({MockitoExtension.class})
class ChatServiceTest {

    @Mock
    ChatMessageRepository chatMessageRepository;

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    StudyGroupCommandRepository studyGroupCommandRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    ChatService chatService;

    @Mock
    StudyGroup studyGroup;

    @Mock
    Member member;

    @Test
    @DisplayName("그룹 생성 이벤트를 수신하고 해당 스터디 그룹이 존재하면 채팅방을 생성한다.")
    void givenStudyGroupId_whenCreateChatRoom_thenReturnChatRoomId() {
        // given
        Long studyGroupId = 1L;
        StudyGroupCreatedEvent event = new StudyGroupCreatedEvent(studyGroupId);

        ChatRoom willReturnChatRoom = ChatRoom.of(studyGroupId);
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(willReturnChatRoom);

        // when
        chatService.createChatRoom(event);

        // then
        ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRoomRepository).save(captor.capture());

        ChatRoom savedChatRoom = captor.getValue();
        assertThat(savedChatRoom.getStudyGroupId()).isEqualTo(studyGroupId);
    }

    @Test
    @DisplayName("그룹에 속한 참여자가 메세지를 보내면 저장하고 메세지를 응답한다.")
    void givenSendMessageCommand_whenSendMessage_thenSaveChatMessage() {
        // given
        Long studyGroupId = 1L;
        Long roomId = 5L;
        Long senderId = 10L;
        String message = "안녕하세요.";
        MessageType type = MessageType.CHAT;

        ChatRoom willReturnChatRoom = ChatRoom.of(studyGroupId);
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(willReturnChatRoom));
        given(studyGroupCommandRepository.findById(willReturnChatRoom.getStudyGroupId())).willReturn(Optional.of(studyGroup));
        given(memberRepository.findById(senderId)).willReturn(Optional.of(member));

        ChatMessage mockSavedMessage = ChatMessage.of(roomId, senderId, message, type);
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(mockSavedMessage);

        SendMessageCommand cmd = new SendMessageCommand(roomId, senderId, message,
                type);

        // when
        SendMessageInfo sendMessageInfo = chatService.sendMessage(cmd);

        // then
        verify(chatRoomRepository, times(1)).findById(roomId);
        verify(studyGroupCommandRepository, times(1)).findById(willReturnChatRoom.getStudyGroupId());
        verify(studyGroup, times(1)).isMember(senderId);
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(memberRepository, times(1)).findById(senderId);
        verifyNoMoreInteractions(chatRoomRepository, studyGroupCommandRepository, studyGroup, chatMessageRepository);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(captor.capture());

        ChatMessage savedMessage = captor.getValue();
        assertThat(savedMessage.getContent()).isEqualTo(message);
        assertThat(savedMessage.getType()).isEqualTo(type);

        assertThat(sendMessageInfo.content()).isEqualTo(message);
    }

    @Test
    @DisplayName("그룹에 속한 참여자가 입장을 하면 입장 메세지를 저장하고 응답한다.")
    void givenRoomIdAndSenderId_whenSendMessage_thenSaveEnterMessage() {
        // given
        Long studyGroupId = 1L;
        Long roomId = 5L;
        Long senderId = 10L;

        ChatRoom willReturnChatRoom = ChatRoom.of(studyGroupId);
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(willReturnChatRoom));
        given(studyGroupCommandRepository.findById(willReturnChatRoom.getStudyGroupId())).willReturn(Optional.of(studyGroup));
        given(memberRepository.findById(senderId)).willReturn(Optional.of(member));
        given(member.getUserInfoNickname()).willReturn("테스트 유저");

        // when
        SendMessageInfo sendMessageInfo = chatService.enterChatRoom(roomId, senderId);

        // then
        verify(chatRoomRepository, times(1)).findById(roomId);
        verify(studyGroupCommandRepository, times(1)).findById(willReturnChatRoom.getStudyGroupId());
        verify(memberRepository, times(1)).findById(senderId);
        verify(studyGroup, times(1)).isMember(senderId);
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verifyNoMoreInteractions(chatRoomRepository, studyGroupCommandRepository, studyGroup, chatMessageRepository);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(captor.capture());

        ChatMessage savedMessage = captor.getValue();
        assertThat(savedMessage.getContent()).isEqualTo("테스트 유저님이 입장하셨습니다.");
        assertThat(savedMessage.getType()).isEqualTo(MessageType.ENTER);

        assertThat(sendMessageInfo.content()).isEqualTo("테스트 유저님이 입장하셨습니다.");
    }

    @Test
    @DisplayName("그룹에 속한 참여자가 퇴장을 하면 퇴장 메세지를 저장하고 퇴장한다.")
    void givenRoomIdAndSenderId_whenSendMessage_thenSaveLeaveMessage() {
        // given
        Long studyGroupId = 1L;
        Long roomId = 5L;
        Long senderId = 10L;

        ChatRoom willReturnChatRoom = ChatRoom.of(studyGroupId);
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(willReturnChatRoom));
        given(studyGroupCommandRepository.findById(willReturnChatRoom.getStudyGroupId())).willReturn(Optional.of(studyGroup));
        given(memberRepository.findById(senderId)).willReturn(Optional.of(member));
        given(member.getUserInfoNickname()).willReturn("테스트 유저");

        // when
        SendMessageInfo sendMessageInfo = chatService.leaveChatRoom(roomId, senderId);

        // then
        verify(chatRoomRepository, times(1)).findById(roomId);
        verify(studyGroupCommandRepository, times(1)).findById(willReturnChatRoom.getStudyGroupId());
        verify(memberRepository, times(1)).findById(senderId);
        verify(studyGroup, times(1)).isMember(senderId);
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verifyNoMoreInteractions(chatRoomRepository, studyGroupCommandRepository, studyGroup, chatMessageRepository);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository).save(captor.capture());

        ChatMessage savedMessage = captor.getValue();
        assertThat(savedMessage.getContent()).isEqualTo("테스트 유저님이 퇴장하셨습니다.");
        assertThat(savedMessage.getType()).isEqualTo(MessageType.LEAVE);

        assertThat(sendMessageInfo.content()).isEqualTo("테스트 유저님이 퇴장하셨습니다.");
    }

    @Test
    @DisplayName("그룹에 속하지 않은 유저는 메세지를 보낼 수 없다.")
    void givenInvalidMember_whenSendMessage_thenThrowException() {
        // given
        Long studyGroupId = 1L;
        Long roomId = 5L;
        Long invalidSenderId = 10L;
        String message = "안녕하세요.";
        MessageType type = MessageType.CHAT;

        ChatRoom willReturnChatRoom = ChatRoom.of(studyGroupId);
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(willReturnChatRoom));
        given(studyGroupCommandRepository.findById(willReturnChatRoom.getStudyGroupId())).willReturn(Optional.of(studyGroup));

        doThrow(new IllegalArgumentException("해당 유저는 승인된 참여자가 아닙니다."))
                .when(studyGroup).isMember(invalidSenderId);

        SendMessageCommand cmd = new SendMessageCommand(roomId, invalidSenderId, message, type);

        // when & then
        assertThatThrownBy(() -> chatService.sendMessage(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 유저는 승인된 참여자가 아닙니다.");

        verifyNoMoreInteractions(chatRoomRepository, studyGroupCommandRepository, studyGroup, chatMessageRepository);
    }

    @Test
    @DisplayName("그룹에 속한 참여자는 채팅 기록을 조회할 수 있다.")
    void givenRoomIdAndMemberIdAndPageable_whenGetChatHistory_thenReturnPagingSendMessageInfo() {
        // given
        Long studyGroupId = 1L;
        Long roomId = 1L;
        Long memberId = 10L;
        Pageable pageable = PageRequest.of(0, 8);

        ChatRoom willReturnChatRoom = ChatRoom.of(studyGroupId);
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(willReturnChatRoom));
        given(studyGroupCommandRepository.findById(willReturnChatRoom.getStudyGroupId())).willReturn(Optional.of(studyGroup));

        List<Object[]> allMockMessageInfo = createMockGetMessageInfo(roomId, 25);
        List<Object[]> firstPagingData = allMockMessageInfo.subList((int) pageable.getOffset(),
                (int) (pageable.getOffset() + pageable.getPageSize()));
        PageImpl<Object[]> mockPages = new PageImpl<>(firstPagingData, pageable, 25);

        given(chatMessageRepository.findChatMessageHistoryWithUser(roomId, pageable)).willReturn(mockPages);

        // when
        Page<SendMessageInfo> result = chatService.getChatHistory(roomId, memberId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(8);

        SendMessageInfo firstMessageInfo = result.getContent().get(0);
        assertThat(firstMessageInfo.nickname()).isEqualTo("nickname2");
        assertThat(firstMessageInfo.content()).isEqualTo("메세지 1");

        verify(chatRoomRepository, times(1)).findById(roomId);
        verify(studyGroupCommandRepository, times(1)).findById(willReturnChatRoom.getStudyGroupId());
        verify(studyGroup, times(1)).isMember(memberId);
        verify(chatMessageRepository, times(1)).findChatMessageHistoryWithUser(roomId, pageable);
        verifyNoMoreInteractions(chatRoomRepository, studyGroupCommandRepository, studyGroup, chatMessageRepository);
    }

    @Test
    @DisplayName("그룹에 속하지 않은 유저는 채팅 내역을 조회할 수 없다.")
    void givenInvalidMember_whenGetChatHistory_thenThrowException() {
        // given
        Long studyGroupId = 1L;
        Long roomId = 1L;
        Long invalidMemberId = 10L;
        Pageable pageable = PageRequest.of(0, 8);

        ChatRoom willReturnChatRoom = ChatRoom.of(studyGroupId);
        given(chatRoomRepository.findById(roomId)).willReturn(Optional.of(willReturnChatRoom));
        given(studyGroupCommandRepository.findById(willReturnChatRoom.getStudyGroupId())).willReturn(Optional.of(studyGroup));

        doThrow(new IllegalArgumentException("해당 유저는 승인된 참여자가 아닙니다."))
                .when(studyGroup).isMember(invalidMemberId);

        // when & then
        assertThatThrownBy(() -> chatService.getChatHistory(roomId, invalidMemberId, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 유저는 승인된 참여자가 아닙니다.");

        verifyNoMoreInteractions(chatRoomRepository, studyGroupCommandRepository, studyGroup, chatMessageRepository);
    }

    private List<Object[]> createMockGetMessageInfo(Long roomId, int count) {
        List<Object[]> dtos = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String nickname = i % 2 == 0 ? "nickname1" : "nickname2";
            ChatMessage chatMessage = ChatMessage.of(roomId, (long) i, "메세지 " + i,
                    MessageType.CHAT);
            Object[] mockObj = {chatMessage, nickname, chatMessage.getSenderId()};
            dtos.add(mockObj);
        }

        return dtos;
    }
}