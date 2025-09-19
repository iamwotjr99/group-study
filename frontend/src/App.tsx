import { BrowserRouter, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import { useUserStore } from "./store/userStore";
import { useEffect } from "react";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import Header from "./components/common/Header";
import PublicRoute from "./components/auth/PublicRoute";

function App() {
  const { setToken } = useUserStore();

  useEffect(() => {
    // 앱이 시작될 떄 localStorage에서 accessToken 확인
    const token = localStorage.getItem("accessToken");
    if (token) {
      // 토큰이 있으면 전역 상태에 설정
      setToken(token);
    }
  }, [setToken]);
  return (
    <BrowserRouter>
      <Header />
      <main>
        <Routes>
          <Route
            path="/login"
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            }
          />
          <Route
            path="/signup"
            element={
              <PublicRoute>
                <SignupPage />
              </PublicRoute>
            }
          />
          {/* TODO: 메인 페이지 추가*/}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>메인 페이지(로그인한 사용자만 볼 수 있음)</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
