package com.jaeseok.groupStudy.auth.application;

import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username;

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다."));

        return createMemberDetails(member);
    }

    private UserDetails createMemberDetails(Member member) {
        return User.builder()
                .username(String.valueOf(member.getId())) // 바뀌기 쉬운 이메일보다 바뀔 가능성이 없는 id로 name 설정
                .password(member.getUserInfoPassword())
                .authorities(Collections.emptyList())
                .build();
    }
}
