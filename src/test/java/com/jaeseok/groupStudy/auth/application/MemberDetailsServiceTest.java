package com.jaeseok.groupStudy.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.member.domain.vo.MemberInfo;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberDetailsService 테스트")
class MemberDetailsServiceTest {

    @InjectMocks
    MemberDetailsService memberDetailsService;

    @Mock
    MemberRepository memberRepository;

    @Test
    @DisplayName("Email로 member를 DB에서 가져온다.")
    void givenMemberEmail_whenLoadByUsername_thenReturnMember() {
        // given
        String email = "test@email.com";

        Member savedMember = Member.from(1L,
                MemberInfo.of("testUser", email, "encodedTest1234"));

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(savedMember));

        // when
        UserDetails userDetails = memberDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails.getUsername()).isEqualTo(String.valueOf(savedMember.getId()));
        assertThat(userDetails.getPassword()).isEqualTo(savedMember.getUserInfoPassword());

        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Email로 member를 가져올 때 member가 없다면 예외를 발생시킨다.")
    void givenInvalidMemberEmail_whenLoadByUsername_thenThrowException() {
        // given
        String email = "noExist@email.com";

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> memberDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("존재하지 않는 유저입니다.");

        verify(memberRepository, times(1)).findByEmail(email);
    }
}