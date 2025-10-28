package com.jaeseok.groupStudy.auth.application;

import com.jaeseok.groupStudy.auth.application.dto.LoginInfo;
import com.jaeseok.groupStudy.auth.application.dto.LoginQuery;
import com.jaeseok.groupStudy.auth.application.dto.SignUpCommand;
import com.jaeseok.groupStudy.auth.application.dto.SignUpInfo;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.persistence.RefreshTokenRepository;
import com.jaeseok.groupStudy.auth.infrastructure.jwt.persistence.entity.RefreshToken;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginResponse;
import com.jaeseok.groupStudy.member.domain.Member;
import com.jaeseok.groupStudy.member.domain.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginInfo login(LoginQuery query, HttpServletResponse response) {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                query.email(), query.rawPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        Instant refreshTokenExpiry = tokenProvider.getRefreshTokenExpiryAsInstant();

        Member member = memberRepository.findByEmail(query.email())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));

        refreshTokenRepository.findByMemberEntity_Id(member.getId())
                .ifPresentOrElse(
                        (existingToken) -> existingToken.updateToken(refreshToken, refreshTokenExpiry),
                        () -> refreshTokenRepository.save(new RefreshToken(member, refreshToken, refreshTokenExpiry))
                );

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(tokenProvider.getRefreshTokenValidityInMilliSec() / 1000)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new LoginInfo(accessToken, member.getId(), member.getUserInfoNickname());
    }

    @Transactional
    public SignUpInfo signUp(SignUpCommand cmd) {
        isDuplicatedEmail(cmd.email());
        isDuplicatedNickname(cmd.nickname());

        String encodedPassword = passwordEncoder.encode(cmd.rawPassword());

        Member member = Member.createMember(cmd.nickname(), cmd.email(), encodedPassword);
        Member saved = memberRepository.save(member);

        return new SignUpInfo(saved.getId());
    }

    // 중복 이메일 체크 API용 메서드
    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existByEmail(email);
    }

    // 중복 닉네임 체크 API용 메서드
    public boolean checkNicknameDuplicate(String nickname) {
        return memberRepository.existByNickname(nickname);
    }

    private void isDuplicatedEmail(String email) {
        if (memberRepository.existByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private void isDuplicatedNickname(String nickname) {
        if (memberRepository.existByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

    }
}
