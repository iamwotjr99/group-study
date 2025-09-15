package com.jaeseok.groupStudy.unit.member.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.member.infrastructure.persistence.MemberRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
@Import({MemberRepositoryImpl.class})
@DisplayName("UserRepository 구현체 테스트")
class MemberRepositoryImplTest {

    @Autowired
    MemberRepository memberRepository;

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
    @DisplayName("User를 DB에 저장할 수 있다.")
    void givenUser_whenSave_thenSaveInDB() {
        // given
        Member member = member1;

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertThat(savedMember.getMemberInfo()).isEqualTo(member.getMemberInfo());
    }

    @Test
    @DisplayName("User를 DB에서 아이디로 조회할 수 있다.")
    void givenUserId_whenFindById_thenReturnUser() {
        // given
        Member savedMember = memberRepository.save(member1);
        Long userId = savedMember.getId();

        // when
        Member foundMember = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // then
        assertThat(foundMember.getId()).isEqualTo(userId);
        assertThat(foundMember.getMemberInfo()).isEqualTo(savedMember.getMemberInfo());
    }

    @Test
    @DisplayName("User를 DB에서 이메일로 조회할 수 있다.")
    void givenEmail_whenFindByEmail_thenReturnUser() {
        // given
        Member savedMember = memberRepository.save(member1);
        Long userId = savedMember.getId();
        String email = savedMember.getUserInfoEmail();

        // when
        Member foundMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        // then
        assertThat(foundMember.getId()).isEqualTo(userId);
        assertThat(foundMember.getMemberInfo()).isEqualTo(savedMember.getMemberInfo());
    }

    @Test
    @DisplayName("특정 Nickname 값이 DB에 있는지 확인할 수 있다.")
    void givenNickname_whenExistNickname_thenReturnTrueOrFalse() {
        // given
        Member save = memberRepository.save(member1);

        Member foundMember = memberRepository.findById(save.getId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        String existNickname = foundMember.getUserInfoNickname();
        String notExistNickname = "존재x닉네임";

        // when
        boolean existed = memberRepository.existByNickname(existNickname);
        boolean notExisted = memberRepository.existByNickname(notExistNickname);

        // then
        assertThat(existed).isTrue();
        assertThat(notExisted).isFalse();
    }

    @Test
    @DisplayName("특정 Email 값이 DB에 있는지 확인할 수 있다.")
    void givenEmail_whenExistEmail_thenReturnTrueOrFalse() {
        // given
        Member save = memberRepository.save(member1);

        Member foundMember = memberRepository.findById(save.getId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));

        String existEmail = foundMember.getUserInfoEmail();
        String notExistEmail = "NotExistEmail@notexist.com";

        // when
        boolean existed = memberRepository.existByEmail(existEmail);
        boolean notExisted = memberRepository.existByEmail(notExistEmail);

        // then
        assertThat(existed).isTrue();
        assertThat(notExisted).isFalse();
    }
}