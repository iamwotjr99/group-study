import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { checkEmailAPI, checkNicknameAPI, signupAPI } from "../apis/authApi";

export const useSignup = () => {
  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  // 닉네임, 이메일 중복확인 결과 메시지와 상태를 관리할 state
  const [nicknameCheckResult, setNicknameCheckResult] = useState("");
  const [isNicknameAvailable, setIsNicknameAvailable] = useState(false);
  const [emailCheckResult, setEmailCheckResult] = useState("");
  const [isEmailAvailable, setIsEmailAvailable] = useState(false);

  const handleCheckNickname = async () => {
    if (!nickname) {
      setNicknameCheckResult("닉네임을 입력해주세요.");
      return;
    }
    try {
      const isDuplicate = await checkNicknameAPI(nickname);

      if (isDuplicate) {
        setNicknameCheckResult("이미 사용중인 닉네임입니다.");
        setIsNicknameAvailable(false);
      } else {
        setNicknameCheckResult("사용 가능한 닉네임입니다.");
        setIsNicknameAvailable(true);
      }
    } catch (err) {
      setNicknameCheckResult("닉네임 중복 확인 도중 에러가 발생했습니다.");
      setIsNicknameAvailable(false);
    }
  };

  const handleCheckEmail = async () => {
    if (!email) {
      setNicknameCheckResult("이메일을 입력해주세요.");
      return;
    }
    try {
      const isDuplicate = await checkEmailAPI(email);

      if (isDuplicate) {
        setEmailCheckResult("이미 사용중인 이메일 입니다.");
        setIsEmailAvailable(false);
      } else {
        setEmailCheckResult("사용 가능한 이메일 입니다.");
        setIsEmailAvailable(true);
      }
    } catch (err) {
      setEmailCheckResult("이메일 중복 확인 도중 에러가 발생했습니다.");
      setIsEmailAvailable(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    if (!isNicknameAvailable) {
      alert("닉네임 중복 확인을 해주새요.");
      return;
    }
    if (!isEmailAvailable) {
      alert("이메일 중복 확인을 해주세요.");
      return;
    }

    try {
      await signupAPI({ nickname, email, password });
      alert("회원가입에 성공했습니다! 로그인 페이지로 이동합니다.");
      navigate("/login");
    } catch (err) {
      setError("회원가입에 실패했습니다. 다시 시도해주세요");
    }
  };

  return {
    nickname,
    setNickname,
    email,
    setEmail,
    password,
    setPassword,
    handleSubmit,
    error,
    handleCheckNickname,
    nicknameCheckResult,
    handleCheckEmail,
    emailCheckResult,
  };
};
