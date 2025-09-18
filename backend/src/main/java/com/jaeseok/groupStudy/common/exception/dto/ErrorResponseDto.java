package com.jaeseok.groupStudy.common.exception.dto;

public record ErrorResponseDto(
        int status,
        String message
) {

}
