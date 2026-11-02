import { Link } from "react-router-dom";
import { api } from "../api/apiClient";
import { useAuth } from "../context/AuthContext";
import {
  PageHeader,
  ErrorMessage,
  Loading,
  StatusBadge,
  money,
  useLoad,
  Empty,
} from "../components/UI";

function nextAction(data, officer) {
  const latest = data.loans[0];
  if (officer) {
    if (data.kyc.length)
      return {
        title: "KYC review required",
        detail: `${data.kyc.length} document${data.kyc.length === 1 ? "" : "s"} pending or conflicting.`,
        label: "Open KYC queue",
        to: data.kyc.every((document) => document.status === "CONFLICT")
          ? "/officer/kyc?status=CONFLICT"
          : "/officer/kyc",
      };
    const waiting = data.loans.filter((loan) =>
      ["SUBMITTED", "UNDER_REVIEW"].includes(loan.status),
    ).length;
    if (waiting)
      return {
        title: "Applications awaiting review",
        detail: `${waiting} application${waiting === 1 ? "" : "s"} in the queue.`,
        label: "Open loan queue",
        to: "/officer/loans",
      };
    return {
      title: "No pending reviews",
      detail: "Check portfolio and repayment activity.",
      label: "View portfolio",
      to: "/officer/reports",
    };
  }
  if (!data.profile)
    return {
      title: "Complete your profile",
      detail: "Add your contact, employment and income details.",
      label: "Open profile",
      to: "/borrower/profile",
    };
  if (
    !data.kyc.some((document) =>
      ["PENDING", "VERIFIED"].includes(document.status),
    )
  )
    return {
      title: "Upload an ID document",
      detail: "A document is required before you apply.",
      label: "Open documents",
      to: "/borrower/kyc",
    };
  if (!latest || ["REJECTED", "CANCELLED"].includes(latest.status))
    return {
      title: "Start a loan application",
      detail: "Choose an amount, purpose and repayment term.",
      label: "Apply for a loan",
      to: "/borrower/apply",
    };
  return {
    title:
      latest.status === "APPROVED"
        ? "Application approved"
        : "Application status",
    detail: `Application ML-${String(latest.id).padStart(4, "0")} · ${latest.status.replaceAll("_", " ").toLowerCase()}`,
    label:
      latest.status === "APPROVED" ? "View loan details" : "View application",
    to: "/borrower/loans/" + latest.id,
  };
}

export default function Dashboard({ officer = false }) {
  const { user } = useAuth();
  const state = useLoad(async () => {
    if (officer) {
      const [loans, kyc] = await Promise.all([
        api("/api/officer/loans"),
        api("/api/officer/kyc"),
      ]);
      return {
        loans,
        kyc: kyc.filter((document) =>
          ["PENDING", "CONFLICT"].includes(document.status),
        ),
      };
    }
    const [loans, kyc, profile] = await Promise.all([
      api("/api/borrower/loans"),
      api("/api/borrower/kyc"),
      api("/api/borrower/profile").catch((e) => {
        if (e.status === 404) return null;
        throw e;
      }),
    ]);
    return { loans, kyc, profile };
  }, [officer]);
  const d = state.data;
  const next = d && nextAction(d, officer);
  const latest = d?.loans[0];
  const submitted =
    officer && d?.loans.filter((loan) => loan.status === "SUBMITTED").length;
  const reviewing =
    officer && d?.loans.filter((loan) => loan.status === "UNDER_REVIEW").length;
  const verified =
    !officer &&
    d?.kyc.filter((document) => document.status === "VERIFIED").length;
  const kycProgress = verified
    ? "Verified"
    : d?.kyc.some((document) => document.status === "PENDING")
      ? "Pending"
      : d?.kyc.some((document) => document.status === "CONFLICT")
        ? "Conflict"
        : d?.kyc.length
          ? "Needs update"
          : "Needed";
  const snapshot =
    d &&
    (officer
      ? [
          ["KYC to review", d.kyc.length],
          ["New loans", submitted],
          ["In review", reviewing],
        ]
      : [
          ["Profile", d.profile ? "Done" : "Needed"],
          ["KYC", kycProgress],
          [
            "Application",
            latest ? latest.status.replaceAll("_", " ") : "Not started",
          ],
        ]);
  const links = officer
    ? [
        ["/officer/kyc", "KYC review", "Verify documents"],
        ["/officer/loans", "Loan queue", "Review applications"],
        ["/officer/reports", "Portfolio", "Track repayments"],
      ]
    : [
        ["/borrower/profile", "Profile", "Update your details"],
        ["/borrower/kyc", "Documents", "Check KYC status"],
        ["/borrower/loans", "Applications", "View your loans"],
      ];
  return (
    <>
      <PageHeader title={officer ? "Officer overview" : "Account overview"}>
        Welcome, {user.fullName}.
      </PageHeader>
      <ErrorMessage error={state.error} />
      {state.loading ? (
        <Loading />
      ) : (
        d && (
          <>
            <section className="hero-card">
              <div>
                <div className="eyebrow">YOUR NEXT STEP</div>
                <h2>{next.title}</h2>
                <p>{next.detail}</p>
                <Link className="button light" to={next.to}>
                  {next.label} ↗
                </Link>
              </div>
              <div
                className="hero-art"
                aria-label={officer ? "Current queue" : "Your progress"}
              >
                <div className="hero-window">
                  <div className="hero-window-bar">
                    <img src="/microloan-mark.svg" alt="" />
                    <span>microloan</span>
                    <span className="hero-window-dots">•••</span>
                  </div>
                  <div className="hero-window-heading">
                    {officer ? "Queue overview" : "Your progress"}
                  </div>
                  <div className="hero-window-steps">
                    {snapshot.map(([label, value]) => (
                      <span key={label}>
                        <small>{label}</small>
                        <b>{value}</b>
                      </span>
                    ))}
                  </div>
                  {latest && (
                    <div className="hero-window-note">
                      Latest application · ML-
                      {String(latest.id).padStart(4, "0")}
                    </div>
                  )}
                </div>
              </div>
            </section>
            <div className="stats-grid">
              {(officer
                ? [
                    [d.kyc.length, "KYC to review", "Pending or conflicting"],
                    [submitted, "New applications", "Awaiting review"],
                    [reviewing, "Under review", "Decision pending"],
                  ]
                : [
                    [
                      d.profile ? "Complete" : "Needed",
                      "Profile",
                      "Contact and income",
                    ],
                    [
                      verified,
                      "Verified documents",
                      d.kyc.length + " submitted",
                    ],
                    [
                      d.loans.length,
                      "Applications",
                      latest
                        ? "Latest: " +
                          latest.status.replaceAll("_", " ").toLowerCase()
                        : "None yet",
                    ],
                  ]
              ).map(([value, label, sub], index) => (
                <div
                  className={"stat-card stat-card-" + (index + 1)}
                  key={label}
                >
                  <span>{label}</span>
                  <strong>{value}</strong>
                  <small>{sub}</small>
                </div>
              ))}
            </div>
            <div className="two-column">
              <section className="panel">
                <div className="row">
                  <h2>
                    {officer ? "Recent applications" : "Latest application"}
                  </h2>
                  <Link to={officer ? "/officer/loans" : "/borrower/loans"}>
                    View all →
                  </Link>
                </div>
                {!d.loans.length ? (
                  <Empty>No applications yet.</Empty>
                ) : (
                  d.loans.slice(0, officer ? 4 : 1).map((a) => (
                    <article className="loan-preview" key={a.id}>
                      <div className="row">
                        <strong>ML-{String(a.id).padStart(4, "0")}</strong>
                        <StatusBadge status={a.status} />
                      </div>
                      <h3>{money(a.loanAmount)}</h3>
                      <p>
                        {a.purpose.replaceAll("_", " ")} · {a.tenureMonths}{" "}
                        months
                      </p>
                      <Link
                        to={
                          (officer ? "/officer" : "/borrower") +
                          "/loans/" +
                          a.id
                        }
                      >
                        View details →
                      </Link>
                    </article>
                  ))
                )}
              </section>
              <section className="panel quick-actions">
                <h2>Quick access</h2>
                <div className="quick-action-list">
                  {links.map(([to, title, detail]) => (
                    <Link className="quick-action" to={to} key={to}>
                      <span>
                        <strong>{title}</strong>
                        <small>{detail}</small>
                      </span>
                      <b>↗</b>
                    </Link>
                  ))}
                </div>
              </section>
            </div>
          </>
        )
      )}
    </>
  );
}
