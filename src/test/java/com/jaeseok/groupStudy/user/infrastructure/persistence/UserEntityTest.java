package com.jaeseok.groupStudy.user.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.user.domain.User;
import com.jaeseok.groupStudy.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@DisplayName("유저 영속성 객체 UserEntity 테스트")
class UserEntityTest {

    @Autowired
    EntityManager em;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("UserEntity 저장 및 조회 테스트")
    void givenUserEntity_whenSaveAndFound_thenEqual() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        User user = User.createUser(rawEmail, rawNickname, encodedPassword);

        UserEntity userEntity = UserEntity.fromDomain(user);

        // when
        em.persist(userEntity);
        em.flush();
        em.clear();

        UserEntity found = em.find(UserEntity.class, userEntity.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(userEntity.getId());
        assertThat(found.getUserInfoEntity().getEmail()).isEqualTo(rawEmail);
        assertThat(found.getUserInfoEntity().getNickname()).isEqualTo(rawNickname);

        assertThat(found.getUserInfoEntity().getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("User -> UserEntity 매핑 테스트")
    void givenUser_whenFromDomain_thenMappedCorrectly() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        User user = User.createUser(rawEmail, rawNickname, encodedPassword);

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
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        User user = User.createUser(rawEmail, rawNickname, encodedPassword);

        UserEntity userEntity = UserEntity.fromDomain(user);

        // when
        User domain = userEntity.toDomain();

        // then
        assertThat(domain.getUserInfoEmail()).isEqualTo(userEntity.getUserInfoEntity().getEmail());
        assertThat(domain.getUserInfoNickname()).isEqualTo(userEntity.getUserInfoEntity().getNickname());
        assertThat(domain.getUserInfoPassword()).isEqualTo(userEntity.getUserInfoEntity().getPassword());
    }
}