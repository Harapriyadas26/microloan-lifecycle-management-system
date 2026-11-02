import React from "react";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, beforeEach, expect, it } from "vitest";
import { AuthProvider } from "../context/AuthContext";
import AuthPage from "../pages/AuthPage";

beforeEach(() => sessionStorage.clear());
afterEach(cleanup);

it("starts registration with empty fields after leaving sign in", () => {
  render(
    <MemoryRouter initialEntries={["/login"]}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<AuthPage key="login" />} />
          <Route
            path="/register"
            element={<AuthPage key="register" register />}
          />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );

  fireEvent.change(screen.getByLabelText("Email address"), {
    target: { value: "borrower@example.com" },
  });
  fireEvent.change(screen.getByLabelText("Password"), {
    target: { value: "Secret123!" },
  });
  fireEvent.click(screen.getByRole("link", { name: "Create an account" }));

  expect(screen.getByRole("heading", { name: "Create your account" })).toBeInTheDocument();
  expect(screen.getByLabelText("Email address")).toHaveValue("");
  expect(screen.getByLabelText("Password")).toHaveValue("");
});
