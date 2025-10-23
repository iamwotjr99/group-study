package com.jaeseok.groupStudy.webrtc.dto;

record SignalPayload(
        // Offer 또는 Answer일 경우 사용될 필드 (RTCSessionDescriptionInit 객체 구조와 유사)
        String type, // 'offer' or 'answer'
        String sdp,

        // ICE Candidate일 경우 사용될 필드 (RTCIceCandidateInit 객체 구조와 유사)
        String candidate,
        String sdpMid,
        Integer sdpMLineIndex
) {}

// 메인 SignalMessage DTO는 SignalPayload를 참조
public record SignalMessage(
        String type, // "offer", "answer", "iceCandidate" 구분자 역할은 유지
        SignalPayload payload, // 구체적인 Payload 객체 사용
        Long senderId,
        Long receiverId
) {
    public static SignalMessage of(String type, String sdp, String candidate, String spdMid, Integer spMI, Long senderId, Long receiverId) {
        SignalPayload signalPayload = new SignalPayload(type, sdp, candidate, spdMid, spMI);
        return new SignalMessage(type, signalPayload, senderId, receiverId);
    }
}
