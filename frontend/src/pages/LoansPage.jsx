import { useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/apiClient";
import {
  PageHeader,
  ErrorMessage,
  Loading,
  Empty,
  StatusBadge,
  money,
  date,
  useLoad,
  Field,
} from "../components/UI";
export default function LoansPage({ officer = false }) {
  const [status, setStatus] = useState("");
  const state = useLoad(
    () =>
      api(
        "/api/" +
          (officer ? "officer" : "borrower") +
          "/loans" +
          (status ? "?status=" + status : ""),
      ),
    [status, officer],
  );
  return (
    <>
      <PageHeader title={officer ? "Loan queue" : "Your applications"}>
        {officer
          ? "Review each application with the full picture."
          : "Every application, every update, in one place."}
      </PageHeader>
      <section className="panel">
        <div className="row">
          <h2>{officer ? "All applications" : "Application history"}</h2>
          {officer ? (
            <Field
              label="Filter by status"
              as="select"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
            >
              <option value="">All statuses</option>
              {[
                "SUBMITTED",
                "UNDER_REVIEW",
                "APPROVED",
                "REJECTED",
                "CANCELLED",
              ].map((s) => (
                <option key={s}>{s}</option>
              ))}
            </Field>
          ) : (
            <Link className="button" to="/borrower/apply">
              New application ＋
            </Link>
          )}
        </div>
        <ErrorMessage error={state.error} />
        {state.loading ? (
          <Loading />
        ) : !state.data?.length ? (
          <Empty>
            No applications {status ? "with this status" : "yet"}.{" "}
            {officer
              ? "New submissions will appear here."
              : "Your next step starts with your first application."}
          </Empty>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Application</th>
                  {officer && <th>Borrower</th>}
                  <th>Amount</th>
                  <th>Purpose</th>
                  <th>Submitted</th>
                  <th>Status</th>
                  <th>
                    <span className="sr-only">Details</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {state.data.map((a) => (
                  <tr key={a.id}>
                    <td>
                      <strong>ML-{String(a.id).padStart(4, "0")}</strong>
                      <small>{a.tenureMonths} months</small>
                    </td>
                    {officer && <td>{a.borrowerName}</td>}
                    <td>{money(a.loanAmount)}</td>
                    <td>{a.purpose.replaceAll("_", " ")}</td>
                    <td>{date(a.submittedAt)}</td>
                    <td>
                      <StatusBadge status={a.status} />
                    </td>
                    <td>
                      <Link
                        to={
                          (officer ? "/officer" : "/borrower") +
                          "/loans/" +
                          a.id
                        }
                      >
                        View →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}
