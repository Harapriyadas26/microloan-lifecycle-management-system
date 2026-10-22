import { useEffect, useState } from "react";
import { api } from "../api/apiClient";
import {
  Field,
  PageHeader,
  ErrorMessage,
  Loading,
  useLoad,
} from "../components/UI";
export default function ProfilePage() {
  const state = useLoad(() =>
    api("/api/borrower/profile").catch((e) => {
      if (e.status === 404) return {};
      throw e;
    }),
  );
  const [values, setValues] = useState({}),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [success, setSuccess] = useState(false);
  useEffect(() => {
    if (state.data) setValues(state.data);
  }, [state.data]);
  const field = (name, label, props = {}) => (
    <Field
      key={name}
      name={name}
      label={label}
      required
      value={values[name] ?? ""}
      onChange={(e) => setValues({ ...values, [name]: e.target.value })}
      {...props}
    />
  );
  async function save(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setSuccess(false);
    try {
      setValues(
        await api("/api/borrower/profile", {
          method: "PUT",
          body: { ...values, monthlyIncome: Number(values.monthlyIncome) },
        }),
      );
      setSuccess(true);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeader title="Your profile">
        A few details to help us understand your application.
      </PageHeader>
      {state.loading ? (
        <Loading />
      ) : (
        <>
          <ErrorMessage error={state.error || error} />
          {success && (
            <div className="alert success" role="status">
              Your profile has been saved.
            </div>
          )}
          <form onSubmit={save} className="panel">
            <div className="section-title">
              <span className="step-number">01</span>
              <div>
                <h2>Personal & contact details</h2>
                <p>Keep these details accurate and up to date.</p>
              </div>
            </div>
            <div className="form-grid">
              {field("phone", "Phone number", {
                pattern: "[0-9]{10}",
                maxLength: 10,
                autoComplete: "tel",
              })}
              {field("dateOfBirth", "Date of birth", {
                type: "date",
                max: new Date(Date.now() - 86400000).toISOString().slice(0, 10),
              })}
              {field("address", "Address", {
                maxLength: 250,
                autoComplete: "street-address",
              })}
              {field("city", "City", { maxLength: 80 })}
              {field("state", "State", { maxLength: 80 })}
              {field("postalCode", "Postal code", {
                pattern: "[0-9]{6}",
                maxLength: 6,
              })}
            </div>
            <div className="section-title">
              <span className="step-number">02</span>
              <div>
                <h2>Employment & income</h2>
                <p>Used for your transparent risk assessment.</p>
              </div>
            </div>
            <div className="form-grid">
              {field("occupation", "Occupation", { maxLength: 100 })}
              <Field
                label="Employment type"
                name="employmentType"
                as="select"
                required
                value={values.employmentType || ""}
                onChange={(e) =>
                  setValues({ ...values, employmentType: e.target.value })
                }
              >
                <option value="">Select employment</option>
                {[
                  "SALARIED",
                  "SELF_EMPLOYED",
                  "DAILY_WAGE",
                  "STUDENT",
                  "UNEMPLOYED",
                  "OTHER",
                ].map((v) => (
                  <option key={v}>{v}</option>
                ))}
              </Field>
              {field("monthlyIncome", "Monthly income (₹)", {
                type: "number",
                min: "0.01",
                max: "9999999999.99",
                step: "0.01",
              })}
            </div>
            <div className="form-footer">
              <small>All fields are required.</small>
              <button disabled={busy}>
                {busy ? "Saving…" : "Save profile →"}
              </button>
            </div>
          </form>
        </>
      )}
    </>
  );
}
