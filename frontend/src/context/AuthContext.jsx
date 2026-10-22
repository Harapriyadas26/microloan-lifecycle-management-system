import { createContext, useContext, useEffect, useState } from "react";
import { api, readSession } from "../api/apiClient";
const AuthContext = createContext(null);
export function isExpired(token) {
  try {
    const p = JSON.parse(
      atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")),
    );
    return !p.exp || p.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}
export const officerRole = (role) => ["LOAN_OFFICER", "ADMIN"].includes(role);
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const u = readSession();
    if (u && !isExpired(u.token)) return u;
    sessionStorage.removeItem("microloan.session");
    return null;
  });
  const logout = () => {
    sessionStorage.removeItem("microloan.session");
    setUser(null);
  };
  useEffect(() => {
    window.addEventListener("session-expired", logout);
    const timer = setInterval(() => {
      if (user && isExpired(user.token)) logout();
    }, 10000);
    return () => {
      window.removeEventListener("session-expired", logout);
      clearInterval(timer);
    };
  }, [user]);
  const authenticate = async (kind, values) => {
    const session = await api("/api/auth/" + kind, {
      method: "POST",
      body: values,
    });
    sessionStorage.setItem("microloan.session", JSON.stringify(session));
    setUser(session);
    return session;
  };
  return (
    <AuthContext.Provider value={{ user, logout, authenticate }}>
      {children}
    </AuthContext.Provider>
  );
}
export const useAuth = () => useContext(AuthContext);
