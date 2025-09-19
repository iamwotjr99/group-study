import { Navigate } from "react-router-dom";
import { useUserStore } from "../../store/userStore";

type ProtectedRouteProps = {
  children: React.ReactNode;
};

function ProtectedRoute({ children }: ProtectedRouteProps): React.ReactElement {
  // const { accessToken } = useUserStore(); // 이 부분을 아래처럼 변경
  const accessToken = useUserStore((state) => state.accessToken);

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

export default ProtectedRoute;
