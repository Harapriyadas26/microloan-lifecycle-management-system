import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api, viewDocument } from "../api/apiClient";
import {
  PageHeader,
  ErrorMessage,
  Loading,
  StatusBadge,
  Field,
  useLoad,
  money,
  date,
  ConfirmDialog,
} from "../components/UI";
export default function LoanDetailsPage({ officer = false }) {
  const { id } = useParams(),
    base = "/api/" + (officer ? "officer" : "borrower") + "/loans/" + id;
  const state = useLoad(
    () => api(base + (officer ? "" : "/details")),
    [id, officer],
  );
  const [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [confirm, setConfirm] = useState(null),
    [decision, setDecision] = useState("APPROVED"),
    [success, setSuccess] = useState("");
  async function action(path, method = "POST", body) {
    setBusy(true);
    setError(null);
    try {
      await api(path, { method, body });
      setSuccess("Application updated successfully.");
      state.reload();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
      setConfirm(null);
    }
  }
  function confirmDecision(e) {
    e.preventDefault();
    const body = Object.fromEntries(new FormData(e.currentTarget));
    if (decision === "APPROVED") {
      body.approvedAmount = Number(body.approvedAmount);
      body.annualInterestRate = Number(body.annualInterestRate);
    }
    setConfirm({
      title: "Record final " + decision.toLowerCase() + " decision?",
      run: () => action(base + "/decision", "POST", body),
    });
  }
  const d = state.data,
    a = d?.application,
    active = ["SUBMITTED", "UNDER_REVIEW"].includes(a?.status);
  return (
    <>
      <PageHeader
        eyebrow={officer ? "OFFICER WORKSPACE" : "YOUR APPLICATION"}
        title={"Application ML-" + String(id).padStart(4, "0")}
      >
        The details, the assessment, and the next step.
      </PageHeader>
      <ErrorMessage error={error || state.error} />
      {success && (
        <div className="alert success" role="status">
          {success}
        </div>
      )}
      {state.loading ? (
        <Loading />
      ) : (
        d && (
          <>
            <section className="panel application-summary">
              <div>
                <StatusBadge status={a.status} />
                <h2>{money(a.loanAmount)}</h2>
                <p>
                  {a.purpose.replaceAll("_", " ")} · {a.tenureMonths} months
                </p>
                <small>
                  Submitted {date(a.submittedAt)} · Updated {date(a.updatedAt)}
                </small>
              </div>
              <div>
                <span className="muted">Existing monthly debt</span>
                <h3>{money(a.existingMonthlyDebt)}</h3>
                <p>{a.purposeDescription}</p>
              </div>
            </section>
            <div className="two-column">
              <section className="panel">
                <div className="row">
                  <h2>Risk assessment</h2>
                  {d.risk && <StatusBadge status={d.risk.riskLevel} />}
                </div>
                {d.risk ? (
                  <>
                    <div className="risk-heading">
                      <strong>
                        {d.risk.score}
                        <small>/100</small>
                      </strong>
                      <div>
                        <span className="muted">Recommendation</span>
                        <p>
                          <StatusBadge status={d.risk.recommendation} />
                        </p>
                      </div>
                    </div>
                    <div className="risk-track">
                      <span style={{ width: d.risk.score + "%" }} />
                    </div>
                    <div className="mini-stats">
                      <div>
                        Monthly estimate
                        <strong>
                          {money(d.risk.estimatedMonthlyInstallment)}
                        </strong>
                      </div>
                      <div>
                        Debt-to-income
                        <strong>
                          {Number(d.risk.debtToIncomeRatio).toFixed(2)}%
                        </strong>
                      </div>
                      <div>
                        Loan / annual income
                        <strong>
                          {Number(d.risk.loanToAnnualIncomeRatio).toFixed(3)}
                        </strong>
                      </div>
                    </div>
                    <ul className="risk-reasons">
                      {d.risk.reasons.map((r) => (
                        <li key={r}>{r}</li>
                      ))}
                    </ul>
                    <small className="muted">
                      {d.risk.ruleVersion} · {date(d.risk.evaluatedAt)}
                    </small>
                    {officer && active && (
                      <p>
                        <button
                          disabled={busy}
                          className="secondary"
                          onClick={() => action(base + "/risk")}
                        >
                          Recalculate assessment
                        </button>
                      </p>
                    )}
                  </>
                ) : (
                  <p>No assessment available.</p>
                )}
              </section>
              <section className="panel">
                <h2>{d.decision ? "Final decision" : "What happens next?"}</h2>
                {d.decision ? (
                  <>
                    <StatusBadge status={d.decision.decision} />
                    <p>{d.decision.reason}</p>
                    {d.decision.approvedAmount != null && (
                      <>
                        <h3>{money(d.decision.approvedAmount)}</h3>
                        <p>
                          Annual interest rate: {d.decision.annualInterestRate}%
                        </p>
                      </>
                    )}
                    <small className="muted">
                      Decided by {d.decision.officerName} ·{" "}
                      {date(d.decision.decidedAt)}
                    </small>
                  </>
                ) : a.status === "CANCELLED" ? (
                  <p>This application was cancelled.</p>
                ) : (
                  <>
                    <p>
                      A loan officer will review the documents and assessment
                      before recording a decision.
                    </p>
                    <div className="notice">
                      Final approval requires verified KYC. The risk score is a
                      recommendation.
                    </div>
                  </>
                )}
                {officer && a.status === "SUBMITTED" && (
                  <button
                    disabled={busy}
                    onClick={() => action(base + "/review", "PATCH")}
                  >
                    Move to review →
                  </button>
                )}
                {!officer && active && (
                  <button
                    className="secondary danger"
                    disabled={busy}
                    onClick={() =>
                      setConfirm({
                        title: "Cancel this application?",
                        run: () => action(base + "/cancel", "PATCH"),
                      })
                    }
                  >
                    Cancel application
                  </button>
                )}
                {officer && a.status === "UNDER_REVIEW" && (
                  <form onSubmit={confirmDecision}>
                    <Field
                      label="Decision"
                      name="decision"
                      as="select"
                      value={decision}
                      onChange={(e) => setDecision(e.target.value)}
                    >
                      <option>APPROVED</option>
                      <option>REJECTED</option>
                    </Field>
                    <Field
                      label="Decision reason"
                      name="reason"
                      as="textarea"
                      required
                      minLength={5}
                      maxLength={500}
                      rows={3}
                    />
                    {decision === "APPROVED" && (
                      <>
                        <Field
                          label="Approved amount (₹)"
                          name="approvedAmount"
                          type="number"
                          min="0.01"
                          max={a.loanAmount}
                          step="0.01"
                          required
                          defaultValue={a.loanAmount}
                        />
                        <Field
                          label="Annual interest rate (%)"
                          name="annualInterestRate"
                          type="number"
                          min={0}
                          max={60}
                          step="0.01"
                          required
                        />
                      </>
                    )}
                    <button disabled={busy}>Record final decision →</button>
                  </form>
                )}
              </section>
            </div>
            {officer && (
              <section className="panel">
                <h2>Borrower information</h2>
                <div className="detail-grid">
                  <div>
                    <small>Name</small>
                    <strong>{d.profile.fullName}</strong>
                    <p>{d.profile.email}</p>
                  </div>
                  <div>
                    <small>Monthly income</small>
                    <strong>{money(d.profile.monthlyIncome)}</strong>
                    <p>
                      {d.profile.employmentType} · {d.profile.occupation}
                    </p>
                  </div>
                  <div>
                    <small>Contact</small>
                    <strong>{d.profile.phone}</strong>
                    <p>
                      {d.profile.address}, {d.profile.city}, {d.profile.state}{" "}
                      {d.profile.postalCode}
                    </p>
                  </div>
                </div>
                <h3>KYC documents</h3>
                {d.kycDocuments.map((k) => (
                  <div className="row document-card" key={k.id}>
                    <span>
                      {k.documentType} · {k.maskedDocumentNumber}
                    </span>
                    <StatusBadge status={k.status} />
                    <button
                      className="secondary"
                      disabled={busy}
                      onClick={async () => {
                        setBusy(true);
                        try {
                          await viewDocument(
                            "/api/officer/kyc/" + k.id + "/file",
                          );
                        } catch (e) {
                          setError(e);
                        } finally {
                          setBusy(false);
                        }
                      }}
                    >
                      View securely ↗
                    </button>
                  </div>
                ))}
                <Link to="/officer/kyc">Open KYC review queue →</Link>
              </section>
            )}
          </>
        )
      )}
      {confirm && (
        <ConfirmDialog
          title={confirm.title}
          busy={busy}
          onClose={() => setConfirm(null)}
          onConfirm={confirm.run}
        >
          Please confirm before continuing. Final loan decisions cannot be
          changed.
        </ConfirmDialog>
      )}
    </>
  );
}
