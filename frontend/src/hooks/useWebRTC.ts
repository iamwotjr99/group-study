import { Client } from "@stomp/stompjs";
import { useCallback, useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";

const STUN_SEVER = "stun:stun.l.google.com:19302";

interface PeerConnectionMap {
  [userId: number]: RTCPeerConnection;
}

interface MediaStreamMap {
  [userId: number]: MediaStream;
}

export const useWebRTC = (
  roomId: string | undefined,
  memberId: number | undefined
) => {
  const [localStream, setLocalStream] = useState<MediaStream | null>(null);
  const [remoteStream, setRemoteStream] = useState<MediaStreamMap>({});
  const peerConnectionRefs = useRef<PeerConnectionMap>({});
  const stompClientRef = useRef<Client | null>(null);

  // 시그널링 메시지 발송 함수
  const sendSignal = useCallback((signal: any) => {
    if (stompClientRef.current?.connected) {
      stompClientRef.current.publish({
        destination: `/pub/signal/${roomId}`,
        body: JSON.stringify(signal),
      });
    }
  }, []);

  // RTCPeerConnection 생성 및 설정 함수
  const createPeerConnection = useCallback(
    (targerUserId: number): RTCPeerConnection => {
      const pc = new RTCPeerConnection({
        iceServers: [{ urls: STUN_SEVER }],
      });

      // ICE Candidate 생성 시 시그널링 서버로 전송
      pc.onicecandidate = (event) => {
        if (event.candidate) {
          console.log("ICE Candidate Event: ", event);
          sendSignal({
            type: "iceCandidate",
            candidate: event.candidate,
            targerUserId,
            senderUserId: memberId,
          });
        }
      };

      // 상대방 스트림 수신 시 업데이트
      pc.ontrack = (event) => {
        console.log("Remote Stream Data: ", event);
        setRemoteStream((prev) => ({
          ...prev,
          [targerUserId]: event.streams[0],
        }));
      };

      // 로컬 스트림의 트랙들을 PeerConnection에 추가 (상대방에게 전송)
      localStream?.getTracks().forEach((track) => {
        pc.addTrack(track, localStream);
      });

      peerConnectionRefs.current[targerUserId] = pc;

      return pc;
    },
    [localStream, memberId, sendSignal]
  );

  // 수신된 시그널 처리 함수
  const handleSignal = useCallback(
    async (signal: any) => {
      const { type, sdp, candidate, senderUserId } = signal;
      const pc =
        peerConnectionRefs.current[senderUserId] ||
        createPeerConnection(senderUserId);

      try {
        if (type === "offer") {
          console.log("Offer: ", { type, sdp, candidate, senderUserId });
          await pc.setRemoteDescription(
            new RTCSessionDescription({ type, sdp })
          );
          const answer = await pc.createAnswer();
          await pc.setLocalDescription(answer);
          sendSignal({
            type: "answer",
            sdp: answer.sdp,
            targerUserId: senderUserId,
            senderUserId: memberId,
          });
        } else if (type === "answer") {
          console.log("Answer: ", { type, sdp, candidate, senderUserId });
          await pc.setRemoteDescription(
            new RTCSessionDescription({ type, sdp })
          );
        } else if (type === "iceCandidate") {
          console.log("iceCandidate: ", { type, sdp, candidate, senderUserId });
          await pc.addIceCandidate(new RTCIceCandidate(candidate));
        }
      } catch (err) {
        console.error("수신된 시그널 처리 중 오류: ", err);
      }
    },
    [createPeerConnection, memberId, sendSignal]
  );

  // 새로운 참여자에게 연결 시작 함수
  const connectToPeer = useCallback(
    async (targerUserId: number) => {
      if (targerUserId === memberId || peerConnectionRefs.current[targerUserId])
        return;

      const pc = createPeerConnection(targerUserId);
      const offer = await pc.createOffer();
      await pc.setLocalDescription(offer);
      sendSignal({
        type: "offer",
        sdp: offer.sdp,
        targerUserId,
        senderUserId: memberId,
      });
      console.log(`Offer 전송 to ${targerUserId}`);
    },
    [createPeerConnection, memberId, sendSignal]
  );

  // 내 미디어 가져오기 및 시그널링 연결
  useEffect(() => {
    if (!roomId || !memberId) return;

    const accessToken = localStorage.getItem("accessToken");

    // --- 1. 내 미디어 스트림 가져오기 ---
    navigator.mediaDevices
      .getUserMedia({ video: true, audio: true })
      .then((stream) => {
        console.log("Local Stream(Video/Audio): ", stream);
        setLocalStream(stream);
      })
      .catch((err) => {
        console.error("미디어 장치 접근 실패: ", err);
      });

    // --- 2. 시그널링 서버(STOMP) 연결 ---
    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8080/ws/chat"),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        console.log("STOMP Connect Success For WebRTC");
        stompClientRef.current = client;

        client.subscribe(`/sub/signal/user/${memberId}`, (message) => {
          const signal = JSON.stringify(message);
          console.log("/sub/signal/user/", signal);
          handleSignal(signal);
        });
      },
    });

    client.activate();

    // 컴포넌트 언마운트시 정리
    return () => {
      // 로컬 스트림 중지
      localStream?.getTracks().forEach((track) => track.stop());
      // 피어 연결 종료
      Object.values(peerConnectionRefs.current).forEach((pc) => pc.close());
      // 웹소켓 연결 해제
      client?.deactivate();
    };
  }, [roomId, memberId, localStream]);

  return { localStream, remoteStream, connectToPeer };
};
