package com.jaeseok.groupStudy.auth.application;

import com.jaeseok.groupStudy.auth.application.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.application.dto.LoginResponse;
import com.jaeseok.groupStudy.auth.application.dto.SignUpRequest;
import com.jaeseok.groupStudy.auth.application.dto.SignUpResponse;
import com.jaeseok.groupStudy.user.domain.User;
import com.jaeseok.groupStudy.user.domain.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (!user.checkPassword(dto.rawPassword(), passwordEncoder)) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return new LoginResponse("jwt token");
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest dto) {
        isDuplicatedEmail(dto.email());
        isDuplicatedNickname(dto.nickname());

        String encodedPassword = passwordEncoder.encode(dto.rawPassword());

        User user = User.createUser(dto.nickname(), dto.email(), encodedPassword);
        User saved = userRepository.save(user);

        return SignUpResponse.builder()
                .id(saved.getId())
                .message("회원가입 성공!")
                .build();
    }

    public void isDuplicatedEmail(String email) {
        if (userRepository.existByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    public void isDuplicatedNickname(String nickname) {
        if (userRepository.existByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

    }
}
