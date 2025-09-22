import { useEffect, useState } from "react";
import type { StudyDetail } from "../types/study";
import { fetchStudyDetailAPI } from "../apis/studyApi";

export const useStudyDetail = (studyGroupId: number | undefined) => {
  const [studyGroupData, setStudyGroupData] = useState<StudyDetail | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!studyGroupId) {
      setIsLoading(false);
      setError("유효하지 않은 스터디 그룹 ID 입니다.");
      return;
    }

    const getStudyGroupDetail = async () => {
      try {
        setIsLoading(true);
        const studyGroupData = await fetchStudyDetailAPI(studyGroupId);
        setStudyGroupData(studyGroupData);
      } catch (err) {
        setError("스터디 그룹 정보를 불러오는데 실패했습니다.");
        console.error("useStudyDetail Hook Error: ", err);
      } finally {
        setIsLoading(false);
      }
    };

    getStudyGroupDetail();
  }, [studyGroupId]);

  return { studyGroupData, isLoading, error };
};
