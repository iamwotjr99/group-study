package com.jaeseok.groupStudy.integration.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginResponse;
import com.jaeseok.groupStudy.auth.presentation.dto.SignUpRequest;
import com.jaeseok.groupStudy.integration.IntegrationTestSupport;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthUseCaseIntegrationTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    String secretKey;

    String TEST_USER_EMAIL = "test@test.com";
    String TEST_USER_PASSWORD = "password1234";
    String TEST_USER_NICKNAME = "TestUser";

    @BeforeEach
    void setUp() {
        Member member = Member.createMember(TEST_USER_NICKNAME, TEST_USER_EMAIL,
                passwordEncoder.encode(TEST_USER_PASSWORD));
        memberRepository.save(member);
    }

    @Test
    @DisplayName("사용자는 유효한 정보로 회원가입 요청을 하면 회원가입 할 수 있다.")
    void 사용자는_회원가입에_성공한다() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("TestUser02", "otherTest@test.com",
                "password1234");

        // when
        ResultActions actions = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        Member savedMember = memberRepository.findByEmail("otherTest@test.com")
                .orElseThrow(() -> new EntityNotFoundException("유저가 저장되지 않았습니다."));
        actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(savedMember.getId()))
                .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 요청하면 400 에러를 응답한다.")
    void 이미_존재하는_이메일로_회원가입_요청하면_에러를_응답한다() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("TestUser02", TEST_USER_EMAIL,
                TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 회원가입 요청하면 400 에러를 응답한다.")
    void 이미_존재하는_닉네임으로_회원가입_요청하면_에러를_응답한다() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest(TEST_USER_NICKNAME, "otherTest@test.com",
                TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }


    @Test
    @DisplayName("사용자는 유효한 정보로 로그인 요청을 하면 로그인을 할 수 있고 토큰을 응답받는다.")
    void 사용자는_로그인에_성공한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
       actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.message").value("로그인 성공"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인을 요청하면 401 에러를 응답한다.")
    void 존재하지_않는_이메일로_로그인_요청하면_에러를_응답한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest("notExist@test.com", TEST_USER_PASSWORD);

        // when
        ResultActions actions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        actions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("이메일은 맞지만 비밀번호가 틀린 경우에는 401 에러를 응답한다.")
    void 비밀번호가_틀린_경우에는_에러를_응답한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest(TEST_USER_EMAIL, "incorrectPassword");

        // when
        ResultActions actions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        actions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("사용자는 유효한 토큰으로 보호된 API를 사용할 수 있다.")
    void 유효한토큰으로_보호된_API를_사용할_수_있다() throws Exception {
        // given
        LoginRequest request = new LoginRequest(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        ResultActions givenActions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        MvcResult mvcResult = givenActions.andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        LoginResponse loginResponse = objectMapper.readValue(jsonResponse, LoginResponse.class);
        String accessToken = loginResponse.token();

        // when
        ResultActions actions = mockMvc.perform(get("/api/members/me")
                .header("Authorization", "Bearer " + accessToken));

        // then
        Member savedMember = memberRepository.findByEmail(TEST_USER_EMAIL)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(savedMember.getId()))
                .andExpect(jsonPath("$.email").value(TEST_USER_EMAIL))
                .andExpect(jsonPath("$.nickname").value(TEST_USER_NICKNAME));
    }

    @Test
    @DisplayName("토큰 없이 보호된 API를 요청하면 401 에러를 응답한다.")
    void 토큰_없이_보호된_API를_요청하면_에러를_응답한다() throws Exception {
        // given
        String accessToken = "";

        // when
        ResultActions actions = mockMvc.perform(get("/api/members/me")
                .header("Authorization", "Bearer " + accessToken));

        // then
        actions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 보호된 API를 요청하면 401 에러를 응답한다.")
    void 유효하지_않은_토큰으로_보호된_API를_요청하면_에러를_응답한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        String invalidAccessToken = "invalidAccessToken";

        // when
        ResultActions actions = mockMvc.perform(get("/api/members/me")
                .header("Authorization", "Bearer " + invalidAccessToken));

        // then
        actions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("만료된 토큰으로 보호된 API를 요청하면 403 에러를 응답한다.")
    void 만료된_토큰으로_보호된_API를_요청하면_에러를_응답한다() throws Exception {
        // given
        Member member = memberRepository.findByEmail(TEST_USER_EMAIL)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));
        String expiredToken = generateExpiredToken(member.getId());

        // when
        ResultActions actions = mockMvc.perform(get("/api/members/me")
                .header("Authorization", "Bearer " + expiredToken));

        // then
        actions
                .andExpect(status().isUnauthorized());
    }

    // 만료된 토큰 관련 테스트를 위한 1초 만료된 토큰 생성 메서드
    private String generateExpiredToken(Long userId) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        long now = new Date().getTime();
        Date expiredDate = new Date(now - 1000);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .expiration(expiredDate)
                .signWith(key)
                .compact();
    }
}
