package com.jaeseok.groupStudy.auth.application;

import com.jaeseok.groupStudy.auth.domain.UserPrincipal;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoadUserPrincipalServiceImpl implements LoadUserPrincipalService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));

        return new UserPrincipal(member.getId(), member.getUserInfoEmail(), member.getUserInfoPassword());
    }
}
