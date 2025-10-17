package com.jaeseok.groupStudy.unit.chat.application;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jaeseok.groupStudy.auth.application.MemberDetailsService;
import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.JwtTokenProvider;
import com.jaeseok.groupStudy.chat.application.ChatService;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import com.jaeseok.groupStudy.chat.exception.ChatRoomNotFoundException;
import com.jaeseok.groupStudy.chat.presentation.ChatHistoryController;
import com.jaeseok.groupStudy.config.SecurityConfig;
import com.jaeseok.groupStudy.member.exception.MemberNotFoundException;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupMemberAccessException;
import com.jaeseok.groupStudy.studyGroup.exception.StudyGroupNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ChatHistoryController.class)
@Import(SecurityConfig.class)
@DisplayName("Chat History Controller 단위 테스트")
class ChatHistoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ChatService chatService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    MemberDetailsService memberDetailsService;

    UserPrincipal testUserPrincipal;

    Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        setTestUserPrincipal(USER_ID);
    }

    @Test
    @DisplayName("유효한 유저의 요청 시, 채팅 내역을 페이징 조회하여 200 OK로 응답한다.")
    void givenValidRoomId_whenGetChatHistory_thenReturnPageableChatHistoryAndOK() throws Exception {
        // given
        Long roomId = 10L;
        Long userId = USER_ID;
        Pageable pageable = PageRequest.of(0, 5);

        List<SendMessageInfo> mockChatHistory = createMockChatHistory(userId, 30);
        List<SendMessageInfo> firstPagingMockChatHistory = mockChatHistory.subList((int) pageable.getOffset(), (int) pageable.getOffset() + pageable.getPageSize());
        Page<SendMessageInfo> mockPage = new PageImpl<>(firstPagingMockChatHistory, pageable, 30);

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        given(chatService.getChatHistory(roomId, userId, pageable)).willReturn(mockPage);

        // when
        ResultActions actions = mockMvc.perform(
                get("/api/chat/history/{roomId}", roomId)
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nickname").value("nickname1"))
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.content.length()").value(5))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹의 채팅 내역을 조회 시, 예외를 던진다.")
    void givenNotExistStudyGroup_whenGetChatHistory_thenThrowException() throws Exception {
        // given
        Long roomId = 10L;
        Long userId = USER_ID;
        Pageable pageable = PageRequest.of(0, 5);

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        given(chatService.getChatHistory(roomId, userId, pageable)).willThrow(new StudyGroupNotFoundException("존재하지 않는 스터디 그룹입니다."));

        // when
        ResultActions actions = mockMvc.perform(
                get("/api/chat/history/{roomId}", roomId)
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isNotFound());

        verify(chatService, times(1)).getChatHistory(roomId, userId, pageable);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방의 채팅 내역을 조회 시, 예외를 던진다.")
    void givenNotExistRoomId_whenGetChatHistory_thenThrowException() throws Exception {
        // given
        Long notExistRoomId = 404L;
        Long userId = USER_ID;
        Pageable pageable = PageRequest.of(0, 5);

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        given(chatService.getChatHistory(notExistRoomId, userId, pageable)).willThrow(new ChatRoomNotFoundException("존재하지 않는 채팅방 입니다."));

        // when
        ResultActions actions = mockMvc.perform(
                get("/api/chat/history/{roomId}", notExistRoomId)
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isNotFound());

        verify(chatService, times(1)).getChatHistory(notExistRoomId, userId, pageable);
    }

    @Test
    @DisplayName("존재하지 않는 유저가 채팅방의 채팅 내역을 조회 시, 예외를 던진다.")
    void givenNotExistUserId_whenGetChatHistory_thenThrowException() throws Exception {
        // given
        Long notExistRoomId = 404L;
        Long userId = USER_ID;
        Pageable pageable = PageRequest.of(0, 5);

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        given(chatService.getChatHistory(notExistRoomId, userId, pageable)).willThrow(new MemberNotFoundException("존재하지 않는 채팅방 입니다."));

        // when
        ResultActions actions = mockMvc.perform(
                get("/api/chat/history/{roomId}", notExistRoomId)
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isNotFound());

        verify(chatService, times(1)).getChatHistory(notExistRoomId, userId, pageable);
    }

    @Test
    @DisplayName("채팅방에 접근 권한이 없는 경우 (참여자 X) 채팅 내역을 조회 시, 예외를 던진다.")
    @WithMockUser(username = "test@test.com")
    void givenNotMemberRoomId_whenGetChatHistory_thenThrowException() throws Exception {
        // given
        Long notExistRoomId = 404L;
        Long userId = USER_ID;
        Pageable pageable = PageRequest.of(0, 5);

        given(memberDetailsService.loadUserByUsername("test@test.com")).willReturn(testUserPrincipal);
        given(chatService.getChatHistory(notExistRoomId, userId, pageable)).willThrow(new StudyGroupMemberAccessException("해당 유저는 승인된 참여자가 아닙니다."));

        // when
        ResultActions actions = mockMvc.perform(
                get("/api/chat/history/{roomId}", notExistRoomId)
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isForbidden());

        verify(chatService, times(1)).getChatHistory(notExistRoomId, userId, pageable);
    }

    private void setTestUserPrincipal(Long userId) {
        testUserPrincipal = new UserPrincipal(userId, "test@test.com", "password1234");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUserPrincipal, null, testUserPrincipal.getAuthorities())
        );
    }

    private List<SendMessageInfo> createMockChatHistory(Long senderId, int count) {
        List<SendMessageInfo> dtos = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String nickname = "nickname" + (i % 3);
            SendMessageInfo sendMessageInfo = new SendMessageInfo(senderId, nickname, "테스트 메세지 " + i,
                    LocalDateTime.now());
            dtos.add(sendMessageInfo);
        }

        return dtos;
    }
}