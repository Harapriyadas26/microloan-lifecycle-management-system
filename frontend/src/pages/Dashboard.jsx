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
export default function Dashboard({ officer = false }) {
  const { user } = useAuth();
  const state = useLoad(async () => {
    if (officer) {
      const [loans, kyc] = await Promise.all([
        api("/api/officer/loans"),
        api("/api/officer/kyc?status=PENDING"),
      ]);
      return { loans, kyc };
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
  return (
    <>
      <PageHeader title={"Hello, " + user.fullName.split(" ")[0] + "."}>
        {officer
          ? "Here’s what needs your attention today."
          : "Your goals deserve a clear path. Let’s take the next step."}
      </PageHeader>
      <ErrorMessage error={state.error} />
      {state.loading ? (
        <Loading />
      ) : (
        d && (
          <>
            <section className="hero-card">
              <div>
                <div className="eyebrow">
                  {officer
                    ? "MAKE INFORMED DECISIONS"
                    : "A LITTLE SUPPORT GOES A LONG WAY"}
                </div>
                <h2>
                  {officer ? (
                    <>
                      A full picture.
                      <br />A considered decision.
                    </>
                  ) : (
                    <>
                      Big plans start
                      <br />
                      with small steps.
                    </>
                  )}
                </h2>
                <p>
                  {officer
                    ? "Transparent risk factors and verified documents, together."
                    : "From your first application to a clear decision — we’re with you."}
                </p>
                <Link
                  className="button light"
                  to={officer ? "/officer/loans" : "/borrower/apply"}
                >
                  {officer ? "Open loan queue" : "Apply for a loan"} ↗
                </Link>
              </div>
              <div className="hero-art" aria-hidden="true">
                <div className="arc arc-one" />
                <div className="arc arc-two" />
                <div className="arc arc-three" />
                <span>↗</span>
                <small>GROW AT YOUR PACE</small>
              </div>
            </section>
            <div className="stats-grid">
              {(officer
                ? [
                    [
                      d.kyc.length,
                      "Pending documents",
                      "Awaiting verification",
                    ],
                    [
                      d.loans.filter((a) => a.status === "SUBMITTED").length,
                      "New applications",
                      "Ready for review",
                    ],
                    [
                      d.loans.filter((a) => a.status === "UNDER_REVIEW").length,
                      "Under review",
                      "Decisions in progress",
                    ],
                  ]
                : [
                    [
                      d.profile ? "Complete" : "Get started",
                      "Your profile",
                      "Personal & income details",
                    ],
                    [
                      d.kyc.filter((k) => k.status === "VERIFIED").length,
                      "Verified documents",
                      d.kyc.length + " documents submitted",
                    ],
                    [d.loans.length, "Applications", "Track your progress"],
                  ]
              ).map(([value, label, sub]) => (
                <div className="stat-card" key={label}>
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
                    {officer
                      ? "Recent applications"
                      : "Your latest application"}
                  </h2>
                  <Link to={officer ? "/officer/loans" : "/borrower/loans"}>
                    View all →
                  </Link>
                </div>
                {!d.loans.length ? (
                  <Empty>
                    No applications yet. Your loan journey will appear here.
                  </Empty>
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
                        See application details →
                      </Link>
                    </article>
                  ))
                )}
              </section>
              <section className="panel journey">
                <div className="eyebrow">
                  {officer
                    ? "REVIEW WITH CONFIDENCE"
                    : "YOUR JOURNEY, MADE SIMPLE"}
                </div>
                <h2>
                  {officer ? "A thoughtful review." : "Know what comes next."}
                </h2>
                {(officer
                  ? [
                      "Review submitted KYC",
                      "Read the risk factors",
                      "Record a reasoned decision",
                    ]
                  : [
                      "Complete your borrower profile",
                      "Submit a KYC document",
                      "Apply & understand your assessment",
                      "Follow your officer’s decision",
                    ]
                ).map((text, i) => (
                  <div className="journey-step" key={text}>
                    <span>{String(i + 1).padStart(2, "0")}</span>
                    <div>{text}</div>
                  </div>
                ))}
                <div className="notice">
                  Risk assessments explain the factors. A loan officer makes the
                  final decision.
                </div>
              </section>
            </div>
          </>
        )
      )}
    </>
  );
}
