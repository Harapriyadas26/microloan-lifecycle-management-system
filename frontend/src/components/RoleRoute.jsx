import { Navigate, Outlet } from "react-router-dom";
import { useAuth, officerRole } from "../context/AuthContext";
export default function RoleRoute({ officer = false }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return officerRole(user.role) === officer ? (
    <Outlet />
  ) : (
    <Navigate to={officerRole(user.role) ? "/officer" : "/borrower"} replace />
  );
}
