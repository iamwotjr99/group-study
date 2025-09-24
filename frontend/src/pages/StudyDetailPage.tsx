import { useNavigate, useParams } from "react-router-dom";
import { useStudyDetail } from "../hooks/useStudyDetail";
import { useUserStore } from "../store/userStore";
import {
  applyStudyGroupAPI,
  approveApplicantAPI,
  cancleStudyGroupApplyAPI,
  closeStudyGroupAPI,
  kickParticipantAPI,
  leaveStudyGroupAPI,
  rejectApplicantAPI,
  startStudyGroupAPI,
} from "../apis/studyApi";

function StudyDetailPage() {
  const { studyGroupId } = useParams<{ studyGroupId: string }>();
  const id = studyGroupId ? parseInt(studyGroupId, 10) : undefined;

  const { studyGroupData, isLoading, error, refetch } = useStudyDetail(id);
  const { userInfo } = useUserStore();

  const navigate = useNavigate();

  const statusStyles = {
    RECRUITING: "bg-green-100 text-green-800",
    START: "bg-blue-100 text-blue-800",
    CLOSE: "bg-gray-100 text-gray-800",
  };

  const statusTexts = {
    RECRUITING: "모집중",
    START: "진행중",
    CLOSE: "종료",
  };

  // --- API 호출 및 화면 갱신을 위한 공통 핸들러 ---
  const handleAction = async (action: () => Promise<any>) => {
    if (!id) return;
    try {
      const result = await action();
      alert(result.message);
      refetch(); // 액션 성공 후 데이터를 다시 불러와 화면을 갱신
    } catch (err) {
      alert("요청 처리 중 오류가 발생했습니다.");
      console.error("Study Detail Page Handle Action Error: ", err);
    }
  };

  const handleApply = () => handleAction(() => applyStudyGroupAPI(id!));
  const handleCancel = () => handleAction(() => cancleStudyGroupApplyAPI(id!));
  const handleLeave = () => handleAction(() => leaveStudyGroupAPI(id!));
  const handleApprove = (applicantId: number) =>
    handleAction(() => approveApplicantAPI(id!, applicantId));
  const handleReject = (applicantId: number) =>
    handleAction(() => rejectApplicantAPI(id!, applicantId));
  const handleKick = (participantId: number) =>
    handleAction(() => kickParticipantAPI(id!, participantId));
  const handleStartStudy = () => handleAction(() => startStudyGroupAPI(id!));
  const handleCloseStudy = () => handleAction(() => closeStudyGroupAPI(id!));
  const handleEnterRoom = () => navigate(`/study-groups/${id}/room`);

  const isHost = userInfo?.memberId === studyGroupData?.hostId;
  const currentUserStatus = studyGroupData?.participants.find(
    (p) => p.userId === userInfo?.memberId
  )?.status;
  const isParticipant = currentUserStatus === "APPROVED";
  const isApplicant = currentUserStatus === "PENDING";
  const cannotApply =
    currentUserStatus === "CANCELED" ||
    currentUserStatus === "KICKED" ||
    currentUserStatus === "LEAVE" ||
    currentUserStatus === "REJECTED";

  const applicants =
    studyGroupData?.participants.filter((p) => p.status === "PENDING") || [];
  const approvedParticipants = studyGroupData?.participants.filter(
    (p) => p.status === "APPROVED"
  );

  const renderMainButton = () => {
    // 재신청 불가 조건
    if (cannotApply) {
      return (
        <div className="text-center p-3 bg-gray-100 rounded-md">
          이 스터디와 상호작용할 수 없습니다.
        </div>
      );
    }
    // 1. 방장일 경우
    if (isHost) {
      if (studyGroupData?.state === "RECRUITING") {
        return (
          <button
            onClick={handleStartStudy}
            className="w-full bg-blue-600 text-white py-3 rounded-md hover:bg-blue-700 font-semibold"
          >
            스터디 시작
          </button>
        );
      }
      if (studyGroupData?.state === "START") {
        return (
          <div className="space-y-4">
            <button
              onClick={handleEnterRoom}
              className="w-full bg-green-600 text-white py-3 rounded-md hover:bg-green-700 font-semibold"
            >
              화상 채팅방 입장
            </button>
            <button
              onClick={handleCloseStudy}
              className="w-full bg-red-600 text-white py-3 rounded-md hover:bg-red-700 font-semibold"
            >
              스터디 종료
            </button>
          </div>
        );
      }
    }

    if (isParticipant) {
      if (studyGroupData?.state === "START") {
        return (
          <button
            onClick={handleEnterRoom}
            className="w-full bg-green-600 text-white py-3 rounded-md hover:bg-green-700 font-semibold"
          >
            화상 채팅방 입장
          </button>
        );
      }
      if (studyGroupData?.state === "RECRUITING") {
        return (
          <div className="text-center p-3 bg-gray-100 rounded-md">
            스터디 시작을 기다리는 중입니다.
          </div>
        );
      }
    }
    // 3. 신청자일 경우
    if (isApplicant) {
      return (
        <button
          onClick={handleCancel}
          className="w-full bg-yellow-500 text-black py-3 rounded-md hover:bg-yellow-600 font-semibold"
        >
          참여 신청 취소
        </button>
      );
    }

    // 4. 외부인일 경우
    if (studyGroupData?.state === "RECRUITING") {
      return (
        <button
          onClick={handleApply}
          className="w-full bg-indigo-600 text-white py-3 rounded-md hover:bg-indigo-700 font-semibold"
        >
          참여 신청하기
        </button>
      );
    }

    // 그 외 (모집 종료 등)
    return (
      <div className="text-center p-3 bg-gray-100 rounded-md">
        모집이 마감된 스터디입니다.
      </div>
    );
  };

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
            statusStyles[studyGroupData.state] || "bg-gray-100 text-gray-800"
          }`}
        >
          {statusTexts[studyGroupData.state] || "알 수 없음"}
        </span>
        <span>
          모집 방식: {studyGroupData.policy === "AUTO" ? "선착순" : "승인제"}
        </span>
        <span>
          모집 마감: {new Date(studyGroupData.deadline).toLocaleString()}
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div className="md:col-span-2 bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-2xl font-semibold mb-4">스터디 참여</h2>
          {renderMainButton()}
          {isParticipant && !isHost && studyGroupData.state !== "CLOSE" && (
            <button
              onClick={handleLeave}
              className="w-full mt-4 bg-gray-500 text-white py-2 rounded-md hover:bg-gray-600 font-semibold"
            >
              스터디 퇴장
            </button>
          )}
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold mb-4">
            참가자 ({studyGroupData.curMemberCount} / {studyGroupData.capacity})
          </h2>
          <ul className="space-y-3">
            {approvedParticipants?.map((p) => (
              <li key={p.userId} className="flex justify-between items-center">
                <span>
                  {p.nickname} {p.role === "HOST" && "(방장)"}
                </span>
                {isHost && p.userId !== userInfo?.memberId && (
                  <button
                    onClick={() => handleKick(p.userId)}
                    className="bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600"
                  >
                    강퇴
                  </button>
                )}
              </li>
            ))}
          </ul>
        </div>

        {/* 방장에게만 보이는 신청자 관리 UI */}
        {isHost && applicants?.length > 0 && (
          <div className="mt-8 bg-white p-6 rounded-lg shadow-md">
            <h2 className="text-xl font-semibold mb-4">참여 신청자 목록</h2>
            <ul className="space-y-3">
              {applicants?.map((applicant) => (
                <li
                  key={applicant.userId}
                  className="flex justify-between items-center"
                >
                  <span>{applicant.nickname}</span>
                  <div className="space-x-2">
                    <button
                      onClick={() => handleApprove(applicant.userId)}
                      className="bg-blue-500 text-white px-3 py-1 text-sm rounded hover:bg-blue-600"
                    >
                      승인
                    </button>
                    <button
                      onClick={() => handleReject(applicant.userId)}
                      className="bg-red-500 text-white px-3 py-1 text-sm rounded hover:bg-red-600"
                    >
                      거절
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}

export default StudyDetailPage;
