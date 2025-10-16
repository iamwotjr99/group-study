import { Outlet, useLocation } from "react-router-dom";
import Header from "./Header";

function Layout() {
  // 현재 URL 정보
  const location = useLocation();

  // 현재 경로에 "/room"이 포함되어 있는지 확인
  const isStudyRoomPage = location.pathname.includes("/room");

  return (
    <>
      {/* 스터디 룸 페이지가 아닐 때만 Header를 보여줌 */}
      {!isStudyRoomPage && <Header />}

      {/* Outlet은 자식 Route들이 렌더링될 위치를 지정 */}
      <main>
        <Outlet />
      </main>
    </>
  );
}

export default Layout;
