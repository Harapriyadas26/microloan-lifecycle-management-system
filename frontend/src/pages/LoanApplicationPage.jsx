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
      <PageHeader title="Make your next move">
        Tell us what you have in mind. We’ll make the assessment clear.
      </PageHeader>
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
            label="Tell us more about the purpose"
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
          <div className="eyebrow">BEFORE YOU APPLY</div>
          <h2>A clear path forward.</h2>
          <ol>
            <li>
              <strong>Complete your profile</strong>
              <p>Add your contact, employment and income details.</p>
            </li>
            <li>
              <strong>Submit your KYC</strong>
              <p>
                A pending document lets you apply. Verification is required for
                approval.
              </p>
            </li>
            <li>
              <strong>Understand your assessment</strong>
              <p>See your risk factors and recommendation immediately.</p>
            </li>
            <li>
              <strong>Follow the decision</strong>
              <p>
                A loan officer reviews your application and records the final
                outcome.
              </p>
            </li>
          </ol>
          <small>
            ₹1,000–₹5,00,000 · 3–60 months
            <br />
            One active application at a time.
          </small>
        </aside>
      </div>
    </>
  );
}
