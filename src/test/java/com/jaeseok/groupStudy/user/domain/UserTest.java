package com.jaeseok.groupStudy.user.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jaeseok.groupStudy.user.domain.vo.Email;
import com.jaeseok.groupStudy.user.domain.vo.Nickname;
import com.jaeseok.groupStudy.user.domain.vo.Password;
import com.jaeseok.groupStudy.user.domain.vo.UserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserTest {

    Email email = new Email("test001@test.com");
    Nickname nickname = new Nickname("테스터001");
    String rawPassword = "asd1234";

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    String encodedPassword = encoder.encode(rawPassword);

    Password password = new Password(encodedPassword);

    @Test
    @DisplayName("이메일이 null, empty, length < 5, 유효하지 않은 포맷인 경우 예외")
    void givenInvalidEmail_whenNewEmail_thenThrowException() {
        // given & when & then
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일은 필수입니다.");
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일은 필수입니다.");
        assertThatThrownBy(() -> new Email("@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않는 이메일입니다.");
        assertThatThrownBy(() -> new Email("invalid#email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않는 이메일입니다.");
        assertThatThrownBy(() -> new Email("invalid@email#com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않는 이메일입니다.");
        assertThatThrownBy(() -> new Email("invalid@email.c"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않는 이메일입니다.");


    }

    @Test
    @DisplayName("비밀번호가 null, blank 이면 예외")
    void givenNullPassword_whenNewPassword_thenThrowException() {
        // given & when & then
        assertThatThrownBy(() -> new Password(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 필수입니다.");
        assertThatThrownBy(() -> new Password(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 필수입니다.");

    }

    @Test
    @DisplayName("닉네임이 null, blank, length < 5, length > 10 이면 예외")
    void givenInvalidNickname_whenNewNickname_thenThrowException() {
        // given & when & then
        assertThatThrownBy(() -> new Nickname(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 필수입니다.");
        assertThatThrownBy(() -> new Nickname(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 필수입니다.");
        assertThatThrownBy(() -> new Nickname("가"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임이 너무 짧습니다.");
        assertThatThrownBy(() -> new Nickname("이글자는10글자일까요"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 10자 이하로 설정해주세요");
    }

    @Test
    @DisplayName("User는 유효한 유저 정보가 들어오면 유저를 생성할 수 있다.")
    void givenValidUserInfo_whenCreateUser_thenReturnUser() {
        // given
        UserInfo userInfo = new UserInfo(email, nickname, password);

        // when
        User user = User.createUser(userInfo);

        // then
        assertThat(user.getUserInfo().nickname()).isEqualTo(nickname);
        assertThat(user.getUserInfo().email()).isEqualTo(email);
    }

    @Test
    @DisplayName("User는 자신의 비밀번호의 유효성을 체크할 수 있다.")
    void givenValidPassword_whenCheckPassword_thenReturnTrue() {
        // given
        UserInfo userInfo = new UserInfo(email, nickname, password);
        User user = User.createUser(userInfo);

        String givenPassword = "asd1234";

        // when
        boolean result = user.checkPassword(givenPassword, encoder);

        // then
        assertTrue(result);
    }


    @Test
    @DisplayName("비밀번호가 유효하지 않으면 false 반환")
    void givenInValidPassword_whenCheckPassword_thenReturnFalse() {
        // given
        UserInfo userInfo = new UserInfo(email, nickname, password);
        User user = User.createUser(userInfo);

        String givenPassword = "invalid_password";

        // when
        boolean result = user.checkPassword(givenPassword, encoder);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("User는 닉네임을 바꿀 수 있다.")
    void givenChangedNickname_whenChangeNickname_thenReturnCollectNickname() {
        // given
        UserInfo userInfo = new UserInfo(email, nickname, password);
        User user = User.createUser(userInfo);

        // when
        User changedNicknameUser = user.changeNickname("닉네임변경001");

        // then
        assertEquals(new Nickname("닉네임변경001"), changedNicknameUser.getUserInfo().nickname());
    }

    @Test
    @DisplayName("10자 이상 닉네임으로 변경하려고 할 시 예외")
    void given10LengthOverNickname_whenChangeNickname_thenThrowException() {
        // given
        UserInfo userInfo = new UserInfo(email, nickname, password);
        User user = User.createUser(userInfo);

        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> {
            user.changeNickname("닉네임변경테스터001");
        });
    }
}