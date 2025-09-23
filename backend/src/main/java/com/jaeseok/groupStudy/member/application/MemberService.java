package com.jaeseok.groupStudy.member.application;

import com.jaeseok.groupStudy.member.application.dto.MemberInfoDto;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import com.jaeseok.groupStudy.member.exception.MemberNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberInfoDto getMemberInfo(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 유저입니다."));

        return MemberInfoDto.builder()
                .userId(member.getId())
                .email(member.getUserInfoEmail())
                .nickname(member.getUserInfoNickname())
                .build();
    }
}
