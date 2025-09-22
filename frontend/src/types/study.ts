// 스터디 목록 조회시 (Summary)
export interface StudySummary {
  studyGroupId: number;
  title: string;
  curMemberCount: number;
  capacity: number;
  deadline: string; // LocalDateTime은 string으로 받습니다.
  policy: "AUTO" | "APPROVAL"; // RecruitingPolicy enum
  state: "RECRUITING" | "IN_PROGRESS" | "COMPLETED"; // GroupState enum
}

// 스터디 상제 조회시 (Detail)
export interface StudyDetail extends StudySummary {
  participants: Participant[];
}

// 참여자 정보
export interface Participant {
  memberId: number;
  email: string;
  nickname: string;
  role: "MEMBER" | "HOST";
  status: "PENDING" | "APPROVED" | "REJECTED" | "CANCELED" | "LEAVE" | "KICKED";
}

// Spring의 Page 객체 타입
export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // 현재 페이지 번호 (0부터 시작))
}

// 스터디 생성시 서버에 보낼 객체 타입
export interface CreateStudyGroupData {
  title: string;
  capacity: number;
  deadline: string;
  policy: "AUTO" | "APPROVAL";
}
