package com.jaeseok.groupStudy.auth.application.dto;

import lombok.Builder;

@Builder
public record SignUpResponse(Long id, String message) {

}
