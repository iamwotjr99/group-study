package com.jaeseok.groupStudy.auth.application.dto;

public record SignUpCommand(
        String nickname,
        String email,
        String rawPassword
) {

}
