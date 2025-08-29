package com.jaeseok.groupStudy.auth.application.dto;

public record LoginQuery(
        String email,
        String rawPassword
) {
}
