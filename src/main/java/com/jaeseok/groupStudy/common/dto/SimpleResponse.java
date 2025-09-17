package com.jaeseok.groupStudy.common.dto;

public record SimpleResponse(String message)
{
    public static SimpleResponse of(String message) {
        return new SimpleResponse(message);
    }
}
