package com.jaeseok.groupStudy.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.user.domain.User;
import com.jaeseok.groupStudy.user.domain.vo.Email;
import com.jaeseok.groupStudy.user.domain.vo.Nickname;
import com.jaeseok.groupStudy.user.domain.vo.Password;
import com.jaeseok.groupStudy.user.domain.vo.UserInfo;
import com.jaeseok.groupStudy.user.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@DisplayName("유저 JPA 레포지터리 테스트")
class JpaUserRepositoryTest {

    @Autowired
    JpaUserRepository jpaUserRepository;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    User user1;

    @BeforeEach
    void setUp() {
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        user1 = User.createUser(rawNickname, rawEmail, encodedPassword);
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
    @DisplayName("JPA를 통해 email로 User를 조회할 수 있다.")
    void givenEmail_whenFindByEmail_thenReturnUser() {
        // given
        UserEntity userEntity = UserEntity.fromDomain(user1);
        UserEntity savedUserEntity = jpaUserRepository.save(userEntity);
        User user = savedUserEntity.toDomain();
        String email = user.getUserInfoEmail();

        // when
        UserEntity foundUserEntity = jpaUserRepository.findByUserInfoEntity_Email(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        User foundUser = foundUserEntity.toDomain();

        // then
        assertThat(foundUser.getId()).isEqualTo(user.getId());
        assertThat(foundUser.getUserInfo()).isEqualTo(user.getUserInfo());
    }

    @Test
    @DisplayName("JPA를 통해 해당 nickname이 DB에 존재하는지 알 수 있다.")
    void givenNickname_whenExistByNickname_thenReturnTrueOrFalse() {
        // given
        UserEntity userEntity = UserEntity.fromDomain(user1);
        UserEntity savedUserEntity = jpaUserRepository.save(userEntity);
        User user = savedUserEntity.toDomain();

        String existNickname = user.getUserInfoNickname();
        String notExistNickname = "존재x닉네임";

        // when
        boolean existed = jpaUserRepository.existsByUserInfoEntity_Nickname(existNickname);
        boolean notExisted = jpaUserRepository.existsByUserInfoEntity_Nickname(notExistNickname);

        // then
        assertThat(existed).isTrue();
        assertThat(notExisted).isFalse();
    }

    @Test
    @DisplayName("JPA를 통해 해당 Email이 DB에 존재하는지 알 수 있다.")
    void givenEmail_whenExistByEmail_thenReturnTrueOrFalse() {
        // given
        UserEntity userEntity = UserEntity.fromDomain(user1);
        UserEntity savedUserEntity = jpaUserRepository.save(userEntity);
        User user = savedUserEntity.toDomain();

        String existEmail = user.getUserInfoEmail();
        String notExistEmail = "NotExistEmail@notexist.com";

        // when
        boolean existed = jpaUserRepository.existsByUserInfoEntity_Email(existEmail);
        boolean notExisted = jpaUserRepository.existsByUserInfoEntity_Email(notExistEmail);

        // then
        assertThat(existed).isTrue();
        assertThat(notExisted).isFalse();
    }
}