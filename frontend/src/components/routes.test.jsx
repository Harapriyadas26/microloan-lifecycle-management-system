import React from "react";
import { render, screen, cleanup, waitFor } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { afterEach, beforeEach, describe, it, expect, vi } from "vitest";
import ProtectedRoute from "./ProtectedRoute";
import RoleRoute from "./RoleRoute";
import { StatusBadge, money } from "./UI";
import { AuthProvider, isExpired } from "../context/AuthContext";
import { api } from "../api/apiClient";
const token = (exp) => "header." + btoa(JSON.stringify({ exp })) + ".signature";
beforeEach(() => {
  sessionStorage.clear();
});
afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});
function mount(route, user) {
  if (user) sessionStorage.setItem("microloan.session", JSON.stringify(user));
  render(
    <MemoryRouter initialEntries={["/secure"]}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<p>Login required</p>} />
          <Route path="/borrower" element={<p>Borrower home</p>} />
          <Route path="/officer" element={<p>Officer home</p>} />
          <Route element={route}>
            <Route path="/secure" element={<p>Secure content</p>} />
          </Route>
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}
describe("session and route protection", () => {
  it("redirects anonymous users", () => {
    mount(<ProtectedRoute />);
    expect(screen.getByText("Login required")).toBeInTheDocument();
  });
  it("allows authenticated users", () => {
    mount(<ProtectedRoute />, {
      token: token(Date.now() / 1000 + 3600),
      role: "BORROWER",
    });
    expect(screen.getByText("Secure content")).toBeInTheDocument();
  });
  it("rejects an expired token and removes the stored session", () => {
    mount(<ProtectedRoute />, { token: token(1), role: "BORROWER" });
    expect(screen.getByText("Login required")).toBeInTheDocument();
    expect(sessionStorage.getItem("microloan.session")).toBeNull();
  });
  it("blocks borrowers from officer pages", () => {
    mount(<RoleRoute officer />, {
      token: token(Date.now() / 1000 + 3600),
      role: "BORROWER",
    });
    expect(screen.getByText("Borrower home")).toBeInTheDocument();
  });
  it("allows officers", () => {
    mount(<RoleRoute officer />, {
      token: token(Date.now() / 1000 + 3600),
      role: "LOAN_OFFICER",
    });
    expect(screen.getByText("Secure content")).toBeInTheDocument();
  });
  it("clears the session on a server 401", async () => {
    mount(<ProtectedRoute />, {
      token: token(Date.now() / 1000 + 3600),
      role: "BORROWER",
    });
    vi.spyOn(globalThis, "fetch").mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({ message: "Authentication required" }),
    });
    await expect(api("/api/borrower/profile")).rejects.toThrow(
      "Authentication required",
    );
    await waitFor(() =>
      expect(screen.getByText("Login required")).toBeInTheDocument(),
    );
    expect(sessionStorage.getItem("microloan.session")).toBeNull();
  });
  it("treats malformed JWTs as expired", () =>
    expect(isExpired("bad")).toBe(true));
});
describe("risk and money display", () => {
  it("renders a readable status", () => {
    render(<StatusBadge status="MANUAL_REVIEW" />);
    expect(screen.getByText("MANUAL REVIEW")).toHaveClass("manual_review");
  });
  it("formats Indian currency", () =>
    expect(money(100000)).toContain("1,00,000"));
});
