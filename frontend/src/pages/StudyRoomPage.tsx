// src/pages/StudyRoomPage.tsx
import { useNavigate, useParams } from "react-router-dom";
import { useChat } from "../hooks/useChat";
import { useEffect, useState } from "react";
import { format } from "date-fns";
import { ko } from "date-fns/locale";
import { useUserStore } from "../store/userStore";
import { useStudyDetail } from "../hooks/useStudyDetail";
import { useWebRTC } from "../hooks/useWebRTC";

function StudyRoomPage() {
  const navigate = useNavigate();
  const { studyGroupId } = useParams<{ studyGroupId: string }>();
  const id = studyGroupId ? parseInt(studyGroupId, 10) : undefined;

  const [newMessage, setNewMessage] = useState("");
  const memberId = useUserStore((state) => state.userInfo?.memberId);

  const { studyGroupData } = useStudyDetail(id);
  const approvedParticipants =
    studyGroupData?.participants.filter((p) => p.status === "APPROVED") || [];

  const { messages, onlineParticipants, sendMessage, disconnect } = useChat(
    studyGroupId,
    memberId
  );

  const {
    localStream,
    remoteStream,
    isMediaReady,
    connectToPeer,
    connectToPeerForceOffer,
    disconnectWebRTC,
    pendingOfferIds,
    isCoolingDown,
  } = useWebRTC(studyGroupId, memberId);

  const onlineUserIds = new Set(onlineParticipants.map((p) => p.userId));

  const handleSendMessage = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (newMessage.trim() != "") {
      sendMessage(newMessage);
      setNewMessage("");
    }
  };

  const handleLeaveRoom = () => {
    disconnectWebRTC();
    disconnect();
    navigate(-1);
  };

  // 참가자 수에 따라 동적으로 그리드 클래스를 결정하는 함수
  const getGridClass = (count: number): string => {
    if (count === 1) {
      // 1명일 때는 한 칸을 꽉 채웁니다.
      return "grid-cols-1";
    }
    if (count === 2) {
      // 2명일 때는 세로로 쌓거나(모바일), 가로로 2칸(데스크탑)을 만듭니다.
      return "grid-cols-1 lg:grid-cols-2";
    }
    if (count <= 4) {
      // 3~4명일 때는 2x2 그리드를 만듭니다.
      return "grid-cols-2";
    }
    if (count <= 9) {
      // 5~9명일 때는 3x3 그리드를 만듭니다.
      return "grid-cols-3";
    }
    // 10명 이상일 때는 4열 그리드를 만듭니다.
    return "grid-cols-4";
  };

  // 💡 [새로운 useEffect] Offer 재시도 로직 실행 (상태 변화에만 반응)
  useEffect(() => {
    // ✅ Cooldown 중이 아니고 미디어가 준비되었을 때만 실행
    if (isMediaReady && !isCoolingDown) {
      // console.log("[RoomPage] Re-evaluating pending offers.");
      pendingOfferIds.forEach((targetId) => {
        // connectToPeerForceOffer는 ID 비교를 무시하고 Offer를 보냄
        connectToPeerForceOffer(targetId);
      });
    }
  }, [isMediaReady, pendingOfferIds, connectToPeerForceOffer, isCoolingDown]); // ✅ isCoolingDown 의존성 추가!

  // onlineParticipants 목록이 변경될 때마다 새로운 참여자에게 연결 시도
  useEffect(() => {
    // ✅ Cooldown 중이 아니고 미디어가 준비되었을 때만 실행
    if (isMediaReady && !isCoolingDown && memberId) {
      console.log("[RoomPage] Media Ready. Checking for new peers.");

      const connectedPeerIds = new Set(Object.keys(remoteStream).map(Number));

      onlineParticipants.forEach((p) => {
        if (
          p.userId !== memberId &&
          !connectedPeerIds.has(p.userId) &&
          !pendingOfferIds.has(p.userId)
        ) {
          console.log(
            `[RoomPage] New peer detected: ${p.userId}. Calling connectToPeer.`
          );
          connectToPeer(p.userId);
        }
      });
    } else {
      console.log(
        "[RoomPage] Waiting for local media stream to be ready or cooling down..."
      );
    }
  }, [
    onlineParticipants,
    memberId,
    connectToPeer,
    isMediaReady,
    isCoolingDown,
    remoteStream, // 💡 [수정] remoteStream 의존성 추가
    pendingOfferIds, // 💡 [수정] pendingOfferIds 의존성 추가
  ]);

  const gridClass = getGridClass(onlineParticipants.length);

  return (
    <div className="flex h-screen bg-gray-100">
      {/* ===== 메인 콘텐츠 (비디오 그리드 & 컨트롤러) ===== */}
      <div className="flex-1 flex flex-col">
        {/* --- 상단 헤더 --- */}
        <header className="p-4 bg-white border-b border-gray-200 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-800">
            스터디 룸 (ID: {studyGroupId})
          </h1>
          {isCoolingDown && (
            <div className="bg-yellow-100 text-yellow-700 px-3 py-1 rounded font-medium text-sm">
              연결 불안정. 잠시 후 자동 재시도됩니다... ⏳
            </div>
          )}
        </header>

        {/* --- 비디오 그리드 --- */}
        <main
          className={`flex-1 bg-gray-200 p-4 grid gap-4 overflow-y-auto ${gridClass}`}
        >
          {/* 1. 내 비디오 화면 (localStream) */}
          {localStream && ( // localStream이 있을 때만 렌더링
            <div
              key="local" // 고유한 key 부여
              className="relative bg-black rounded-lg aspect-video flex items-center justify-center overflow-hidden"
            >
              <video
                ref={(video) => {
                  // 비디오 요소가 생성되면 srcObject에 localStream 연결
                  if (video) {
                    video.srcObject = localStream;
                  }
                }}
                className="w-full h-full object-cover" // 비디오가 영역을 꽉 채우도록
                autoPlay
                muted // 내 소리는 내가 듣지 않도록 음소거
                playsInline
              />
              <div className="absolute bottom-2 left-2 bg-black bg-opacity-50 text-white text-sm px-2 py-1 rounded">
                나 (You)
              </div>
            </div>
          )}

          {/* 2. 다른 참여자 비디오 화면 (remoteStreams) */}
          {onlineParticipants
            // 나 자신은 제외
            .filter((p) => p.userId !== memberId)
            .map((p) => {
              // 해당 참여자의 remoteStream 찾기
              const stream = remoteStream[p.userId];

              return (
                <div
                  key={p.userId} // 참여자의 userId를 key로 사용
                  className="relative bg-black rounded-lg aspect-video flex items-center justify-center overflow-hidden"
                >
                  {stream ? ( // remoteStream이 있으면 비디오 렌더링
                    <video
                      ref={(video) => {
                        if (video) {
                          video.srcObject = stream;
                        }
                      }}
                      className="w-full h-full object-cover"
                      autoPlay
                      playsInline
                    />
                  ) : (
                    // stream이 없을 때 (연결 중 상태)
                    <div className="flex flex-col items-center justify-center text-white space-y-3">
                      {/* 1. 아바타 (식별용) */}
                      <div className="w-16 h-16 bg-gray-600 rounded-full flex items-center justify-center">
                        <span className="text-xl text-white">
                          {/* 💡 [수정 1] 닉네임이 undefined일 때 크래시 방지 */}
                          {p.nickname?.charAt(0) || "?"}
                        </span>
                      </div>

                      {/* 2. 스피너 및 텍스트 */}
                      <div className="flex items-center space-x-2">
                        <svg
                          className="animate-spin h-4 w-4 text-white"
                          xmlns="http://www.w3.org/2000/svg"
                          fill="none"
                          viewBox="0 0 24 24"
                        >
                          <circle
                            className="opacity-25"
                            cx="12"
                            cy="12"
                            r="10"
                            stroke="currentColor"
                            strokeWidth="4"
                          ></circle>
                          <path
                            className="opacity-75"
                            fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                          ></path>
                        </svg>
                        <span className="text-sm font-medium">연결 중...</span>
                      </div>
                    </div>
                  )}
                  {/* 💡 [수정 2] 하단 닉네임 오버레이도 보호 */}
                  <div className="absolute bottom-2 left-2 bg-black bg-opacity-50 text-white text-sm px-2 py-1 rounded">
                    {p.nickname || "참가자..."}
                  </div>
                </div>
              );
            })}
        </main>

        {/* --- 하단 컨트롤 바 --- */}
        <footer className="p-4 bg-white border-t border-gray-200">
          <div className="flex justify-center space-x-4">
            <button className="bg-gray-200 p-3 rounded-full hover:bg-gray-300">
              🎤 마이크
            </button>
            <button className="bg-gray-200 p-3 rounded-full hover:bg-gray-300">
              📹 카메라
            </button>
            <button className="bg-gray-200 p-3 rounded-full hover:bg-gray-300">
              🖥️ 화면 공유
            </button>
            <button
              className="bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 font-bold"
              onClick={handleLeaveRoom}
              disabled={isCoolingDown}
            >
              나가기
            </button>
          </div>
        </footer>
      </div>

      {/* ===== 오른쪽 사이드바 (참가자 & 채팅) ===== */}
      <aside className="w-96 bg-white flex flex-col border-l border-gray-200">
        <h2 className="p-4 font-bold text-lg border-b border-gray-200">
          참가자 ({onlineParticipants.length} / {approvedParticipants?.length})
        </h2>

        {/* --- 참가자 목록 --- */}
        <div className="p-4 border-b border-gray-200">
          <ul className="space-y-3">
            {/* 전체 참여자를 기준으로 목록 선언 */}
            {approvedParticipants?.map((p) => {
              // 현재 사용자가 온라인 상태인지 확인
              const isOnline = onlineUserIds.has(p.userId);

              return (
                <li key={p.userId} className="flex items-center gap-2">
                  {/* 온라인 상태 표시 점 */}
                  <span
                    className={`w-2 h-2 rounded-full ${
                      isOnline ? "bg-green-500" : "bg-gray-300"
                    }`}
                  ></span>
                  {/* 닉네임 (오프라인일 경우 회색 처리) */}
                  <span
                    className={`text-sm ${
                      isOnline ? "text-gray-800" : "text-gray-400"
                    }`}
                  >
                    {p.nickname}
                  </span>
                  {p.role === "HOST" && <span className="text-xs">👑</span>}
                </li>
              );
            })}
          </ul>
        </div>

        {/* --- 채팅 메시지 목록 --- */}
        <div className="flex-1 p-4 overflow-y-auto space-y-4">
          {messages.map((msg, index) => {
            const isMyMessage = msg.senderId === memberId;

            return (
              <div key={index} className="text-sm">
                {/* isMyMessage 값에 따라 다른 색상 클래스를 적용 */}
                <span
                  className={`font-bold ${
                    isMyMessage ? "text-blue-600" : "text-black"
                  }`}
                >
                  {isMyMessage ? "나" : msg.nickname}:
                </span>

                <span> {msg.content}</span>

                <span className="text-xs text-gray-500 ml-2">
                  {format(new Date(msg.timestamp), "yyyy. M. d. a h:mm", {
                    locale: ko,
                  })}
                </span>
              </div>
            );
          })}
        </div>

        {/* --- 메시지 입력 폼 --- */}
        <form
          className="p-4 border-t border-gray-200 flex"
          onSubmit={handleSendMessage}
        >
          <input
            type="text"
            className="flex-1 border border-gray-300 rounded-l-md p-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            placeholder="메시지 입력..."
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
          />
          <button
            type="submit"
            className="bg-indigo-600 text-white px-4 rounded-r-md hover:bg-indigo-700"
          >
            전송
          </button>
        </form>
      </aside>
    </div>
  );
}

export default StudyRoomPage;
