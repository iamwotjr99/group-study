package com.jaeseok.groupStudy.webrtc.application;

public record SignalCommand(
        Long roomId,
        Long senderId,
        Long receiverId
) {

}
