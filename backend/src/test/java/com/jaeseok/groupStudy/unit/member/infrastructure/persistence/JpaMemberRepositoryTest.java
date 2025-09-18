package com.jaeseok.groupStudy.unit.member.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.infrastructure.persistence.JpaMemberRepository;
import com.jaeseok.groupStudy.member.infrastructure.persistence.entity.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@DisplayName("유저 JPA 레포지터리 테스트")
class JpaMemberRepositoryTest {

    @Autowired
    JpaMemberRepository jpaMemberRepository;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    Member member1;

    @BeforeEach
    void setUp() {
        String rawEmail = "test001@test.com";
        String rawNickname = "테스터001";
        String encodedPassword = encoder.encode("asd1234");

        member1 = Member.createMember(rawNickname, rawEmail, encodedPassword);
    }

    @Test
    @DisplayName("JPA를 통해 User를 저장할 수 있다.")
    void givenUserDomain_whenSave_thenSaveInDB() {
        // given
        Member member = member1;
        MemberEntity memberEntity = MemberEntity.fromDomain(member);

        // when
        MemberEntity savedMemberEntity = jpaMemberRepository.save(memberEntity);
        Member savedMember = savedMemberEntity.toDomain();

        // then
        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getMemberInfo()).isEqualTo(member.getMemberInfo());
    }

    @Test
    @DisplayName("JPA를 통해 userId로 User를 조회할 수 있다.")
    void givenUserId_whenFindById_thenReturnUser() {
        // given
        MemberEntity memberEntity = MemberEntity.fromDomain(member1);
        MemberEntity savedMemberEntity = jpaMemberRepository.save(memberEntity);
        Member member = savedMemberEntity.toDomain();
        Long userId = member.getId();

        // when
        MemberEntity foundMemberEntity = jpaMemberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        Member foundMember = foundMemberEntity.toDomain();

        // then
        assertThat(foundMember.getId()).isEqualTo(userId);
        assertThat(foundMember.getMemberInfo()).isEqualTo(member.getMemberInfo());
    }

    @Test
    @DisplayName("JPA를 통해 email로 User를 조회할 수 있다.")
    void givenEmail_whenFindByEmail_thenReturnUser() {
        // given
        MemberEntity memberEntity = MemberEntity.fromDomain(member1);
        MemberEntity savedMemberEntity = jpaMemberRepository.save(memberEntity);
        Member member = savedMemberEntity.toDomain();
        String email = member.getUserInfoEmail();

        // when
        MemberEntity foundMemberEntity = jpaMemberRepository.findByMemberInfoEntity_Email(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Member foundMember = foundMemberEntity.toDomain();

        // then
        assertThat(foundMember.getId()).isEqualTo(member.getId());
        assertThat(foundMember.getMemberInfo()).isEqualTo(member.getMemberInfo());
    }

    @Test
    @DisplayName("JPA를 통해 해당 nickname이 DB에 존재하는지 알 수 있다.")
    void givenNickname_whenExistByNickname_thenReturnTrueOrFalse() {
        // given
        MemberEntity memberEntity = MemberEntity.fromDomain(member1);
        MemberEntity savedMemberEntity = jpaMemberRepository.save(memberEntity);
        Member member = savedMemberEntity.toDomain();

        String existNickname = member.getUserInfoNickname();
        String notExistNickname = "존재x닉네임";

        // when
        boolean existed = jpaMemberRepository.existsByMemberInfoEntity_Nickname(existNickname);
        boolean notExisted = jpaMemberRepository.existsByMemberInfoEntity_Nickname(notExistNickname);

        // then
        assertThat(existed).isTrue();
        assertThat(notExisted).isFalse();
    }

    @Test
    @DisplayName("JPA를 통해 해당 Email이 DB에 존재하는지 알 수 있다.")
    void givenEmail_whenExistByEmail_thenReturnTrueOrFalse() {
        // given
        MemberEntity memberEntity = MemberEntity.fromDomain(member1);
        MemberEntity savedMemberEntity = jpaMemberRepository.save(memberEntity);
        Member member = savedMemberEntity.toDomain();

        String existEmail = member.getUserInfoEmail();
        String notExistEmail = "NotExistEmail@notexist.com";

        // when
        boolean existed = jpaMemberRepository.existsByMemberInfoEntity_Email(existEmail);
        boolean notExisted = jpaMemberRepository.existsByMemberInfoEntity_Email(notExistEmail);

        // then
        assertThat(existed).isTrue();
        assertThat(notExisted).isFalse();
    }
}