import { Client } from "@stomp/stompjs";
import { useCallback, useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import type { SignalMessage } from "../types/webRTC";

const STUN_SERVER = "stun:stun.l.google.com:19302";
const WEBSOCKET_URL = "http://localhost:8080/ws/chat";

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
  // ✅ 로컬 스트림 (Ref: WebRTC 로직용 최신 값)
  const localStreamRef = useRef<MediaStream | null>(null);
  // ✅ UI용 상태 (useState: 리렌더링 유발)
  const [displayLocalStream, setDisplayLocalStream] =
    useState<MediaStream | null>(null);

  const [remoteStream, setRemoteStream] = useState<MediaStreamMap>({});
  const peerConnectionRefs = useRef<PeerConnectionMap>({});
  const stompClientRef = useRef<Client | null>(null);
  const iceCandidateQueueRef = useRef<{ [userId: number]: RTCIceCandidate[] }>(
    {}
  );

  const [isMediaReady, setIsMediaReady] = useState(false);

  // ✅ 타이밍 문제로 Offer를 놓친 피어 ID 목록 (재시도 플래그)
  const [pendingOfferIds, setPendingOfferIds] = useState<Set<number>>(
    new Set()
  );

  const [isCoolingDown, setIsCoolingDown] = useState(false); // ✅ Cooldown 상태

  // 1. 시그널링 메시지 발송 함수 (변경 없음)
  const sendSignal = useCallback(
    (signalData: {
      type: "offer" | "answer" | "iceCandidate";
      payload: any;
      receiverId: number;
    }) => {
      if (stompClientRef.current?.connected && memberId) {
        const signalToSend: SignalMessage = {
          type: signalData.type,
          payload: signalData.payload,
          senderId: memberId,
          receiverId: signalData.receiverId,
        };
        stompClientRef.current.publish({
          destination: `/pub/signal/${roomId}`,
          body: JSON.stringify(signalToSend),
        });
        console.log("Sent Signal:", signalToSend);
      }
    },
    [memberId, roomId]
  );

  // 2. RTCPeerConnection 생성 및 설정 함수 (변경 없음)
  // src/hooks/useWebRTC.ts

  // 2. RTCPeerConnection 생성 및 설정 함수 (쿨다운 로직 제거됨)
  const createPeerConnection = useCallback(
    (targetUserId: number, streamToAdd: MediaStream): RTCPeerConnection => {
      console.log(
        `[WebRTC DEBUG] CREATING NEW PeerConnection for ${targetUserId}`
      );

      // 기존 연결이 있다면 닫고 새로 만듦 (재연결 대비)
      if (peerConnectionRefs.current[targetUserId]) {
        peerConnectionRefs.current[targetUserId].close();
      }

      const pc = new RTCPeerConnection({
        iceServers: [{ urls: STUN_SERVER }],
      });

      // ICE Candidate 생성 시그널 전송
      pc.onicecandidate = (event) => {
        if (event.candidate) {
          sendSignal({
            type: "iceCandidate",
            payload: {
              candidate: event.candidate.candidate,
              sdpMid: event.candidate.sdpMid,
              sdpMLineIndex: event.candidate.sdpMLineIndex,
            },
            receiverId: targetUserId,
          });
        }
      };

      // 상대방 스트림 수신 처리 (하이브리드 방식)
      pc.ontrack = (event) => {
        console.log(`[WebRTC] 상대방 트랙 수신: ${event.track.kind}`);

        if (event.streams && event.streams[0]) {
          console.log(
            `[WebRTC] ${targetUserId}번 유저의 전체 스트림 수신 (event.streams[0])`
          );
          setRemoteStream((prevMap) => ({
            ...prevMap,
            [targetUserId]: event.streams[0],
          }));
          return;
        }

        console.warn(
          `[WebRTC] event.streams[0] 없음. ${targetUserId}번 유저 트랙 수동 누적.`
        );
        setRemoteStream((prevMap) => {
          const newMap = { ...prevMap };
          let stream = newMap[targetUserId];

          if (!stream) {
            stream = new MediaStream();
            newMap[targetUserId] = stream;
            console.log(
              `[WebRTC] ${targetUserId}번 유저를 위한 새 MediaStream 생성 (수동 누적용)`
            );
          }
          stream.addTrack(event.track);
          return newMap;
        });
      };

      // 로컬 스트림 트랙 추가 (필수)
      streamToAdd.getTracks().forEach((track) => {
        pc.addTrack(track, streamToAdd);
      });

      // 💡 [수정됨] 연결 상태 로깅 (Stale Handler 방지)
      pc.onconnectionstatechange = () => {
        console.log(
          `[WebRTC] PC state (${targetUserId}): ${pc.connectionState}`
        );
        if (
          pc.connectionState === "disconnected" ||
          pc.connectionState === "failed"
        ) {
          console.log(
            `[WebRTC] Disconnected/Failed: Peer ${targetUserId} closed.`
          );

          // 💡 [핵심 수정]
          // 현재 ref map에 있는 PC가 '나(pc)' 자신일 때만 상태(Stream)와 ref를 삭제합니다.
          // 롤백으로 인해 닫힌 'Stale' 핸들러가
          // 새로 생성된 PC(PC_A2)를 삭제하는 것을 방지합니다.
          if (peerConnectionRefs.current[targetUserId] === pc) {
            console.log(
              `[WebRTC] Cleaning up 'active' PeerConnection ref for ${targetUserId}.`
            );
            setRemoteStream((prev) => {
              const newState = { ...prev };
              delete newState[targetUserId];
              return newState;
            });
            delete peerConnectionRefs.current[targetUserId];
          } else {
            console.warn(
              `[WebRTC] 'Stale' PeerConnection (${targetUserId}) state change detected. Ignoring cleanup.`
            );
          }
        }
      };

      peerConnectionRefs.current[targetUserId] = pc;
      return pc;
    },
    // 💡 [수정됨] 의존성 배열에 'setRemoteStream' 추가
    [sendSignal, setRemoteStream]
  );

  // 3. 수신된 시그널 처리 함수 (로컬 스트림 준비 보장)
  const handleSignal = useCallback(
    async (signal: SignalMessage) => {
      const { type, payload, senderId } = signal;

      // --- 1. 로컬 스트림 미준비 시 Offer/Answer 무시 (ICE는 통과) ---
      const currentLocalStream = localStreamRef.current;
      if (!currentLocalStream && (type === "offer" || type === "answer")) {
        console.warn(
          `[WebRTC] Local stream not ready. Ignoring signal type: ${type}`
        );
        return;
      }

      // --- 2. ICE Candidate 수신 처리 (가장 먼저 처리) ---
      // 💡 [핵심 수정]
      // 'pc'가 있든 없든, 'iceCandidate'는 먼저 큐에 저장합니다.
      // 롤백(Rollback) 중에 'pc'가 일시적으로 undefined여도 캔디데이트가 유실되는 것을 방지합니다.
      if (type === "iceCandidate") {
        if (payload && payload.candidate) {
          const candidate = new RTCIceCandidate({
            candidate: payload.candidate,
            sdpMid: payload.sdpMid,
            sdpMLineIndex: payload.sdpMLineIndex,
          });

          // 큐가 없으면 생성
          iceCandidateQueueRef.current[senderId] =
            iceCandidateQueueRef.current[senderId] || [];

          iceCandidateQueueRef.current[senderId].push(candidate);
          console.log(
            `[WebRTC] Queued ICE candidate from ${senderId}. (Queue size: ${iceCandidateQueueRef.current[senderId].length})`
          );
        }

        // 'pc'와 'remoteDescription'이 이미 준비되었다면 큐를 즉시 처리 시도
        // (준비 안됐으면 Offer/Answer 핸들러가 나중에 처리할 것임)
        const pc = peerConnectionRefs.current[senderId];
        if (pc && pc.remoteDescription && pc.signalingState !== "closed") {
          // (아래 'processIceQueue' 함수 로직과 동일)
          while (iceCandidateQueueRef.current[senderId]?.length > 0) {
            const queuedCandidate =
              iceCandidateQueueRef.current[senderId].shift();
            if (queuedCandidate) {
              console.log(
                `[WebRTC] Adding queued ICE candidate from ${senderId} (Immediate)`
              );
              await pc.addIceCandidate(queuedCandidate);
            }
          }
        }
        return; // ICE 처리는 여기서 종료
      }

      // --- 유틸리티: ICE 큐 처리 함수 ---
      const processIceQueue = async (
        pc: RTCPeerConnection,
        targetId: number
      ) => {
        const queue = iceCandidateQueueRef.current[targetId];
        if (
          pc &&
          pc.remoteDescription &&
          pc.signalingState !== "closed" &&
          queue
        ) {
          console.log(
            `[WebRTC] Processing ${queue.length} queued ICE candidates for ${targetId}.`
          );
          while (queue.length > 0) {
            const candidate = queue.shift();
            if (candidate) {
              console.log(`[WebRTC] Adding queued candidate from ${targetId}`);
              await pc.addIceCandidate(candidate);
            }
          }
        }
      };

      // --- 3. Offer / Answer 수신 처리 ---
      let pc: RTCPeerConnection | undefined =
        peerConnectionRefs.current[senderId];

      try {
        if (type === "offer") {
          // 역할 충돌(Role Conflict) 감지 및 롤백
          if (pc && pc.signalingState === "have-local-offer") {
            console.warn(
              `[WebRTC] Role Conflict detected! Received Offer from ${senderId} while in have-local-offer state. Performing Rollback.`
            );
            pc.close(); // 기존 PC 닫기
            delete peerConnectionRefs.current[senderId]; // ref에서 삭제
            pc = undefined; // pc 변수 초기화
          }

          // PeerConnection이 없으면 로컬 스트림을 전달하여 새로 생성
          if (!pc) {
            pc = createPeerConnection(
              senderId,
              currentLocalStream as MediaStream
            );
          }

          await pc.setRemoteDescription(
            new RTCSessionDescription({
              type: payload.type as RTCSdpType,
              sdp: payload.sdp,
            })
          );

          const answer = await pc.createAnswer();
          await pc.setLocalDescription(answer);

          sendSignal({
            type: "answer",
            payload: { type: answer.type, sdp: answer.sdp },
            receiverId: senderId,
          });

          // 💡 [수정] Offer/Answer 교환 완료 후 큐 처리
          await processIceQueue(pc, senderId);
        } else if (pc && type === "answer" && payload.sdp) {
          // Answer 수신 시
          if (pc.signalingState === "have-local-offer") {
            await pc.setRemoteDescription(
              new RTCSessionDescription({
                type: payload.type as RTCSdpType,
                sdp: payload.sdp,
              })
            );

            // Answer를 성공적으로 받은 후, pendingOfferIds에서 제거
            if (pendingOfferIds.has(senderId)) {
              setPendingOfferIds((prev) => {
                const newSet = new Set(prev);
                newSet.delete(senderId);
                return newSet;
              });
              console.warn(
                `[WebRTC] Successfully received Answer from ${senderId}. Removed from pending queue.`
              );
            }

            // 💡 [수정] Offer/Answer 교환 완료 후 큐 처리
            await processIceQueue(pc, senderId);
          }
        }
        // 'iceCandidate' 처리는 이미 위에서 끝났음
      } catch (err) {
        console.error(
          "수신된 시그널 처리 중 치명적인 오류 발생. PC 정리 시작:",
          err
        );
        if (pc) {
          console.warn(
            `[WebRTC] Forcing closure of PC for ${senderId} due to critical signal error.`
          );
          pc.close();
          delete peerConnectionRefs.current[senderId];
          setRemoteStream((prev) => {
            const newState = { ...prev };
            delete newState[senderId];
            return newState;
          });
        }
        // Cooldown 시작
        console.warn(`[WebRTC] Starting 3-second Cooldown via catch block.`);
        setIsCoolingDown(true);
        setTimeout(() => {
          setIsCoolingDown(false);
        }, 3000);
      }
    },
    [
      createPeerConnection,
      sendSignal,
      setPendingOfferIds,
      pendingOfferIds,
      setRemoteStream, // 💡 [추가] setRemoteStream 의존성
    ]
  );

  // 4. 새로운 참여자에게 연결 시작 (Offer 생성) 함수
  const connectToPeer = useCallback(
    async (targetUserId: number) => {
      if (isCoolingDown) {
        // 👈 이 로직이 최신 state를 참조
        console.log(
          `[WebRTC] Cooldown in progress. Skipping Offer to ${targetUserId}.`
        );
        return;
      }
      const currentLocalStream = localStreamRef.current;
      if (!currentLocalStream) {
        console.warn("[WebRTC] Local stream not ready. Cannot send Offer.");
        return;
      }

      // 롤백이 아닌, 일반적인 상황에서
      // 이미 연결이 진행중(connecting)이거나 완료(connected)된 PC는 닫지 않는다.
      if (peerConnectionRefs.current[targetUserId]) {
        const existingPc = peerConnectionRefs.current[targetUserId];
        if (
          existingPc.connectionState === "connecting" ||
          existingPc.connectionState === "connected"
        ) {
          console.log(
            `[WebRTC] PC for ${targetUserId} is already ${existingPc.connectionState}. Skipping connectToPeer.`
          );
          return;
        }

        // 'failed', 'new', 'disconnected' 상태는 닫고 새로 만든다.
        console.warn(
          `[WebRTC] Closing existing '${existingPc.connectionState}' PC for ${targetUserId} to reconnect.`
        );
        existingPc.close();
        delete peerConnectionRefs.current[targetUserId];
      }

      const pc = createPeerConnection(targetUserId, currentLocalStream);
      const offer = await pc.createOffer();

      // 💡 [핵심 수정]
      // setLocalDescription을 호출하기 전에 'signalingState'를 확인합니다.
      // 'stable'이 아니면 (즉, handleSignal이 이미 offer를 처리 중이면)
      // 이 함수(Offerer)는 물러나고 handleSignal(Answerer)에게 양보합니다.
      if (pc.signalingState !== "stable") {
        console.warn(
          `[WebRTC] Race condition detected in connectToPeer for ${targetUserId}. ` +
            `Signaling state is '${pc.signalingState}', not 'stable'. ` +
            `Aborting local offer, letting handleSignal take over.`
        );
        return;
      }

      await pc.setLocalDescription(offer);

      sendSignal({
        type: "offer",
        payload: { type: offer.type, sdp: offer.sdp },
        receiverId: targetUserId,
      });
      console.log(`Offer 전송 to ${targetUserId}`);
    },
    [
      createPeerConnection,
      memberId,
      sendSignal,
      setPendingOfferIds,
      isCoolingDown, // 💡 [수정] 의존성 추가
    ]
  );

  // 5. Force Offer 함수 (변경 없음)
  const connectToPeerForceOffer = useCallback(
    async (targetUserId: number) => {
      if (isCoolingDown) {
        console.log(
          `[WebRTC] Cooldown in progress. Skipping Force Offer to ${targetUserId}.`
        );
        return;
      }
      const currentLocalStream = localStreamRef.current;
      if (!currentLocalStream) return;
      if (targetUserId === memberId) return;

      if (peerConnectionRefs.current[targetUserId]) {
        console.warn(
          `[WebRTC] Cleaning up existing (stale?) PC for ${targetUserId} before Force Offer.`
        );
        peerConnectionRefs.current[targetUserId].close();
        delete peerConnectionRefs.current[targetUserId];
      }

      console.warn(
        `[WebRTC] FORCE OFFER to ${targetUserId} due to previous failure.`
      );

      const pc = createPeerConnection(targetUserId, currentLocalStream);
      const offer = await pc.createOffer();

      // 💡 [핵심 수정]
      // setLocalDescription을 호출하기 전에 'signalingState'를 확인합니다.
      if (pc.signalingState !== "stable") {
        console.warn(
          `[WebRTC] Race condition detected in connectToPeerForceOffer for ${targetUserId}. ` +
            `Signaling state is '${pc.signalingState}', not 'stable'. ` +
            `Aborting local offer, letting handleSignal take over.`
        );
        return;
      }

      await pc.setLocalDescription(offer);

      sendSignal({
        type: "offer",
        payload: { type: offer.type, sdp: offer.sdp },
        receiverId: targetUserId,
      });
      console.log(`Force Offer 전송 to ${targetUserId}`);
    },
    [createPeerConnection, memberId, sendSignal, isCoolingDown]
  );

  // 6. 미디어 및 STOMP 연결 로직 (초기화)
  useEffect(() => {
    if (!roomId || !memberId) return;

    // --- 1. 내 미디어 스트림 가져오기 ---
    navigator.mediaDevices
      .getUserMedia({ video: true, audio: true })
      .then((stream) => {
        localStreamRef.current = stream;
        setDisplayLocalStream(stream);
        setIsMediaReady(true);
        console.log("[WebRTC] Local media stream ready.");
      })
      .catch((err) => {
        console.error("미디어 장치 접근 실패: ", err);
      });

    // --- 2. 시그널링 서버(STOMP) 연결 ---
    const accessToken = localStorage.getItem("accessToken");
    const client = new Client({
      webSocketFactory: () => new SockJS(WEBSOCKET_URL),
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
          const signal = JSON.parse(message.body);
          handleSignal(signal);
        });
      },
    });

    client.activate();

    // 정리 (Cleanup)
    return () => {
      // 로컬 스트림 정리
      localStreamRef.current?.getTracks().forEach((track) => track.stop());
      // 피어 연결 종료
      Object.values(peerConnectionRefs.current).forEach((pc) => pc.close());
      peerConnectionRefs.current = {}; // 맵 초기화
      // 웹소켓 연결 해제
      client?.deactivate();
      console.log("[WebRTC] Cleanup completed.");
    };
  }, [roomId, memberId, handleSignal]); // handleSignal은 useCallback으로 감싸져 있으므로 안전함

  // 7. 연결 해제 함수
  const disconnectWebRTC = useCallback(() => {
    console.log("[WebRTC] 명시적 연결 해제 시작.");

    localStreamRef.current?.getTracks().forEach((track) => track.stop());
    Object.values(peerConnectionRefs.current).forEach((pc) => pc.close());
    peerConnectionRefs.current = {};
    stompClientRef.current?.deactivate();

    setDisplayLocalStream(null);
    setRemoteStream({});
    setPendingOfferIds(new Set());
    console.log("[WebRTC] 명시적 연결 해제 완료.");
  }, []);

  return {
    localStream: displayLocalStream,
    remoteStream,
    isMediaReady,
    connectToPeer,
    connectToPeerForceOffer,
    disconnectWebRTC,
    pendingOfferIds,
    isCoolingDown,
  };
};
