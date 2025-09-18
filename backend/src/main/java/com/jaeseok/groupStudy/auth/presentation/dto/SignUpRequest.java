package com.jaeseok.groupStudy.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jaeseok.groupStudy.auth.application.dto.SignUpCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
        @JsonProperty("nickname")
        String nickname,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효하지 않은 이메일입니다.")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @JsonProperty("password")
        String rawPassword)
{
        public SignUpCommand toCommand() {
                return new SignUpCommand(this.nickname, this.email, this.rawPassword);
        }
}
