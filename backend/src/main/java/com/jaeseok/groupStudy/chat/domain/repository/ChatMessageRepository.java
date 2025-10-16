package com.jaeseok.groupStudy.chat.domain.repository;

import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 해당 채팅방의 메세지 기록 최신순으로 페이징하여 조회한다.
     * @param chatRoomId
     * @param pageable
     * @return 페이지 요구 개수에 맞는 메세지 개수 배열을 반환
     */
    @Query("SELECT cm, me.memberInfoEntity.nickname, me.id "
            + "FROM ChatMessage cm JOIN MemberEntity me ON cm.senderId = me.id "
            + "WHERE cm.chatRoomId = :chatRoomId "
            + "ORDER BY cm.createdAt DESC")
    Page<Object[]> findChatMessageHistoryWithUser(@Param("chatRoomId") Long chatRoomId, Pageable pageable);
}
