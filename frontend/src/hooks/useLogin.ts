import { useState } from "react";
import { loginAPI } from "../apis/authApi";
import { useUserStore } from "../store/userStore";

export const useLogin = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // 로딩 및 에러 상태 추가 (나중에 UI에 연결)
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { setUser } = useUserStore();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    // 로딩 시작
    setIsLoading(true);
    setError(null);

    // authApi.login() API 호출 로직
    try {
      const data = await loginAPI({ email, password });
      console.log("data: ", data);
      if (data.access_token && data.memberId) {
        const userInfo = {
          memberId: data.memberId,
          nickname: data.nickname,
        };

        // zustand에 accessToken, memberId, nickname 저장
        setUser(data.access_token, userInfo);
      }
    } catch (err) {
      console.error("로그인 실패", err);
      setError("로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.");
    } finally {
      setIsLoading(false);
    }
  };

  return {
    email,
    setEmail,
    password,
    setPassword,
    handleSubmit,
    isLoading,
    error,
  };
};
