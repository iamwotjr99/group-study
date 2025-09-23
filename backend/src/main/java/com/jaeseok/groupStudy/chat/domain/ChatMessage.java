package com.jaeseok.groupStudy.chat.domain;

import com.jaeseok.groupStudy.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "chat_room_id")
    Long chatRoomId;

    @Column(name = "sender_id")
    Long senderId;

    @Column(name = "content")
    String content;

    @Column(name = "timestamp")
    LocalDateTime timestamp;

    @Column(name = "type")
    MessageType type;

    public static ChatMessage of(Long chatRoomId, Long senderId, String content, LocalDateTime timestamp, MessageType type) {
        return new ChatMessage(null, chatRoomId, senderId, content, timestamp, type);
    }

    private ChatMessage(Long id, Long chatRoomId, Long senderId, String content, LocalDateTime timestamp, MessageType type) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
    }
}
