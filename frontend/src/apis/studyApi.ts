import type {
  CreateStudyGroupData,
  Page,
  StudyDetail,
  StudySummary,
} from "../types/study";
import api from "./instance";

// 스터디 목록 전체를 페이징으로 조회하는 API 함수
// page: 페이지 번호(0부터 시작), size: 페이지당 항목 수
export const fetchStudiesAPI = async (
  page = 0,
  size = 5
): Promise<Page<StudySummary>> => {
  try {
    const response = await api.get("/study-groups", {
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
    const response = await api.get(`/study-groups/${studyGroupId}`);
    return response.data;
  } catch (err) {
    console.error(`Fetch Study Detail API Error (ID: ${studyGroupId}):`, err);
    throw err;
  }
};

// ---- 스터디 Lifecycle API ----
// 새로운 스터디를 생성하는 API 함수
export const createStudyGroupAPI = async (
  studyGroupData: CreateStudyGroupData
): Promise<{ studyGroupId: number }> => {
  try {
    const response = await api.post("/study-groups", studyGroupData);
    return response.data;
  } catch (err) {
    console.error("Create StudyGroup API Error: ", err);
    throw err;
  }
};

// 스터디를 시작하는 API
export const startStudyGroupAPI = async (
  studyGroupId: number
): Promise<{ message: string }> => {
  try {
    const response = await api.post(`/study-groups/${studyGroupId}/start`);
    return response.data;
  } catch (err) {
    console.error("Start StudyGroup API Error : ", err);
    throw err;
  }
};

// 스터디를 종료하는 API
export const closeStudyGroupAPI = async (
  studyGroupId: number
): Promise<{ message: string }> => {
  try {
    const response = await api.post(`/study-groups/${studyGroupId}/close`);
    return response.data;
  } catch (err) {
    console.error("Close StudyGroup API Error : ", err);
    throw err;
  }
};

// ---- 스터디 Host API ----
// 참여 신청을 승인하는 API
export const approveApplicantAPI = async (
  studyGroupId: number,
  applicantId: number
): Promise<{ message: string }> => {
  try {
    const response = await api.post(
      `/study-groups/${studyGroupId}/applicants/${applicantId}/approve`
    );
    return response.data;
  } catch (err) {
    console.error("Approve Applicant API Error: ", err);
    throw err;
  }
};

// 참여 신청을 거절하는 API
export const rejectApplicantAPI = async (
  studyGroupId: number,
  applicantId: number
): Promise<{ message: string }> => {
  try {
    const response = await api.delete(
      `/study-groups/${studyGroupId}/applicants/${applicantId}/reject`
    );
    return response.data;
  } catch (err) {
    console.error("Reject Applicant API Error: ", err);
    throw err;
  }
};

// 스터디 그룹원을 강퇴하는 API
export const kickParticipantAPI = async (
  studyGroupId: number,
  participantId: number
): Promise<{ message: string }> => {
  try {
    const response = await api.delete(
      `/study-groups/${studyGroupId}/participants/${participantId}/kick`
    );
    return response.data;
  } catch (err) {
    console.error("Kick Participant API Error: ", err);
    throw err;
  }
};

// ---- 스터디 Participant API ----
// 특정 스터디 그룹에 참여 신청하는 API
export const applyStudyGroupAPI = async (
  studyGroupId: number
): Promise<{ message: string }> => {
  try {
    const response = await api.post(
      `/study-groups/${studyGroupId}/applicants/apply`
    );
    return response.data;
  } catch (err) {
    console.error("Apply To StudyGroup API Error: ", err);
    throw err;
  }
};

// 특정 스터디 그룹에 신청한 참여 신청을 취소하는 API
export const cancleStudyGroupApplyAPI = async (
  studyGroupId: number
): Promise<{ message: string }> => {
  try {
    const response = await api.delete(
      `/study-groups/${studyGroupId}/applicants/cancel`
    );
    return response.data;
  } catch (err) {
    console.error("Cancel To StudyGroup API Error: ", err);
    throw err;
  }
};

// 내가 속한 스터디 그룹을 퇴장하는 API
export const leaveStudyGroupAPI = async (
  studyGroupId: number
): Promise<{ message: string }> => {
  try {
    const response = await api.delete(
      `/study-groups/${studyGroupId}/participants/leave`
    );
    return response.data;
  } catch (err) {
    console.error("Leave To StudyGroup API Error: ", err);
    throw err;
  }
};
