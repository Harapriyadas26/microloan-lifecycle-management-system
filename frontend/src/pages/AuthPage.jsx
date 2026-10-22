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
    setError(null);
    setBusy(true);
    try {
      const u = await authenticate(
        register ? "register" : "login",
        Object.fromEntries(new FormData(e.currentTarget)),
      );
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
          <span className="brand-mark">m.</span> microloan
        </div>
        <div>
          <div className="eyebrow">CLEAR STEPS. BETTER FUTURES.</div>
          <h1>
            A little support.
            <br />A world of
            <br />
            <em>possibility.</em>
          </h1>
          <p>
            Apply with confidence. Understand your assessment.
            <br />
            Follow every step in one simple workspace.
          </p>
          <div className="story-graphic">
            <span>
              01
              <br />
              <small>Apply</small>
            </span>
            <i>→</i>
            <span>
              02
              <br />
              <small>Assess</small>
            </span>
            <i>→</i>
            <span>
              03
              <br />
              <small>Decide</small>
            </span>
          </div>
        </div>
        <small>MICROLOAN LIFECYCLE MANAGEMENT SYSTEM</small>
      </section>
      <section className="auth-form">
        <div className="auth-card">
          <div className="eyebrow">LET’S GET STARTED</div>
          <h2>{register ? "Create your account" : "Welcome back"}</h2>
          <p>
            {register
              ? "Your next chapter starts here."
              : "Sign in to continue your loan journey."}
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
            <button disabled={busy}>
              {busy
                ? "Please wait…"
                : register
                  ? "Create account →"
                  : "Sign in →"}
            </button>
          </form>
          <p>
            {register ? "Already registered?" : "New to Microloan?"}{" "}
            <Link to={register ? "/login" : "/register"}>
              {register ? "Sign in" : "Create an account"}
            </Link>
          </p>
          <small className="muted">
            Your details stay in your workspace. Use fictional documents for
            this academic demo.
          </small>
        </div>
      </section>
    </div>
  );
}
