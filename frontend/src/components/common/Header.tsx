// src/components/common/Header.tsx
import { Link, useNavigate } from "react-router-dom";
import { useUserStore } from "../../store/userStore";

function Header() {
  const { accessToken, clearToken } = useUserStore((state) => state);
  const navigate = useNavigate();

  const handleLogout = () => {
    clearToken();
    alert("로그아웃 되었습니다.");
    navigate("/login");
  };

  return (
    <header className="bg-white shadow-md sticky top-0 z-50">
      <nav className="container mx-auto px-6 py-4 flex justify-between items-center">
        {/* 로고 */}
        <Link to="/" className="text-2xl font-bold text-indigo-600">
          스터디 링크
        </Link>

        {/* 메뉴 */}
        <div className="flex items-center space-x-6">
          {accessToken ? (
            // --- 로그인 상태일 때 ---
            <>
              <Link
                to="/my-studies"
                className="text-gray-600 hover:text-indigo-600 font-medium"
              >
                내 스터디
              </Link>
              <Link
                to="/find-studies"
                className="text-gray-600 hover:text-indigo-600 font-medium"
              >
                스터디 찾기
              </Link>
              <Link
                to="/create-study"
                className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700 font-medium"
              >
                + 스터디 만들기
              </Link>
              <button
                onClick={handleLogout}
                className="bg-gray-200 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-300 font-medium"
              >
                로그아웃
              </button>
            </>
          ) : (
            // --- 로그아웃 상태일 때 ---
            <div className="space-x-4">
              <Link
                to="/login"
                className="text-gray-600 hover:text-indigo-600 font-medium"
              >
                로그인
              </Link>
              <Link
                to="/signup"
                className="bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700 font-medium"
              >
                회원가입
              </Link>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
}

export default Header;
