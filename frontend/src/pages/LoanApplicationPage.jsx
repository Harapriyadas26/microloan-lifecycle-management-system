import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/apiClient";
import { PageHeader, Field, ErrorMessage } from "../components/UI";
export default function LoanApplicationPage() {
  const [purpose, setPurpose] = useState("BUSINESS"),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  const navigate = useNavigate();
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const body = Object.fromEntries(new FormData(e.currentTarget));
    for (const k of ["loanAmount", "tenureMonths", "existingMonthlyDebt"])
      body[k] = Number(body[k]);
    try {
      const loan = await api("/api/borrower/loans", { method: "POST", body });
      navigate("/borrower/loans/" + loan.id);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeader title="Apply for a loan" />
      <ErrorMessage error={error} />
      <div className="two-column">
        <form className="panel" onSubmit={submit}>
          <h2>Loan application</h2>
          <div className="form-grid">
            <Field
              name="loanAmount"
              label="Requested amount (₹)"
              type="number"
              min={1000}
              max={500000}
              step="0.01"
              required
            />
            <Field
              name="tenureMonths"
              label="Tenure (months)"
              type="number"
              min={3}
              max={60}
              step={1}
              required
            />
            <Field
              name="purpose"
              label="Loan purpose"
              as="select"
              value={purpose}
              onChange={(e) => setPurpose(e.target.value)}
            >
              {[
                "BUSINESS",
                "EDUCATION",
                "MEDICAL",
                "AGRICULTURE",
                "HOME_IMPROVEMENT",
                "PERSONAL",
                "EMERGENCY",
                "OTHER",
              ].map((v) => (
                <option key={v}>{v}</option>
              ))}
            </Field>
            <Field
              name="existingMonthlyDebt"
              label="Existing monthly debt (₹)"
              type="number"
              min={0}
              max="9999999999.99"
              step="0.01"
              defaultValue={0}
              required
            />
          </div>
          <Field
            name="purposeDescription"
            label="Purpose details"
            as="textarea"
            maxLength={300}
            required={purpose === "OTHER"}
            rows={3}
          />
          <button disabled={busy}>
            {busy ? "Submitting…" : "Submit application →"}
          </button>
        </form>
        <aside className="panel guide">
          <div className="eyebrow">APPLICATION CHECKLIST</div>
          <h2>Before you submit</h2>
          <ol>
            <li>
              <strong>Complete your profile</strong>
              <p>Contact and income details are required.</p>
            </li>
            <li>
              <strong>Upload an ID document</strong>
              <p>
                You can apply while it is pending. Approval needs verification.
              </p>
            </li>
            <li>
              <strong>Check your request</strong>
              <p>₹1,000–₹5,00,000 over 3–60 months.</p>
            </li>
          </ol>
          <small>One active application at a time.</small>
        </aside>
      </div>
    </>
  );
}
