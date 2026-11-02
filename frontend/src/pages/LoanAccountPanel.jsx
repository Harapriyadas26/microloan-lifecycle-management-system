import { useState } from "react";
import { api } from "../api/apiClient";
import { useAuth } from "../context/AuthContext";
import {
  ErrorMessage,
  Field,
  Loading,
  money,
  date,
  useLoad,
} from "../components/UI";

export default function LoanAccountPanel({ id, officer }) {
  const { user } = useAuth();
  const base = "/api/" + (officer ? "officer" : "borrower") + "/loans/" + id;
  const state = useLoad(
    () =>
      api(base + "/account").catch((e) => {
        if (e.status === 404) return null;
        throw e;
      }),
    [id, officer],
  );
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  async function submit(e, endpoint) {
    e.preventDefault();
    const body = Object.fromEntries(new FormData(e.currentTarget));
    if (body.amount) body.amount = Number(body.amount);
    setBusy(true);
    setError(null);
    try {
      await api(base + endpoint, { method: "POST", body });
      state.reload();
      e.currentTarget.reset();
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  }
  const account = state.data;
  return (
    <section className="panel">
      <h2>Loan account</h2>
      <p className="muted">Demo records only. No funds are transferred.</p>
      <ErrorMessage error={error || state.error} />
      {state.loading ? (
        <Loading />
      ) : !account ? (
        <>
          <p>Awaiting disbursement record.</p>
          {officer && user.role === "ADMIN" && (
            <form onSubmit={(e) => submit(e, "/disbursement")}>
              <Field
                name="reference"
                label="Disbursement reference"
                required
                minLength={5}
                maxLength={40}
              />
              <button disabled={busy}>Record disbursement</button>
            </form>
          )}
        </>
      ) : (
        <>
          <p>
            <strong>{account.status}</strong> · Principal{" "}
            {money(account.principal)} · Annual rate {account.annualRate}%
          </p>
          <p>
            Outstanding balance: <strong>{money(account.outstanding)}</strong>
          </p>
          {account.installments.some((row) => row.status === "OVERDUE") && (
            <p className="alert error">
              An installment is overdue. Check the schedule for late charges.
            </p>
          )}
          {account.installments.some(
            (row) =>
              row.status === "DUE" &&
              new Date(row.dueDate) <= new Date(Date.now() + 7 * 86400000),
          ) && (
            <p className="notice">An installment is due within seven days.</p>
          )}
          <small>Disbursement reference: {account.disbursementReference}</small>
          {officer && account.status === "ACTIVE" && (
            <form onSubmit={(e) => submit(e, "/payments")}>
              <h3>Record payment</h3>
              <Field
                name="reference"
                label="Unique payment reference"
                required
                minLength={5}
                maxLength={40}
              />
              <Field
                name="amount"
                label="Amount (₹)"
                type="number"
                min="0.01"
                max={account.outstanding}
                step="0.01"
                required
              />
              <button disabled={busy}>Record payment</button>
            </form>
          )}
          <h3>Monthly installment schedule</h3>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>No.</th>
                  <th>Due</th>
                  <th>Principal</th>
                  <th>Interest</th>
                  <th>Late charge</th>
                  <th>Paid</th>
                  <th>Remaining</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {account.installments.map((row) => (
                  <tr key={row.number}>
                    <td>{row.number}</td>
                    <td>{row.dueDate}</td>
                    <td>{money(row.principalDue)}</td>
                    <td>{money(row.interestDue)}</td>
                    <td>{money(row.penaltyDue)}</td>
                    <td>{money(row.amountPaid)}</td>
                    <td>{money(row.remaining)}</td>
                    <td>{row.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <h3>Recorded payments</h3>
          {account.payments.length ? (
            <ul>
              {account.payments.map((p) => (
                <li key={p.reference}>
                  {p.reference}: {money(p.amount)} · {date(p.recordedAt)} ·{" "}
                  {p.recordedBy}
                </li>
              ))}
            </ul>
          ) : (
            <p>No payments recorded yet.</p>
          )}
        </>
      )}
    </section>
  );
}
