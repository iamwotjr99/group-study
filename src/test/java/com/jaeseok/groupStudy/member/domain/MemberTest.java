package com.jaeseok.groupStudy.member.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jaeseok.groupStudy.member.domain.vo.Email;
import com.jaeseok.groupStudy.member.domain.vo.Nickname;
import com.jaeseok.groupStudy.member.domain.vo.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("유저 도메인 테스트")
class MemberTest {

    PasswordEncoder encoder = new BCryptPasswordEncoder();

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
    void givenValidUserInfo_whencreateMember_thenReturnUser() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        // when
        Member member = Member.createMember(rawNickname, rawEmail, encodedPassword);

        // then
        assertThat(member.getUserInfoEmail()).isEqualTo(rawEmail);
        assertThat(member.getUserInfoNickname()).isEqualTo(rawNickname);
        assertThat(member.getUserInfoPassword()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("User는 자신의 비밀번호의 유효성을 체크할 수 있다.")
    void givenValidPassword_whenCheckPassword_thenReturnTrue() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        Member member = Member.createMember(rawNickname, rawEmail, encodedPassword);

        String givenPassword = "asd1234";

        // when
        boolean result = member.checkPassword(givenPassword, encoder);

        // then
        assertTrue(result);
    }


    @Test
    @DisplayName("비밀번호가 유효하지 않으면 false 반환")
    void givenInValidPassword_whenCheckPassword_thenReturnFalse() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        Member member = Member.createMember(rawNickname, rawEmail, encodedPassword);

        String givenPassword = "invalid_password";

        // when
        boolean result = member.checkPassword(givenPassword, encoder);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("User는 닉네임을 바꿀 수 있다.")
    void givenChangedNickname_whenChangeNickname_thenReturnCollectNickname() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        Member member = Member.createMember(rawNickname, rawEmail, encodedPassword);

        // when
        Member changedNicknameMember = member.changeNickname("닉네임변경001");

        // then
        assertEquals(new Nickname("닉네임변경001"), changedNicknameMember.getMemberInfo().nickname());
    }

    @Test
    @DisplayName("10자 이상 닉네임으로 변경하려고 할 시 예외")
    void given10LengthOverNickname_whenChangeNickname_thenThrowException() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        Member member = Member.createMember(rawNickname, rawEmail, encodedPassword);

        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> {
            member.changeNickname("닉네임변경테스터001");
        });
    }
}