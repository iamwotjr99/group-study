package com.jaeseok.groupStudy.integration.chat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import com.jaeseok.groupStudy.chat.domain.repository.ChatMessageRepository;
import com.jaeseok.groupStudy.chat.domain.repository.ChatRoomRepository;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatHistoryIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    StudyGroupCommandRepository studyGroupRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    ChatRoom chatRoom;
    Member memberA; // 방장
    Member memberB; // 참여자
    Member notMemberC; // 비 참여자

    @BeforeEach
    void setUp() {
        // 유저 3명 (A, B, C)
        Member memberAObj = Member.createMember("닉네임A", "testA@test.com", passwordEncoder.encode("password1234"));
        Member memberBObj = Member.createMember("닉네임B", "testB@test.com", passwordEncoder.encode("password1234"));
        Member memberCObj = Member.createMember("닉네임B", "testC@test.com", passwordEncoder.encode("password1234"));
        memberA = memberRepository.save(memberAObj);
        memberB = memberRepository.save(memberBObj);
        notMemberC = memberRepository.save(memberCObj);


        // 스터디 그룹 1개 (방장 A, 참여자 B, 비 참여자 C) + 채팅방 1개
        StudyGroup studyGroup = StudyGroup.createWithHost(memberA.getId(), "테스트 스터디그룹", 5,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
        studyGroup.apply(memberB.getId());
        studyGroup.apply(notMemberC.getId());
        studyGroup.approveParticipant(memberA.getId(), memberB.getId());
        StudyGroup savedStudyGroup = studyGroupRepository.save(studyGroup);

        ChatRoom chatRoomObj = ChatRoom.of(savedStudyGroup.getId());
        chatRoom = chatRoomRepository.save(chatRoomObj);

        // 채팅 내역 30개
        saveChatHistory(30);
    }

    @Test
    @DisplayName("채팅 기록 조회 요청 시 페이징된 채팅 내역과 200 OK를 응답한다.")
    void givenMemberIdAndRoomId_whenGetChatHistory_thenReturnPageAndOk() throws Exception {
        // given
        Long memberId = memberB.getId();
        Long roomId = chatRoom.getId();

        UserPrincipal userPrincipal = new UserPrincipal(memberId, memberB.getUserInfoEmail(),
                memberB.getUserInfoPassword());

        // when
        ResultActions actions = mockMvc.perform(get("/api/chat/history/{roomId}", roomId)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(userPrincipal))
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.content[0].content").value("테스트 메세지 29"));
    }

    @Test
    @DisplayName("멤버가 아닌 유저가 채팅 기록 조회 요청 시, 403 에러를 응답한다.")
    void givenNotMemberIdAndRoomId_whenGetChatHistory_thenReturnForbidden() throws Exception {
        // given
        Long memberId = notMemberC.getId();
        Long roomId = chatRoom.getId();

        UserPrincipal userPrincipal = new UserPrincipal(memberId, notMemberC.getUserInfoEmail(),
                notMemberC.getUserInfoPassword());
        // when
        ResultActions actions = mockMvc.perform(get("/api/chat/history/{roomId}", roomId)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(userPrincipal))
        );

        // then
        actions
                .andExpect(status().isForbidden());
    }

    private void saveChatHistory(int count) {
        for (int i = 0; i < count; i++) {
            Long senderId = i % 2 == 0 ? memberA.getId() : memberB.getId();
            ChatMessage chatMessage = ChatMessage.of(chatRoom.getId(), senderId, "테스트 메세지 " + i,
                    MessageType.CHAT);
            chatMessageRepository.save(chatMessage);
        }
    }

}
