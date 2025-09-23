package com.jaeseok.groupStudy.chat.domain;

import com.jaeseok.groupStudy.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, name = "study_group_id")
    private Long studyGroupId;

    public static ChatRoom of(Long studyGroupId) {
        return new ChatRoom(null, studyGroupId);
    }

    private ChatRoom(Long id, Long studyGroupId) {
        this.id = id;
        this.studyGroupId = studyGroupId;
    }
}
