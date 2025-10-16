import { BrowserRouter, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import { useUserStore } from "./store/userStore";
import { useEffect } from "react";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import PublicRoute from "./components/auth/PublicRoute";
import MainPage from "./pages/MainPage";
import CreateStudyPage from "./pages/CreateStudyPage";
import StudyDetailPage from "./pages/StudyDetailPage";
import StudyRoomPage from "./pages/StudyRoomPage";
import Layout from "./components/common/Layout";

function App() {
  const { setUser } = useUserStore();

  useEffect(() => {
    // 앱이 시작될 떄 localStorage에서 accessToken 확인
    const token = localStorage.getItem("accessToken");
    const userInfoString = localStorage.getItem("userInfo");
    if (token && userInfoString) {
      // 토큰, 유저 정보가 있으면 전역 상태에 설정
      const userInfo = JSON.parse(userInfoString);
      setUser(token, userInfo);
    }
  }, [setUser]);
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
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
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/create-study"
            element={
              <ProtectedRoute>
                <CreateStudyPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/study-groups/:studyGroupId"
            element={
              <ProtectedRoute>
                <StudyDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/study-groups/:studyGroupId/room"
            element={
              <ProtectedRoute>
                <StudyRoomPage />
              </ProtectedRoute>
            }
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
