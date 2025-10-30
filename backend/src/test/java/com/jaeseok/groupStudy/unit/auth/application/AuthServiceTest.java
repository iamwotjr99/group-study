package com.jaeseok.groupStudy.unit.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jaeseok.groupStudy.auth.application.AuthService;
import com.jaeseok.groupStudy.auth.application.TokenProvider;
import com.jaeseok.groupStudy.auth.application.dto.LoginInfo;
import com.jaeseok.groupStudy.auth.application.dto.LoginQuery;
import com.jaeseok.groupStudy.auth.application.dto.SignUpCommand;
import com.jaeseok.groupStudy.auth.application.dto.SignUpInfo;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginResponse;
import com.jaeseok.groupStudy.auth.presentation.dto.SignUpRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.SignUpResponse;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.member.domain.vo.MemberInfo;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    AuthService authService;

    @Mock
    TokenProvider tokenProvider;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 정보로 회원가입에 성공한다.")
    void givenSignUpRequest_whenSignUp_thenReturnSuccess() {
        // given
        SignUpRequest request = new SignUpRequest("testUser", "test@email.com", "test1234");

        Member savedMember = Member.from(1L,
                MemberInfo.of(request.nickname(), request.email(), request.rawPassword()));

        SignUpCommand command = request.toCommand();

        when(memberRepository.existByEmail(command.email())).thenReturn(false);
        when(memberRepository.existByNickname(command.nickname())).thenReturn(false);
        when(passwordEncoder.encode(command.rawPassword())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // when
        SignUpInfo signUpInfo = authService.signUp(command);
        SignUpResponse signUpResponse = SignUpResponse.from(signUpInfo);

        // then
        assertThat(signUpResponse.id()).isEqualTo(savedMember.getId());
        assertThat(signUpResponse.message()).isEqualTo("회원가입에 성공했습니다.");

        verify(memberRepository, times(1)).existByEmail(command.email());
        verify(memberRepository, times(1)).existByNickname(command.nickname());
        verify(passwordEncoder, times(1)).encode(command.rawPassword());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 시 이메일이 중복되면 예외를 발생시킨다.")
    void givenSignUpRequest_whenIsDuplicatedEmail_thenThrowException() {
        // given
        SignUpRequest request = new SignUpRequest("testUser", "test@email.com", "test1234");
        SignUpCommand command = request.toCommand();

        // when
        when(memberRepository.existByEmail(request.email())).thenReturn(true);
        assertThatThrownBy(() -> authService.signUp(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");

        // then
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 시 닉네임이 중복되면 예외를 발생시킨다.")
    void givenSignUpRequest_whenIsDuplicatedNickname_thenThrowException() {
        // given
        SignUpRequest request = new SignUpRequest("testUser", "test@email.com", "test1234");
        SignUpCommand command = request.toCommand();

        // when
        when(memberRepository.existByNickname(request.nickname())).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");

        // then
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("유효한 로그인 요청이 오면 로그인에 성공한다.")
    void givenLoginRequest_whenLogin_thenReturnLoginResponse() {
        // given
        LoginRequest request = new LoginRequest("test@email.com", "test1234");

        LoginQuery query = request.toQuery();

        Authentication mockAuthenticationObj = mock(Authentication.class);

        Member validMember = Member.createMember("test001", request.email(), request.rawPassword());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthenticationObj);
        when(tokenProvider.generateAccessToken(mockAuthenticationObj)).thenReturn("access-token");
        when(memberRepository.findByEmail(query.email())).thenReturn(Optional.of(validMember));

        // when
        LoginInfo loginInfo = authService.login(query);
        LoginResponse loginResponse = LoginResponse.from(loginInfo);

        // then
        assertThat(loginResponse.token()).isEqualTo("access-token");

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, times(1)).generateAccessToken(mockAuthenticationObj);
    }

    @Test
    @DisplayName("존재하지 않은 로그인 정보로 로그인 요청이 오면 예외를 발생시킨다.")
    void givenInValidLoginRequest_whenLogin_thenReturnException() {
        // given
        LoginRequest request = new LoginRequest("noExist@email.com", "test5678");

        LoginQuery query = request.toQuery();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("존재하지 않는 유저입니다."));

        // when
        // then
        assertThatThrownBy(() -> authService.login(query))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("존재하지 않는 유저입니다.");

        verify(tokenProvider, never()).generateAccessToken(any());
    }
}