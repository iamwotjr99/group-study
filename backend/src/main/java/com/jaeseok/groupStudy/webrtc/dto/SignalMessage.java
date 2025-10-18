package com.jaeseok.groupStudy.webrtc.dto;

public record SignalMessage(String type, Object payload, Long senderId, Long receiverId) {

}
