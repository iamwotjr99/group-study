package com.jaeseok.groupStudy.auth.application;

import com.jaeseok.groupStudy.auth.application.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.application.dto.LoginResponse;
import com.jaeseok.groupStudy.auth.application.dto.SignUpRequest;
import com.jaeseok.groupStudy.auth.application.dto.SignUpResponse;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest dto) {
        Member member = memberRepository.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (!member.checkPassword(dto.rawPassword(), passwordEncoder)) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return new LoginResponse("jwt token");
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest dto) {
        isDuplicatedEmail(dto.email());
        isDuplicatedNickname(dto.nickname());

        String encodedPassword = passwordEncoder.encode(dto.rawPassword());

        Member member = Member.createMember(dto.nickname(), dto.email(), encodedPassword);
        Member saved = memberRepository.save(member);

        return SignUpResponse.builder()
                .id(saved.getId())
                .message("회원가입 성공!")
                .build();
    }

    public void isDuplicatedEmail(String email) {
        if (memberRepository.existByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    public void isDuplicatedNickname(String nickname) {
        if (memberRepository.existByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

    }
}
