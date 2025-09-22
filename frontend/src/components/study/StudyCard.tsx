import { Link } from "react-router-dom";
import type { StudySummary } from "../../types/study";

type StudyCardProps = {
  study: StudySummary;
};

function StudyCard({ study }: StudyCardProps) {
  const statusStyles = {
    RECRUITING: "bg-green-100 text-grenn-800",
    START: "bg-blue-100 text-blue-800",
    CLOSE: "bg-gray-100 text-gray-800",
  };

  return (
    <Link
      to={`/study-groups/${study.studyGroupId}`}
      className="block bg-white rounded-lg shadow-md hover:shadow-xl transition-shadow duration-300"
    >
      <div className="p-6">
        <div className="flex justify-between items-start">
          <h3 className="text-xl font-bold text-gray-800">{study.title}</h3>
          <span
            className={`px-2 py-1 text-xs font-semibold rounded-full ${
              statusStyles[study.state]
            }`}
          >
            {study.state === "RECRUITING"
              ? "모집중"
              : study.state === "START"
              ? "진행중"
              : "종료"}
          </span>
        </div>
        <p className="mt-2 text-sm text-gray-600">
          모집 방식: {study.policy === "AUTO" ? "선착순" : "승인제"}
        </p>
        <div className="mt-4 text-sm font-medium text-gray-700">
          <span>
            인원: {study.curMemberCount} / {study.capacity}
          </span>
        </div>
      </div>
    </Link>
  );
}

export default StudyCard;
