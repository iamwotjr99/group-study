package com.jaeseok.groupStudy.chat.presentation;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.chat.application.ChatService;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/history")
public class ChatHistoryController {

    private final ChatService chatService;

    @RequestMapping("{roomId}")
    public ResponseEntity<Page<SendMessageInfo>> getChatHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Long roomId,
            Pageable pageable
    ) {
        Page<SendMessageInfo> chatHistory = chatService.getChatHistory(roomId,
                userPrincipal.userId(), pageable);

        return ResponseEntity.ok(chatHistory);
    }
}
