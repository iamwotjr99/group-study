package com.jaeseok.groupStudy.chat.presentation;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.chat.application.ChatService;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/history")
public class ChatHistoryController {

    private final ChatService chatService;

    /**
     * 해당 채팅방의 채팅 내역을 페이징 조회한다.
     * @param userPrincipal 유저의 인증 객체
     * @param roomId 해당 채팅방의 ID
     * @param pageable 페이지객체 ex) 25개를 5개씩 페이징 -> 5페이지 5개
     * @return 페이지 요구사항에 맞는 개수에 해당하는 채팅 내역 응답
     */
    @RequestMapping("{roomId}")
    public ResponseEntity<Page<SendMessageInfo>> getChatHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long roomId,
            Pageable pageable
    ) {
        Page<SendMessageInfo> chatHistory = chatService.getChatHistory(roomId,
                userPrincipal.userId(), pageable);

        return ResponseEntity.ok(chatHistory);
    }
}
