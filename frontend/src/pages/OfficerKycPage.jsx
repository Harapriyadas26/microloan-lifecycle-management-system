import { useState } from "react";
import { api, viewDocument } from "../api/apiClient";
import {
  PageHeader,
  ErrorMessage,
  Loading,
  Empty,
  StatusBadge,
  Field,
  useLoad,
  date,
  ConfirmDialog,
} from "../components/UI";
export default function OfficerKycPage() {
  const [status, setStatus] = useState("PENDING"),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [confirmation, setConfirmation] = useState(null),
    [success, setSuccess] = useState("");
  const state = useLoad(
    () => api("/api/officer/kyc" + (status ? "?status=" + status : "")),
    [status],
  );
  async function action() {
    setBusy(true);
    setError(null);
    try {
      await api(
        "/api/officer/kyc/" + confirmation.id + "/" + confirmation.action,
        {
          method: "PATCH",
          body: confirmation.reason
            ? { reason: confirmation.reason }
            : undefined,
        },
      );
      setConfirmation(null);
      setSuccess("KYC decision recorded.");
      state.reload();
    } catch (e) {
      setError(e);
      setConfirmation(null);
    } finally {
      setBusy(false);
    }
  }
  async function view(id, download = false) {
    setBusy(true);
    try {
      await viewDocument("/api/officer/kyc/" + id + "/file", download);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeader eyebrow="OFFICER WORKSPACE" title="KYC review">
        Verify the details behind every application.
      </PageHeader>
      <ErrorMessage error={error || state.error} />
      {success && (
        <div className="alert success" role="status">
          {success}
        </div>
      )}
      <section className="panel">
        <div className="row">
          <h2>Document queue</h2>
          <Field
            label="Document status"
            as="select"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            <option value="">All statuses</option>
            {["PENDING", "VERIFIED", "REJECTED"].map((s) => (
              <option key={s}>{s}</option>
            ))}
          </Field>
        </div>
        {state.loading ? (
          <Loading />
        ) : !state.data?.length ? (
          <Empty>No documents awaiting attention in this queue.</Empty>
        ) : (
          state.data.map((d) => (
            <article key={d.id} className="document-card">
              <div className="row">
                <div>
                  <h3>{d.borrowerName}</h3>
                  <small>{d.borrowerEmail}</small>
                </div>
                <StatusBadge status={d.status} />
              </div>
              <p>
                <strong>{d.documentType}</strong> · {d.maskedDocumentNumber} ·{" "}
                {date(d.uploadedAt)}
              </p>
              {d.rejectionReason && (
                <p className="alert error">{d.rejectionReason}</p>
              )}
              <div className="actions">
                <button
                  className="secondary"
                  disabled={busy}
                  onClick={() => view(d.id)}
                >
                  View securely ↗
                </button>
                <button
                  className="text-button"
                  disabled={busy}
                  onClick={() => view(d.id, true)}
                >
                  Download
                </button>
              </div>
              {d.status === "PENDING" && (
                <form
                  className="review-form"
                  onSubmit={(e) => {
                    e.preventDefault();
                    setConfirmation({
                      id: d.id,
                      action: "reject",
                      reason: new FormData(e.currentTarget).get("reason"),
                    });
                  }}
                >
                  <Field
                    label="Rejection reason (required to reject)"
                    name="reason"
                    minLength={5}
                    maxLength={300}
                    required
                  />
                  <div className="actions">
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() =>
                        setConfirmation({ id: d.id, action: "verify" })
                      }
                    >
                      Verify document
                    </button>
                    <button className="danger secondary" disabled={busy}>
                      Reject document
                    </button>
                  </div>
                </form>
              )}
            </article>
          ))
        )}
      </section>
      {confirmation && (
        <ConfirmDialog
          title={
            confirmation.action === "verify"
              ? "Verify this document?"
              : "Reject this document?"
          }
          busy={busy}
          onClose={() => setConfirmation(null)}
          onConfirm={action}
        >
          This records your KYC review outcome.
        </ConfirmDialog>
      )}
    </>
  );
}
