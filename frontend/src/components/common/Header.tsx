import { Link } from "react-router-dom";
import { useUserStore } from "../../store/userStore";

function Header() {
  const { accessToken, clearToken } = useUserStore();

  const handleLogout = () => {
    clearToken();
    alert("로그아웃 되었습니다.");
  };

  return (
    <header
      style={{
        display: "fles",
        justifyContent: "space-between",
        padding: "1rem",
      }}
    >
      <Link to="/">
        <h1>스터디 링크</h1>
      </Link>
      <nav>
        {accessToken ? (
          <button onClick={handleLogout}>로그아웃</button>
        ) : (
          <>
            <Link to="/login" style={{ marginRight: "1rem" }}>
              로그인하러 가기
            </Link>
            <Link to="/signup">회원가입하러 가기</Link>
          </>
        )}
      </nav>
    </header>
  );
}

export default Header;
