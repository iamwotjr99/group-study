package com.jaeseok.groupStudy.chat.domain.repository;

import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

}
