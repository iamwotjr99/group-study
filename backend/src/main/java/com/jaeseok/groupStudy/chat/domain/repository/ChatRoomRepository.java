package com.jaeseok.groupStudy.chat.domain.repository;

import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsById(Long id);
    Optional<ChatRoom> findByStudyGroupId(Long studyGroupId);
}
