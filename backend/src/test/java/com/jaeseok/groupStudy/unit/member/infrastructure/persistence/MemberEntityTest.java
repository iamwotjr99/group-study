package com.jaeseok.groupStudy.unit.member.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.MemberEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@DisplayName("유저 영속성 객체 UserEntity 테스트")
class MemberEntityTest {

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

        Member member = Member.createMember(rawNickname, rawEmail, encodedPassword);

        MemberEntity memberEntity = MemberEntity.fromDomain(member);

        // when
        em.persist(memberEntity);
        em.flush();
        em.clear();

        MemberEntity found = em.find(MemberEntity.class, memberEntity.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(memberEntity.getId());
        assertThat(found.getMemberInfoEntity().getEmail()).isEqualTo(rawEmail);
        assertThat(found.getMemberInfoEntity().getNickname()).isEqualTo(rawNickname);

        assertThat(found.getMemberInfoEntity().getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("User -> UserEntity 매핑 테스트")
    void givenUser_whenFromDomain_thenMappedCorrectly() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        Member member = Member.createMember(rawNickname, rawEmail, encodedPassword);

        // when
        MemberEntity memberEntity = MemberEntity.fromDomain(member);

        // then
        assertThat(memberEntity.getMemberInfoEntity().getEmail()).isEqualTo(member.getUserInfoEmail());
        assertThat(memberEntity.getMemberInfoEntity().getNickname()).isEqualTo(member.getUserInfoNickname());
        assertThat(memberEntity.getMemberInfoEntity().getPassword()).isEqualTo(member.getUserInfoPassword());
    }

    @Test
    @DisplayName("UserEntity -> User 매핑 테스트")
    void givenUserEntity_whenToDomain_thenMappedCorrectly() {
        // given
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        Member member = Member.createMember(rawNickname, rawEmail, encodedPassword);

        MemberEntity memberEntity = MemberEntity.fromDomain(member);

        // when
        Member domain = memberEntity.toDomain();

        // then
        assertThat(domain.getUserInfoEmail()).isEqualTo(memberEntity.getMemberInfoEntity().getEmail());
        assertThat(domain.getUserInfoNickname()).isEqualTo(memberEntity.getMemberInfoEntity().getNickname());
        assertThat(domain.getUserInfoPassword()).isEqualTo(memberEntity.getMemberInfoEntity().getPassword());
    }
}