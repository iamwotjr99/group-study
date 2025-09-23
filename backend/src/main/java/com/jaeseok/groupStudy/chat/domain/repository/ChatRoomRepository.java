package com.jaeseok.groupStudy.chat.domain.repository;

import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
