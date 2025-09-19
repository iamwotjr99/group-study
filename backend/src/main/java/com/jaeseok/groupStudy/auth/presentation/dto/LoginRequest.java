package com.jaeseok.groupStudy.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeseok.groupStudy.auth.application.dto.LoginQuery;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효하지 않은 이메일입니다.")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @JsonProperty("password")
        String rawPassword
) {
        public LoginQuery toQuery() {
                return new LoginQuery(this.email, this.rawPassword);
        }
}
