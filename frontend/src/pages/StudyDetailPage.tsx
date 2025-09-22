import { useParams } from "react-router-dom";
import { useStudyDetail } from "../hooks/useStudyDetail";
import { useUserStore } from "../store/userStore";

function StudyDetailPage() {
  const { studyGroupId } = useParams<{ studyGroupId: string }>();
  const id = studyGroupId ? parseInt(studyGroupId, 10) : undefined;

  const { studyGroupData, isLoading, error } = useStudyDetail(id);
  const { userInfo } = useUserStore();

  if (isLoading) {
    return <div className="text-center p-10">스터디 정보를 불러오는 중...</div>;
  }

  if (error) {
    return <div className="text-center p-10 text-red-600">{error}</div>;
  }

  if (!studyGroupData) {
    return (
      <div className="text-center p-10 text-red-600">
        스터디 정보를 찾을 수 없습니다.
      </div>
    );
  }

  const isParticipant = studyGroupData.participants.some(
    (p) => p.userId === userInfo?.memberId
  );

  console.log("Participants Data:", studyGroupData.participants);

  return (
    <div className="container mx-auto p-6 max-w-4xl">
      {/* 스터디 제목 */}
      <h1 className="text-4xl font-bold mb-4 text-gray-800">
        {studyGroupData.title}
      </h1>

      {/* 스터디 상태 및 기본 정보 */}
      <div className="mb-8 flex items-center space-x-4 text-gray-600">
        <span
          className={`px-3 py-1 text-sm font-semibold rounded-full ${
            studyGroupData.state === "RECRUITING"
              ? "bg-green-100 text-green-800"
              : "bg-gray-100 text-gray-800"
          }`}
        >
          {studyGroupData.state === "RECRUITING" ? "모집중" : "진행중/종료"}
        </span>
        <span>
          모집 방식: {studyGroupData.policy === "AUTO" ? "선착순" : "승인제"}
        </span>
        <span>
          모집 마감: {new Date(studyGroupData.deadline).toLocaleString()}
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* 왼쪽: 스터디 참여 버튼 등 메인 콘텐츠 */}
        <div className="md:col-span-2 bg-white p-6 rounded-lg shadow-md">
          {/* isParticipant 값에 따라 다른 내용 렌더링 */}
          {isParticipant ? (
            <>
              <h2 className="text-2xl font-semibold mb-4">
                스터디 룸 입장하기
              </h2>
              <p className="mb-6">스터디에 참여하여 화상 채팅을 시작하세요!</p>
              <button className="w-full bg-green-600 text-white py-3 rounded-md hover:bg-green-700 font-semibold">
                화상 채팅방 입장
              </button>
            </>
          ) : (
            <>
              <h2 className="text-2xl font-semibold mb-4">스터디 참여하기</h2>
              <p className="mb-6">이 스터디에 참여하여 함께 성장하세요!</p>
              <button className="w-full bg-indigo-600 text-white py-3 rounded-md hover:bg-indigo-700 font-semibold">
                참여 신청하기
              </button>
            </>
          )}
        </div>

        {/* 오른쪽: 참가자 정보 */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">
            참가자 ({studyGroupData.curMemberCount} / {studyGroupData.capacity})
          </h2>
          <ul className="space-y-3">
            {studyGroupData.participants.map((p) => (
              <li key={p.userId} className="flex items-center space-x-3">
                <div className="w-8 h-8 bg-gray-300 rounded-full"></div>
                <span>{p.nickname}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

export default StudyDetailPage;
