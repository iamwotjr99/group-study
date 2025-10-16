// src/pages/StudyRoomPage.tsx
import { useNavigate, useParams } from "react-router-dom";
import { useChat } from "../hooks/useChat";
import { useState } from "react";
import { format } from "date-fns";
import { ko } from "date-fns/locale";
import { useUserStore } from "../store/userStore";
import { useStudyDetail } from "../hooks/useStudyDetail";

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

  const onlineUserIds = new Set(onlineParticipants.map((p) => p.userId));

  const handleSendMessage = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (newMessage.trim() != "") {
      sendMessage(newMessage);
      setNewMessage("");
    }
  };

  const handleLeaveRoom = () => {
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
        </header>

        {/* --- 비디오 그리드 --- */}
        <main
          className={`flex-1 bg-gray-200 p-4 grid gap-4 overflow-y-auto ${gridClass}`}
        >
          {onlineParticipants?.map((p) => (
            <div
              key={p.userId}
              className="relative bg-black rounded-lg aspect-video flex items-center justify-center"
            >
              {/* 실제 비디오 스트림이 들어갈 자리 */}
              {/* 지금은 카메라가 꺼져있다고 가정하고 닉네임 이니셜을 표시 */}
              <div className="w-20 h-20 bg-gray-600 rounded-full flex items-center justify-center">
                <span className="text-2xl text-white">
                  {p.nickname.charAt(0)}
                </span>
              </div>

              <div className="absolute bottom-2 left-2 bg-black bg-opacity-50 text-white text-sm px-2 py-1 rounded">
                {p.nickname}
              </div>
            </div>
          ))}
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
