package com.jaeseok.groupStudy.auth.presentation;

import com.jaeseok.groupStudy.auth.application.AuthService;
import com.jaeseok.groupStudy.auth.application.dto.LoginInfo;
import com.jaeseok.groupStudy.auth.application.dto.LoginQuery;
import com.jaeseok.groupStudy.auth.application.dto.SignUpCommand;
import com.jaeseok.groupStudy.auth.application.dto.SignUpInfo;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.LoginResponse;
import com.jaeseok.groupStudy.auth.presentation.dto.SignUpRequest;
import com.jaeseok.groupStudy.auth.presentation.dto.SignUpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpCommand command = request.toCommand();

        SignUpInfo signUpInfo = authService.signUp(command);

        SignUpResponse response = SignUpResponse.from(signUpInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginQuery query = request.toQuery();

        LoginInfo loginInfo = authService.login(query);

        LoginResponse response = LoginResponse.from(loginInfo);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        boolean result = authService.checkEmailDuplicate(email);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean result = authService.checkNicknameDuplicate(nickname);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
