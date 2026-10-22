import { BrowserRouter, Routes, Route, Navigate, Link } from "react-router-dom";
import { AuthProvider, useAuth, officerRole } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import RoleRoute from "./components/RoleRoute";
import Workspace from "./layouts/Workspace";
import AuthPage from "./pages/AuthPage";
import Dashboard from "./pages/Dashboard";
import ProfilePage from "./pages/ProfilePage";
import KycPage from "./pages/KycPage";
import LoanApplicationPage from "./pages/LoanApplicationPage";
import LoansPage from "./pages/LoansPage";
import LoanDetailsPage from "./pages/LoanDetailsPage";
import OfficerKycPage from "./pages/OfficerKycPage";
function Home() {
  const { user } = useAuth();
  return (
    <Navigate
      to={!user ? "/login" : officerRole(user.role) ? "/officer" : "/borrower"}
      replace
    />
  );
}
export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<AuthPage />} />
          <Route path="/register" element={<AuthPage register />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<Workspace />}>
              <Route element={<RoleRoute />}>
                <Route path="/borrower" element={<Dashboard />} />
                <Route path="/borrower/profile" element={<ProfilePage />} />
                <Route path="/borrower/kyc" element={<KycPage />} />
                <Route
                  path="/borrower/apply"
                  element={<LoanApplicationPage />}
                />
                <Route path="/borrower/loans" element={<LoansPage />} />
                <Route
                  path="/borrower/loans/:id"
                  element={<LoanDetailsPage />}
                />
              </Route>
              <Route element={<RoleRoute officer />}>
                <Route path="/officer" element={<Dashboard officer />} />
                <Route path="/officer/kyc" element={<OfficerKycPage />} />
                <Route path="/officer/loans" element={<LoansPage officer />} />
                <Route
                  path="/officer/loans/:id"
                  element={<LoanDetailsPage officer />}
                />
              </Route>
            </Route>
          </Route>
          <Route
            path="*"
            element={
              <div className="not-found">
                <h1>Page not found</h1>
                <Link to="/">Return to your workspace →</Link>
              </div>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
