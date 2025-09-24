package com.jaeseok.groupStudy.chat.application;

import com.jaeseok.groupStudy.chat.application.dto.SendMessageCommand;
import com.jaeseok.groupStudy.chat.application.dto.GetMessageInfo;
import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import com.jaeseok.groupStudy.chat.domain.repository.ChatMessageRepository;
import com.jaeseok.groupStudy.chat.domain.repository.ChatRoomRepository;
import com.jaeseok.groupStudy.chat.exception.ChatRoomNotFoundException;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupNotFoundException;
import lombok.RequiredArgsConstructor;
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

    // 채팅방 생성
    public Long createChatRoom(Long studyGroupId) {
        if(!studyGroupCommandRepository.existsById(studyGroupId)) {
            throw new StudyGroupNotFoundException("존재하지 않는 스터디 그룹입니다.");
        }

        ChatRoom chatRoom = ChatRoom.of(studyGroupId);
        chatRoom = chatRoomRepository.save(chatRoom);

        return chatRoom.getId();
    }

    // 메시지 전송
    public void sendMessage(SendMessageCommand cmd) {
        ChatRoom chatRoom = checkChatRoom(cmd.roomId());
        StudyGroup studyGroup = checkStudyGroup(chatRoom.getStudyGroupId());

        studyGroup.isMember(cmd.senderId());

        ChatMessage chatMessage = ChatMessage.of(cmd.roomId(), cmd.senderId(), cmd.message(), cmd.type());

        chatMessageRepository.save(chatMessage);
    }

    // 채팅 내역 조회
    @Transactional(readOnly = true)
    public Page<GetMessageInfo> getChatHistory(Long roomId, Long memberId, Pageable pageable) {
        ChatRoom chatRoom = checkChatRoom(roomId);
        StudyGroup studyGroup = checkStudyGroup(chatRoom.getStudyGroupId());

        studyGroup.isMember(memberId);

        Page<Object[]> queryResult = chatMessageRepository.findChatMessageHistoryWithNickname(
                roomId, pageable);

        return queryResult.map(result -> {
            ChatMessage cm = (ChatMessage) result[0];
            String n = (String) result[1];
            return new GetMessageInfo(n, cm.getContent(), cm.getCreatedAt());
        });
    }

    private StudyGroup checkStudyGroup(Long studyGroupId) {
        return studyGroupCommandRepository.findById(studyGroupId)
                .orElseThrow(() -> new StudyGroupNotFoundException("존재하지 않는 스터디 그룹 입니다."));
    }

    private ChatRoom checkChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("존재하지 않는 채팅방 입니다."));
    }
}
