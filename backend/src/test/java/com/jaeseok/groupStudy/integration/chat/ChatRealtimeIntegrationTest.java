package com.jaeseok.groupStudy.integration.chat;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.JwtTokenProvider;
import com.jaeseok.groupStudy.chat.application.dto.SendMessageInfo;
import com.jaeseok.groupStudy.chat.domain.ChatMessage;
import com.jaeseok.groupStudy.chat.domain.ChatRoom;
import com.jaeseok.groupStudy.chat.domain.MessageType;
import com.jaeseok.groupStudy.chat.domain.repository.ChatMessageRepository;
import com.jaeseok.groupStudy.chat.domain.repository.ChatRoomRepository;
import com.jaeseok.groupStudy.chat.presentation.dto.SendMessagePayload;
import com.jaeseok.groupStudy.integration.chat.utils.TestStompSessionHandler;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatRealtimeIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    StudyGroupCommandRepository studyGroupRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    String url;

    Member memberA;
    Member memberB;
    ChatRoom chatRoom;
    String memberAToken;
    String memberBToken;

    private void initTestUser() {
        Member memberAObj = Member.createMember("닉네임A", "testA@test.com", passwordEncoder.encode("password1234"));
        memberA = memberRepository.save(memberAObj);
        Member memberBObj = Member.createMember("닉네임B", "testB@test.com", passwordEncoder.encode("password1234"));
        memberB = memberRepository.save(memberBObj);
    }

    private void initStudyGroup() {
        StudyGroup studyGroup = StudyGroup.createWithHost(memberA.getId(), "테스트 스터디그룹", 5,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.APPROVAL);
        StudyGroup savedStudyGroup = studyGroupRepository.save(studyGroup);

        ChatRoom chatRoomObj = ChatRoom.of(savedStudyGroup.getId());
        chatRoom = chatRoomRepository.save(chatRoomObj);
    }

    private void authorizationUser() {
        UserPrincipal userPrincipal = new UserPrincipal(memberA.getId(), memberA.getUserInfoEmail(),
                memberA.getUserInfoPassword());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        memberAToken = tokenProvider.generateAccessToken(authentication);

        userPrincipal = new UserPrincipal(memberB.getId(), memberB.getUserInfoEmail(),
                memberB.getUserInfoPassword());
        authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        memberBToken = tokenProvider.generateAccessToken(authentication);
    }

    private WebSocketStompClient initWebSocketStompClient() {
        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        WebSocketTransport webSocketTransport = new WebSocketTransport(standardWebSocketClient);
        List<Transport> transports = Collections.singletonList(webSocketTransport);
        SockJsClient sockJsClient = new SockJsClient(transports);

        WebSocketStompClient socketStompClient = new WebSocketStompClient(sockJsClient);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        socketStompClient.setMessageConverter(converter);

        return socketStompClient;
    }

    @BeforeEach
    void setUp() {
        // 유저 2명
        initTestUser();
        // 스터디 그룹 1개 (방장 A) + 채팅방 1개
        initStudyGroup();
        // JWT 토큰 발급
        authorizationUser();

        this.url = "ws://localhost:" + port + "/ws/chat";
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        studyGroupRepository.deleteAll();
        chatRoomRepository.deleteAll();
        chatMessageRepository.deleteAll();
    }

    @Test
    @DisplayName("인증된 유저가 메시지를 보내면, 다른 구독자에게 브로드캐스팅되고 DB에 저장된다.")
    void givenAuthenticatedUserSession_whenSendMessage_thenBroadcastAndSaveToDB() throws Exception {
        // given
        Long roomId = chatRoom.getId();
        String message = "테스트 메세지";

        // 메시지 수신용 큐 (수신 클라이언트 역할)
        BlockingQueue<SendMessageInfo> receivedMessages = new LinkedBlockingDeque<>();
        BlockingQueue<StompHeaders> receivedErrorMessages = new LinkedBlockingDeque<>();
        TestStompSessionHandler sessionHandler = new TestStompSessionHandler(
                receivedMessages, receivedErrorMessages);

        // HTTP Handshake Headers 생성 (JWT 인증용)
        String token = createBearerToken(memberAToken);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, token);
        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders(httpHeaders);

        // STOMP 세션 연결 (JWT 헤더 포함)
        WebSocketStompClient socketStompClient = initWebSocketStompClient();
        StompHeaders stompHeader = new StompHeaders();
        stompHeader.set(HttpHeaders.AUTHORIZATION, token);
        StompSession session = socketStompClient.connectAsync(url, webSocketHttpHeaders,
                        stompHeader, sessionHandler)
                .get(10, TimeUnit.SECONDS);

        // 채팅방 구독
        session.subscribe("/sub/chatroom/" + roomId, sessionHandler);

        SendMessagePayload payload = new SendMessagePayload(message, MessageType.CHAT);

        // when
        session.send("/pub/chatroom/" + roomId + "/message", payload);

        // then
        // 브로드 캐스팅 확인 (비동기 메세지가 큐에 도달할 때까지 대기)
        await().atMost(10, TimeUnit.SECONDS).until(() -> receivedMessages.size() == 1);

        SendMessageInfo receivedMessage = receivedMessages.poll();
        assertThat(receivedMessage.content()).isEqualTo(message);
        assertThat(receivedMessage.nickname()).isEqualTo(memberA.getUserInfoNickname());

        // DB 저장 확인
        // 트랜잭션이 커밋되기 전까지 일정 시간 대기
        await().atMost(3, TimeUnit.SECONDS).until(() -> chatMessageRepository.count() == 1);

        ChatMessage savedMessage = chatMessageRepository.findAll().get(0);
        assertThat(savedMessage.getSenderId()).isEqualTo(memberA.getId());
        assertThat(savedMessage.getContent()).isEqualTo(message);
        assertThat(savedMessage.getChatRoomId()).isEqualTo(roomId);

        session.disconnect();
    }

    String createBearerToken(String token) {
        return "Bearer " + token;
    }
}
