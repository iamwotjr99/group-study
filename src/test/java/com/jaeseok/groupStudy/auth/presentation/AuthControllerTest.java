package com.jaeseok.groupStudy.auth.presentation;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaeseok.groupStudy.auth.application.AuthService;
import com.jaeseok.groupStudy.auth.application.dto.LoginInfo;
import com.jaeseok.groupStudy.auth.application.dto.LoginQuery;
import com.jaeseok.groupStudy.auth.application.dto.SignUpCommand;
import com.jaeseok.groupStudy.auth.application.dto.SignUpInfo;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.JwtTokenProvider;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.SignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @TestConfiguration
    static class MockConfig {

        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }

        @Bean
        public JwtTokenProvider jwtTokenProvider() {
            return Mockito.mock(JwtTokenProvider.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    @DisplayName("유효한 회원가입 요청이 오면 회원가입에 성공한다.")
    void givenSignUpRequest_whenSignUp_thenReturnSignUpSuccess() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("testuser", "test@test.com", "password1234");
        SignUpCommand cmd = request.toCommand();

        Long willCreatedMemberId = 1L;
        SignUpInfo signUpInfo = new SignUpInfo(willCreatedMemberId);
        given(authService.signUp(cmd)).willReturn(signUpInfo);

        // when
        ResultActions action = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        action
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(willCreatedMemberId))
                .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."));
    }

    @Test
    @DisplayName("유효한 로그인 요청이 오면 로그인에 성공한다.")
    void login() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password1234");
        LoginQuery query = request.toQuery();

        LoginInfo loginInfo = new LoginInfo("access-token");
        given(authService.login(query)).willReturn(loginInfo);

        // when
        ResultActions action = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        action
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.message").value("로그인 성공"));
    }


    @Test
    @DisplayName("이메일이 중복되는지 확인할 수 있다.")
    void checkEmailDuplicate() throws Exception {
        // given
        String email = "test@test.com";

        given(authService.checkEmailDuplicate(email)).willReturn(true);

        // when
        ResultActions action = mockMvc.perform(get("/api/auth/check-email")
                .param("email", email));

        // then
        action
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("닉네임이 중복되는지 확인할 수 있다.")
    void givenNickname_whenCheckNicknameDuplicate_thenStatusOKWithTrue() throws Exception {
        // given
        String nickname = "test_user";

        given(authService.checkNicknameDuplicate(nickname)).willReturn(true);

        // when
        ResultActions action = mockMvc.perform(get("/api/auth/check-nickname")
                .param("nickname", nickname));

        // then
        action
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

    }
}