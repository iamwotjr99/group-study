import type { Page, StudyDetail, StudySummary } from "../types/study";
import api from "./instance";

// 스터디 목록 전체를 페이징으로 조회하는 API 함수
// page: 페이지 번호(0부터 시작), size: 페이지당 항목 수
export const fetchStudiesAPI = async (
  page = 0,
  size = 5
): Promise<Page<StudySummary>> => {
  try {
    const response = await api.get("/api/study-groups", {
      params: { page, size },
    });
    return response.data;
  } catch (err) {
    console.error("Fetch Studies API Error: ", err);
    throw err;
  }
};

// 스터디 상세 정보를 조회하는 API 함수
export const fetchStudyDetailAPI = async (
  studyGroupId: number
): Promise<StudyDetail> => {
  try {
    const response = await api.get(`/api/study-groups/${studyGroupId}`);
    return response.data;
  } catch (err) {
    console.error(`Fetch Study Detail API Error (ID: ${studyGroupId}):`, err);
    throw err;
  }
};
