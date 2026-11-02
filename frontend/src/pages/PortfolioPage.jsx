import { api } from "../api/apiClient";
import {
  ErrorMessage,
  Loading,
  PageHeader,
  money,
  useLoad,
} from "../components/UI";

export default function PortfolioPage() {
  const state = useLoad(() => api("/api/officer/reports/portfolio"));
  const report = state.data;
  const metrics = report && [
    ["Active loans", report.activeLoans],
    ["Closed loans", report.closedLoans],
    ["Overdue installments", report.overdueInstallments],
    ["Due within seven days", report.dueWithinSevenDays],
    ["Recorded disbursements", money(report.totalDisbursed)],
    ["Outstanding incl. late charges", money(report.outstanding)],
  ];

  return (
    <>
      <PageHeader eyebrow="OFFICER WORKSPACE" title="Portfolio report">
        Demo balances only. No funds are transferred.
      </PageHeader>
      <ErrorMessage error={state.error} />
      {state.loading ? (
        <Loading />
      ) : (
        metrics && (
          <section className="stats-grid portfolio-grid">
            {metrics.map(([label, value], index) => (
              <div
                className={"stat-card stat-card-" + ((index % 3) + 1)}
                key={label}
              >
                <span>{label}</span>
                <strong>{value}</strong>
              </div>
            ))}
          </section>
        )
      )}
    </>
  );
}
