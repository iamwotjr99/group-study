package com.jaeseok.groupStudy.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.user.domain.User;
import com.jaeseok.groupStudy.user.domain.vo.Email;
import com.jaeseok.groupStudy.user.domain.vo.Nickname;
import com.jaeseok.groupStudy.user.domain.vo.Password;
import com.jaeseok.groupStudy.user.domain.vo.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
class JpaUserRepositoryTest {

    @Autowired
    JpaUserRepository jpaUserRepository;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    Email email = new Email("test001@test.com");
    Nickname nickname = new Nickname("테스터001");

    String rawPassword = "asd1234";
    String encodedPassword = encoder.encode(rawPassword);
    Password password = new Password(encodedPassword);

    UserInfo userInfo1;
    User user1;

    @BeforeEach
    void setUp() {
        userInfo1 = new UserInfo(email, nickname, password);
        user1 = User.createUser(userInfo1);
    }

    @Test
    @DisplayName("JPA를 통해 User를 저장할 수 있다.")
    void givenUserDomain_whenSave_thenSaveInDB() {
        // given
        User user = user1;
        UserEntity userEntity = UserEntity.fromDomain(user);

        // when
        UserEntity savedUserEntity = jpaUserRepository.save(userEntity);
        User savedUser = savedUserEntity.toDomain();

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserInfo()).isEqualTo(user.getUserInfo());
    }

    @Test
    @DisplayName("JPA를 통해 userId로 User를 조회할 수 있다.")
    void givenUserId_whenFindById_thenReturnUser() {
        // given
        UserEntity userEntity = UserEntity.fromDomain(user1);
        UserEntity savedUserEntity = jpaUserRepository.save(userEntity);
        User user = savedUserEntity.toDomain();
        Long userId = user.getId();

        // when
        UserEntity foundUserEntity = jpaUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        User foundUser = foundUserEntity.toDomain();

        // then
        assertThat(foundUser.getId()).isEqualTo(userId);
        assertThat(foundUser.getUserInfo()).isEqualTo(user.getUserInfo());
    }

    @Test
    @DisplayName("JPA를 통해 해당 nickname이 DB에 존재 유무를 알 수 있다.")
    void givenNickname_thenExistByNickname_thenReturnTrue() {
        // given
        UserEntity userEntity = UserEntity.fromDomain(user1);
        UserEntity savedUserEntity = jpaUserRepository.save(userEntity);
        User user = savedUserEntity.toDomain();

        String existNickname = user.getUserInfoNickname();
        String notExistNickname = "존재x닉네임";

        // when
        // then
        assertThat(jpaUserRepository.existsByUserInfoEntity_Nickname(existNickname)).isTrue();
        assertThat(jpaUserRepository.existsByUserInfoEntity_Nickname(notExistNickname)).isFalse();
    }
}