// src/pages/StudyRoomPage.tsx
import { useParams } from "react-router-dom";
import { useChat } from "../hooks/useChat";
import { useState } from "react";
import { format } from "date-fns";
import { ko } from "date-fns/locale";
import { useUserStore } from "../store/userStore";

function StudyRoomPage() {
  const { studyGroupId } = useParams<{ studyGroupId: string }>();

  const { messages, sendMessage } = useChat(studyGroupId);

  const [newMessage, setNewMessage] = useState("");

  const { userInfo } = useUserStore();

  const handleSendMessage = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (newMessage.trim() != "") {
      sendMessage(newMessage);
      setNewMessage("");
    }
  };

  // UI 확인을 위한 더미 데이터
  const dummyParticipants = [
    { id: 1, nickname: "User One", isMuted: false, isCameraOff: false },
    { id: 2, nickname: "User Two", isMuted: true, isCameraOff: false },
    { id: 3, nickname: "You", isMuted: false, isCameraOff: false },
    { id: 4, nickname: "User Four", isMuted: false, isCameraOff: true },
  ];

  return (
    <div className="flex h-screen bg-gray-100">
      {/* ===== 메인 콘텐츠 (비디오 그리드 & 컨트롤러) ===== */}
      <div className="flex-1 flex flex-col">
        {/* --- 상단 헤더 --- */}
        <header className="p-4 bg-white border-b border-gray-200 flex justify-between items-center">
          <h1 className="text-xl font-bold text-gray-800">
            스터디 룸 (ID: {studyGroupId})
          </h1>
          <span className="text-sm text-gray-500">
            참여 인원: {dummyParticipants.length}명
          </span>
        </header>

        {/* --- 비디오 그리드 --- */}
        <main className="flex-1 bg-gray-200 p-4 grid grid-cols-1 md:grid-cols-2 gap-4 overflow-y-auto">
          {dummyParticipants.map((p) => (
            <div
              key={p.id}
              className="relative bg-black rounded-lg aspect-video flex items-center justify-center"
            >
              {p.isCameraOff ? (
                <div className="w-20 h-20 bg-gray-600 rounded-full flex items-center justify-center">
                  <span className="text-2xl text-white">
                    {p.nickname.charAt(0)}
                  </span>
                </div>
              ) : (
                <span className="text-gray-400">비디오 화면</span>
              )}
              <div className="absolute bottom-2 left-2 bg-black bg-opacity-50 text-white text-sm px-2 py-1 rounded">
                {p.isMuted ? "[음소거]" : ""} {p.nickname}
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
            <button className="bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-600 font-bold">
              나가기
            </button>
          </div>
        </footer>
      </div>

      {/* ===== 오른쪽 사이드바 (참가자 & 채팅) ===== */}
      <aside className="w-96 bg-white flex flex-col border-l border-gray-200">
        <h2 className="p-4 font-bold text-lg border-b border-gray-200">
          채팅 및 참가자
        </h2>

        {/* --- 참가자 목록 --- */}
        <div className="p-4 border-b border-gray-200">
          <h3 className="font-semibold mb-2">참가자</h3>
          <ul className="space-y-2">
            {dummyParticipants.map((p) => (
              <li key={p.id} className="text-sm">
                {p.nickname} {p.isMuted ? "(음소거)" : ""}
              </li>
            ))}
          </ul>
        </div>

        {/* --- 채팅 메시지 목록 --- */}
        <div className="flex-1 p-4 overflow-y-auto space-y-4">
          {messages.map((msg, index) => {
            const isMyMessage = msg.senderId === userInfo?.memberId;

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
