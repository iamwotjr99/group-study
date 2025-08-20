package com.jaeseok.groupStudy.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.jaeseok.groupStudy.user.domain.User;
import com.jaeseok.groupStudy.user.domain.UserRepository;
import com.jaeseok.groupStudy.user.domain.vo.Email;
import com.jaeseok.groupStudy.user.domain.vo.Nickname;
import com.jaeseok.groupStudy.user.domain.vo.Password;
import com.jaeseok.groupStudy.user.domain.vo.UserInfo;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@Import({UserRepositoryImpl.class})
@DisplayName("UserRepository 구현체 테스트")
class UserRepositoryImplTest {

    @Autowired
    UserRepository userRepository;

    PasswordEncoder encoder;

    Email email;
    Nickname nickname;

    String rawPassword;
    String encodedPassword;
    Password password;

    User user1;
    UserInfo userInfo1;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        email = new Email("test001@test.com");
        nickname = new Nickname("테스터001");
        rawPassword = "asd1234";
        encodedPassword = encoder.encode(rawPassword);
        password = new Password(encodedPassword);

        userInfo1 = new UserInfo(email, nickname, password);
        user1 = User.createUser(userInfo1);
    }

    @Test
    @DisplayName("User를 DB에 저장할 수 있다.")
    void givenUser_whenSave_thenSaveInDB() {
        // given
        User user = user1;

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getUserInfo()).isEqualTo(user.getUserInfo());
    }

    @Test
    @DisplayName("User를 DB에서 조회할 수 있다.")
    void givenUserId_whenFindById_thenReturnUser() {
        // given
        User savedUser = userRepository.save(user1);
        Long userId = savedUser.getId();

        // when
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // then
        assertThat(foundUser.getId()).isEqualTo(userId);
        assertThat(foundUser.getUserInfo()).isEqualTo(savedUser.getUserInfo());
    }

    @Test
    @DisplayName("특정 Nickname 값이 DB에 있는지 확인할 수 있다.")
    void givenNickname_whenExistNickname_thenReturnTrueOrFalse() {
        // given
        User save = userRepository.save(user1);

        User foundUser = userRepository.findById(save.getId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        String existNickname = foundUser.getUserInfoNickname();
        String notExistNickname = "존재x닉네임";

        // when
        boolean existed = userRepository.existByNickname(existNickname);
        boolean notExisted = userRepository.existByNickname(notExistNickname);

        // then
        assertThat(existed).isTrue();
        assertThat(notExisted).isFalse();
    }

    @Test
    @DisplayName("특정 Email 값이 DB에 있는지 확인할 수 있다.")
    void givenEmail_whenExistEmail_thenReturnTrueOrFalse() {
        // given
        User save = userRepository.save(user1);

        User foundUser = userRepository.findById(save.getId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        String existEmail = foundUser.getUserInfoEmail();
        String notExistEmail = "NotExistEmail@notexist.com";

        // when
        boolean existed = userRepository.existByEmail(existEmail);
        boolean notExisted = userRepository.existByEmail(notExistEmail);

        // then
        assertThat(existed).isTrue();
        assertThat(notExisted).isFalse();
    }
}