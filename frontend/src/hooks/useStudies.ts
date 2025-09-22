import { useEffect, useState } from "react";
import { fetchStudiesAPI } from "../apis/studyApi";
import type { StudySummary } from "../types/study";

export const useStuides = () => {
  const [studies, setStudies] = useState<StudySummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // TODO: 페이지 정보를 상태로 관리해서 페이지네이션 UI 만들기

  useEffect(() => {
    const getStudies = async () => {
      try {
        setIsLoading(true);
        const pageData = await fetchStudiesAPI(0, 5);
        setStudies(pageData.content);
      } catch (err) {
        setError("스터디 목록을 불러오는 데 실패했습니다.");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    getStudies();
  }, []);

  return { studies, isLoading, error };
};
