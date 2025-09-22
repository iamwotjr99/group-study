import StudyCard from "../components/study/StudyCard";
import { useStuides } from "../hooks/useStudies";

function MainPage() {
  const { studies, isLoading, error } = useStuides();

  if (isLoading) {
    return <div className="text-center p-10">스터디 목록을 불러오는 중...</div>;
  }

  if (error) {
    return <div className="text-center p-10 text-red-500">{error}</div>;
  }

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-8 text-gray-800">전체 스터디</h1>

      {/* 스터디가 없으면 메시지 표시*/}
      {studies.length == 0 && (
        <div className="text-center text-gray-500 py-10">
          <p>아직 개설된 스터디가 없습니다.</p>
        </div>
      )}

      {/* 스터디가 있으면 그리드 레이아웃으로 표시*/}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
        {studies.map((study) => (
          <StudyCard key={study.studyGroupId} study={study} />
        ))}
      </div>
    </div>
  );
}

export default MainPage;
