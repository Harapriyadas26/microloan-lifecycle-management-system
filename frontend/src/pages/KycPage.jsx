import { useState } from "react";
import { api, viewDocument } from "../api/apiClient";
import {
  Field,
  PageHeader,
  ErrorMessage,
  Loading,
  StatusBadge,
  Empty,
  date,
  useLoad,
} from "../components/UI";
export default function KycPage() {
  const state = useLoad(() => api("/api/borrower/kyc"));
  const [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [success, setSuccess] = useState("");
  async function upload(e) {
    e.preventDefault();
    const form = e.currentTarget;
    setBusy(true);
    setError(null);
    setSuccess("");
    try {
      const body = new FormData(form);
      if (body.get("file").size > 5 * 1024 * 1024)
        throw new Error("Maximum file size is 5 MB");
      await api("/api/borrower/kyc", { method: "POST", body });
      form.reset();
      setSuccess("Document submitted for verification.");
      state.reload();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  async function view(id, download) {
    setBusy(true);
    try {
      await viewDocument("/api/borrower/kyc/" + id + "/file", download);
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <PageHeader title="Your documents">
        A secure first step towards your loan application.
      </PageHeader>
      <ErrorMessage error={error || state.error} />
      {success && (
        <div className="alert success" role="status">
          {success}
        </div>
      )}
      <div className="two-column">
        <form className="panel" onSubmit={upload}>
          <div className="eyebrow">IDENTITY VERIFICATION</div>
          <h2>Submit a document</h2>
          <p className="muted">PDF, JPG or PNG · Maximum 5 MB</p>
          <Field as="select" name="documentType" label="Document type" required>
            <option value="">Select a type</option>
            {["AADHAAR", "PAN", "VOTER_ID", "PASSPORT", "DRIVING_LICENSE"].map(
              (t) => (
                <option key={t}>{t}</option>
              ),
            )}
          </Field>
          <Field
            label="Document number"
            name="documentNumber"
            required
            minLength={4}
            maxLength={30}
          />
          <Field
            label="Document file"
            name="file"
            type="file"
            accept="application/pdf,image/jpeg,image/png"
            required
          />
          <div className="notice">
            Demo only: use a fictional document marked “NOT A REAL ID”.
          </div>
          <button disabled={busy}>
            {busy ? "Working…" : "Upload document →"}
          </button>
        </form>
        <section className="panel">
          <h2>Submitted documents</h2>
          <p className="muted">
            One document per type. Verified documents cannot be replaced.
          </p>
          {state.loading ? (
            <Loading />
          ) : !state.data?.length ? (
            <Empty>Your documents will appear here after upload.</Empty>
          ) : (
            state.data.map((d) => (
              <article className="document-card" key={d.id}>
                <div className="row">
                  <strong>{d.documentType.replaceAll("_", " ")}</strong>
                  <StatusBadge status={d.status} />
                </div>
                <p>{d.maskedDocumentNumber}</p>
                <small className="muted">Uploaded {date(d.uploadedAt)}</small>
                {d.rejectionReason && (
                  <div className="alert error">{d.rejectionReason}</div>
                )}
                <div className="actions">
                  <button
                    type="button"
                    className="secondary"
                    disabled={busy}
                    onClick={() => view(d.id, false)}
                  >
                    View document
                  </button>
                  <button
                    type="button"
                    className="text-button"
                    disabled={busy}
                    onClick={() => view(d.id, true)}
                  >
                    Download
                  </button>
                </div>
              </article>
            ))
          )}
        </section>
      </div>
    </>
  );
}
