package com.jaeseok.groupStudy.integration.webrtc;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeseok.groupStudy.auth.application.TokenProvider;
import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.integration.webrtc.utils.SignalTestStompSessionHandler;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.studyGroup.domain.RecruitingPolicy;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroup;
import com.jaeseok.groupStudy.studyGroup.domain.StudyGroupCommandRepository;
import com.jaeseok.groupStudy.webrtc.dto.SignalMessage;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
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
class SignalingIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    StudyGroupCommandRepository studyGroupRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    String url;

    Member sender;
    Member receiver;
    Member otherUser;
    StudyGroup studyGroup;

    String senderToken;
    String receiverToken;
    String otherUserToken;

    WebSocketStompClient stompClient;

    private void initTestUser() {
        Member senderObj = Member.createMember("Sender", "sender@test.com",
                passwordEncoder.encode("test1234"));
        Member receiverObj = Member.createMember("Receiver", "receiver@test.com",
                passwordEncoder.encode("test1234"));
        Member otherUserObj = Member.createMember("OtherUser", "otheruser@test.com", passwordEncoder.encode("test1234"));
        sender = memberRepository.save(senderObj);
        receiver = memberRepository.save(receiverObj);
        otherUser = memberRepository.save(otherUserObj);
    }

    private void initStudyGroup() {
        StudyGroup studyGroupObj = StudyGroup.createWithHost(receiver.getId(), "테스트 스터디 그룹", 5,
                LocalDateTime.now().plusDays(1), RecruitingPolicy.AUTO);
        studyGroupObj.apply(sender.getId());
        studyGroupObj.approveParticipant(receiver.getId(), sender.getId());

        studyGroup = studyGroupRepository.save(studyGroupObj);
    }

    private void authorizationUsers() {
        UserPrincipal userPrincipal = new UserPrincipal(sender.getId(), sender.getUserInfoEmail(),
                sender.getUserInfoPassword());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        senderToken = tokenProvider.generateAccessToken(authentication);

        userPrincipal = new UserPrincipal(receiver.getId(), receiver.getUserInfoEmail(), receiver.getUserInfoPassword());
        authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        receiverToken = tokenProvider.generateAccessToken(authentication);

        userPrincipal = new UserPrincipal(otherUser.getId(), otherUser.getUserInfoEmail(), otherUser.getUserInfoPassword());
        authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        otherUserToken = tokenProvider.generateAccessToken(authentication);
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
        initTestUser();
        initStudyGroup();
        authorizationUsers();
        stompClient = initWebSocketStompClient();

        this.url = "ws://localhost:" + port + "/ws/chat";
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        studyGroupRepository.deleteAll();
    }

    @Test
    @DisplayName("인증된 사용자가 시그널링 메시지를 보내면, 같은 방의 수신자가 시그널링 메시지를 받는다.")
    void givenAuthenticatedSession_whenRelaySignal_thenSendToReceiver() throws Exception{
        // given
        Long roomId = studyGroup.getId();

        // given: Receiver 클라이언트가 먼저 연결 및 구독
        SignalTestStompSessionHandler receiverSessionHandler = createStompSessionHandler();
        StompSession receiverSession = createStompSession(receiverToken, receiverSessionHandler);
        receiverSession.subscribe("/sub/signal/user/" + receiver.getId(), receiverSessionHandler);

        // given: Sender 세션 연결
        SignalTestStompSessionHandler senderSessionHandler = createStompSessionHandler();
        StompSession senderSession = createStompSession(senderToken, senderSessionHandler);

        // when
        // when: Signal Message 생성 및 발행
        SignalMessage message = SignalMessage.of("offer", "sdp", "candidate", "spidMid", 1, sender.getId(), receiver.getId());
        senderSession.send("/pub/signal/" + roomId, message);

        // then
        BlockingQueue<SignalMessage> receiveMessageQueue = receiverSessionHandler.getMessages();
        SignalMessage receiveMessage = receiveMessageQueue.poll(10, TimeUnit.SECONDS);

        assertThat(receiveMessage).isNotNull();
        assertThat(receiveMessage.type()).isEqualTo("offer");
        assertThat(receiveMessage.receiverId()).isEqualTo(receiver.getId());
        assertThat(receiveMessage.senderId()).isEqualTo(sender.getId());
    }

    @Test
    @DisplayName("방 멤버가 아닌 사용자가 시그널링 메세지를 보내면, 아무도 메시지를 받지 못한다 (인가 실패)")
    void givenNotMemberSession_whenRelaySignal_thenNotReceive() throws Exception{
        // given
        Long roomId = studyGroup.getId();

        // given: Receiver 클라이언트가 먼저 연결 및 구독
        SignalTestStompSessionHandler receiverSessionHandler = createStompSessionHandler();
        StompSession receiverSession = createStompSession(receiverToken, receiverSessionHandler);
        receiverSession.subscribe("/sub/signal/user/" + receiver.getId(), receiverSessionHandler);

        // given: Sender(방 멤버가 아닌 사용자) 세션 연결
        SignalTestStompSessionHandler senderSessionHandler = createStompSessionHandler();
        StompSession senderSession = createStompSession(otherUserToken, senderSessionHandler);

        // when
        // when: Signal Message 생성 및 발행
        SignalMessage message = SignalMessage.of("offer", "sdp", "candidate", "spidMid", 1, sender.getId(), receiver.getId());
        senderSession.send("/pub/signal/" + roomId, message);

        // then
        BlockingQueue<SignalMessage> receiveMessageQueue = receiverSessionHandler.getMessages();
        SignalMessage receiveMessage = receiveMessageQueue.poll(10, TimeUnit.SECONDS);

        assertThat(receiveMessage).isNotNull();
    }

    private StompSession createStompSession(String token, StompSessionHandler sessionHandler) throws Exception {
        // HTTP Handshake Header 생성 - JWT가 담긴 헤더
        String bearerToken = "Bearer " + token;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, bearerToken);
        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders(httpHeaders);

        // STOMP 세션 연결 (JWT 헤더 포함)
        WebSocketStompClient socketStompClient = initWebSocketStompClient();
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.set(HttpHeaders.AUTHORIZATION, bearerToken);

        StompSession session = socketStompClient.connectAsync(url, webSocketHttpHeaders,
                        stompHeaders, sessionHandler)
                .get(10, TimeUnit.SECONDS);

        return session;
    }

    private SignalTestStompSessionHandler createStompSessionHandler() {
        BlockingQueue<SignalMessage> receiveMessages = new LinkedBlockingDeque<>();
        return new SignalTestStompSessionHandler(receiveMessages);
    }
}
