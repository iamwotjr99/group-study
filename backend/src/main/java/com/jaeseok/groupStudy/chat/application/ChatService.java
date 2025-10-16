package com.jaeseok.groupStudy.chat.application;

import com.jaeseok.groupStudy.chat.application.dto.SendMessageCommand;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import com.jaeseok.groupStudy.chat.domain.repository.ChatMessageRepository;
import com.jaeseok.groupStudy.chat.domain.repository.ChatRoomRepository;
import com.jaeseok.groupStudy.chat.exception.ChatRoomNotFoundException;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.member.exception.MemberNotFoundException;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.event.StudyGroupCreatedEvent;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final StudyGroupCommandRepository studyGroupCommandRepository;
    private final MemberRepository memberRepository;

    // 채팅방 생성
    @EventListener
    public Long createChatRoom(StudyGroupCreatedEvent event) {
        ChatRoom chatRoom = ChatRoom.of(event.studyGroupId());
        chatRoom = chatRoomRepository.save(chatRoom);

        return chatRoom.getId();
    }

    // 메시지 전송
    @Transactional
    public SendMessageInfo sendMessage(SendMessageCommand cmd) {
        validateChatAccess(cmd.roomId(), cmd.senderId());

        Member member = checkMember(cmd.senderId());

        ChatMessage chatMessage = ChatMessage.of(cmd.roomId(), cmd.senderId(), cmd.message(), cmd.type());
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        return new SendMessageInfo(member.getId(), member.getUserInfoNickname(), savedMessage.getContent(), savedMessage.getCreatedAt());
    }

    // 채팅방 입장 메시지 메서드
    @Transactional
    public SendMessageInfo enterChatRoom(Long roomId, Long senderId) {
        validateChatAccess(roomId, senderId);

        Member member = checkMember(senderId);

        String systemMessage = member.getUserInfoNickname() + "님이 입장하셨습니다.";

        ChatMessage enterMessage = ChatMessage.of(roomId, senderId, systemMessage,
                MessageType.ENTER);
        chatMessageRepository.save(enterMessage);

        return new SendMessageInfo(member.getId(), member.getUserInfoNickname(), enterMessage.getContent(), enterMessage.getCreatedAt());
    }

    // 채팅방 퇴장 메시지 메서드
    @Transactional
    public SendMessageInfo leaveChatRoom(Long roomId, Long senderId) {
        validateChatAccess(roomId, senderId);

        Member member = checkMember(senderId);

        String systemMessage = member.getUserInfoNickname() + "님이 퇴장하셨습니다.";
        ChatMessage chatMessage = ChatMessage.of(roomId, senderId, systemMessage,
                MessageType.LEAVE);
        chatMessageRepository.save(chatMessage);

        return new SendMessageInfo(member.getId(), member.getUserInfoNickname(), chatMessage.getContent(), chatMessage.getCreatedAt());
    }

    // 채팅 내역 조회
    @Transactional(readOnly = true)
    public Page<SendMessageInfo> getChatHistory(Long roomId, Long memberId, Pageable pageable) {
        validateChatAccess(roomId, memberId);

        Page<Object[]> queryResult = chatMessageRepository.findChatMessageHistoryWithUser(
                roomId, pageable);

        return queryResult.map(result -> {
            ChatMessage chatMessage = (ChatMessage) result[0];
            String nickname = (String) result[1];
            Long userId = (Long) result[2];
            return new SendMessageInfo(userId, nickname, chatMessage.getContent(), chatMessage.getCreatedAt());
        });
    }

    private void validateChatAccess(Long roomId, Long memberId) {
        ChatRoom chatRoom = checkChatRoom(roomId);
        StudyGroup studyGroup = checkStudyGroup(chatRoom.getStudyGroupId());
        studyGroup.isMember(memberId);
    }

    private StudyGroup checkStudyGroup(Long studyGroupId) {
        return studyGroupCommandRepository.findById(studyGroupId)
                .orElseThrow(() -> new StudyGroupNotFoundException("존재하지 않는 스터디 그룹 입니다."));
    }

    private ChatRoom checkChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("존재하지 않는 채팅방 입니다."));
    }

    private Member checkMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 유저입니다."));
    }
}
