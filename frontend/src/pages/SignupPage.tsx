import { Link } from "react-router-dom";
import { useSignup } from "../hooks/useSignup";

function SignupPage() {
  const {
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
  } = useSignup();

  return (
    <div>
      <h1>회원가입</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="nickname">닉네임</label>
          <input
            type="text"
            id="nickname"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            required
          />
          <button type="submit" onClick={handleCheckNickname}>
            닉네임 중복확인
          </button>
          {nicknameCheckResult && <p>{nicknameCheckResult}</p>}
        </div>
        <div>
          <label htmlFor="email">이메일</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <button type="submit" onClick={handleCheckEmail}>
            이메일 중복확인
          </button>
          {emailCheckResult && <p>{emailCheckResult}</p>}
        </div>
        <div>
          <label htmlFor="password">비밀번호</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {/* 회원가입 실패 시*/}
        {error && <p style={{ color: "red" }}>{error}</p>}

        <button type="submit">회원가입</button>
        <p>
          이미 계정이 있으신가요? <Link to="/login">로그인하러 가기</Link>
        </p>
      </form>
    </div>
  );
}

export default SignupPage;
