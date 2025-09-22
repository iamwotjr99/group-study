import { useState } from "react";
import type { CreateStudyGroupData } from "../types/study";
import { useNavigate } from "react-router-dom";
import { createStudyGroupAPI } from "../apis/studyApi";

export const useCreateStudy = () => {
  const [studyGroupData, setStudyGroupData] = useState<CreateStudyGroupData>({
    title: "",
    capacity: 2,
    deadline: "",
    policy: "AUTO",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setStudyGroupData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      const createData = {
        ...studyGroupData,
        capacity: Number(studyGroupData.capacity),
      };
      const newStudy = await createStudyGroupAPI(createData);
      alert(
        `스터디가 성공적으로 생성되었습니다! 생성된 스터디 ID: ${newStudy.studyGroupId}`
      );
      navigate(`/study-groups/${newStudy.studyGroupId}`);
    } catch (err) {
      setError("스터디 생성에 실패했습니다.");
      console.error("useCreateStudy Error", err);
    } finally {
      setIsLoading(false);
    }
  };

  return { studyGroupData, isLoading, error, handleChange, handleSubmit };
};
