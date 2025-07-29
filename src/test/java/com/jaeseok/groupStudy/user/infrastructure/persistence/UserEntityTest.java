package com.jaeseok.groupStudy.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.user.domain.User;
import com.jaeseok.groupStudy.user.domain.vo.Email;
import com.jaeseok.groupStudy.user.domain.vo.Nickname;
import com.jaeseok.groupStudy.user.domain.vo.Password;
import com.jaeseok.groupStudy.user.domain.vo.UserInfo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
class UserEntityTest {

    @Autowired
    private EntityManager em;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    Email email = new Email("test001@test.com");
    Nickname nickname = new Nickname("테스터001");

    String rawPassword = "asd1234";
    String encodedPassword = encoder.encode(rawPassword);
    Password password = new Password(encodedPassword);

    @Test
    @DisplayName("UserEntity 저장 및 조회 테스트")
    void givenUserEntity_whenSaveAndFound_thenEqual() {
        // given
        UserInfoEntity userInfoEntity = new UserInfoEntity("test1@test.est", "nickname", "pw");
        UserEntity userEntity = new UserEntity(null, userInfoEntity);

        // when
        em.persist(userEntity);
        em.flush();
        em.clear();

        UserEntity found = em.find(UserEntity.class, userEntity.id);

        // then
        assertThat(found).isNotNull();
        assertThat(found.id).isEqualTo(userEntity.id);
        assertThat(found.getUserInfoEntity().getEmail()).isEqualTo("test1@test.est");
        assertThat(found.getUserInfoEntity().getNickname()).isEqualTo("nickname");
        assertThat(found.getUserInfoEntity().getPassword()).isEqualTo("pw");
    }

    @Test
    @DisplayName("User -> UserEntity 매핑 테스트")
    void givenUser_whenFromDomain_thenMappedCorrectly() {
        // given
        UserInfo userInfo = new UserInfo(email, nickname, password);
        User user = User.createUser(userInfo);

        // when
        UserEntity userEntity = UserEntity.fromDomain(user);

        // then
        assertThat(userEntity.getUserInfoEntity().getEmail()).isEqualTo(user.getUserInfoEmail());
        assertThat(userEntity.getUserInfoEntity().getNickname()).isEqualTo(user.getUserInfoNickname());
        assertThat(userEntity.getUserInfoEntity().getPassword()).isEqualTo(user.getUserInfoPassword());
    }

    @Test
    @DisplayName("UserEntity -> User 매핑 테스트")
    void givenUserEntity_whenToDomain_thenMappedCorrectly() {
        // given
        UserInfoEntity userInfoEntity = new UserInfoEntity("test1@test.est", "nickname", "pw");
        UserEntity userEntity = new UserEntity(null, userInfoEntity);

        // when
        User domain = userEntity.toDomain();

        // then
        assertThat(domain.getUserInfoEmail()).isEqualTo(userEntity.getUserInfoEntity().getEmail());
        assertThat(domain.getUserInfoNickname()).isEqualTo(userEntity.getUserInfoEntity().getNickname());
        assertThat(domain.getUserInfoPassword()).isEqualTo(userEntity.getUserInfoEntity().getPassword());
    }
}