import { Navigate } from "react-router-dom";
import { useUserStore } from "../../store/userStore";
import React from "react";

type PublicRouteProps = {
  children: React.ReactNode;
};

function PublicRoute({ children }: PublicRouteProps): React.ReactElement {
  const accessToken = useUserStore((state) => state.accessToken);
  console.log("%cPublicRoute 렌더링. 현재 토큰:", "color: blue;", accessToken);

  if (accessToken) {
    // 토큰이 있으면 메인 페이지로 리디렉션
    return <Navigate to="/" replace />;
  }

  // 토큰이 없으면 요청한 페이지(로그인/회원가입)를 보여줌
  return <>{children}</>;
}

export default PublicRoute;
