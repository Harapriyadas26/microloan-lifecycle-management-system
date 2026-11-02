import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth, officerRole } from "../context/AuthContext";
import { Field, ErrorMessage } from "../components/UI";
export default function AuthPage({ register = false }) {
  const { user, authenticate } = useAuth(),
    navigate = useNavigate();
  const [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  if (user)
    return (
      <Navigate
        to={officerRole(user.role) ? "/officer" : "/borrower"}
        replace
      />
    );
  async function submit(e) {
    e.preventDefault();
    const form = e.currentTarget;
    setError(null);
    setBusy(true);
    try {
      const u = await authenticate(
        register ? "register" : "login",
        Object.fromEntries(new FormData(form)),
      );
      form.reset();
      navigate(officerRole(u.role) ? "/officer" : "/borrower");
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="auth-page">
      <section className="auth-story">
        <div className="brand">
          <img src="/microloan-mark.svg" className="brand-mark" alt="" />{" "}
          microloan
        </div>
        <div>
          <div className="eyebrow">MICROLOAN PORTAL</div>
          <h1>
            Apply for a loan.
            <br />
            Track every step.
          </h1>
          <p>
            Submit documents, follow application updates and manage your loan
            account in one place.
          </p>
        </div>
      </section>
      <section className="auth-form">
        <div className="auth-card">
          <div className="eyebrow">
            {register ? "BORROWER REGISTRATION" : "SECURE SIGN IN"}
          </div>
          <h2>
            {register ? "Create your account" : "Sign in to your account"}
          </h2>
          <p>
            {register
              ? "Register as a borrower to apply for a loan and track its progress."
              : "Enter your registered email address and password to continue."}
          </p>
          <ErrorMessage error={error} />
          <form onSubmit={submit}>
            {register && (
              <Field
                label="Full name"
                name="fullName"
                required
                maxLength={100}
                autoComplete="name"
              />
            )}
            <Field
              label="Email address"
              name="email"
              type="email"
              required
              maxLength={150}
              autoComplete="email"
            />
            <Field
              label="Password"
              name="password"
              type="password"
              required
              minLength={register ? 8 : undefined}
              maxLength={72}
              autoComplete={register ? "new-password" : "current-password"}
            />
            {register && <small className="auth-hint">Use at least 8 characters.</small>}
            <button disabled={busy}>
              {busy
                ? register
                  ? "Creating account…"
                  : "Signing in…"
                : register
                  ? "Create account"
                  : "Sign in"}
            </button>
          </form>
          <p>
            {register ? "Already have an account?" : "New borrower?"}{" "}
            <Link to={register ? "/login" : "/register"}>
              {register ? "Sign in" : "Create an account"}
            </Link>
          </p>
        </div>
      </section>
    </div>
  );
}
