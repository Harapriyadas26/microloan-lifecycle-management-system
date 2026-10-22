import { useEffect, useState, useRef } from "react";
export const money = (value) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(value ?? 0);
export const date = (value) =>
  value ? new Date(value).toLocaleString("en-IN") : "—";
export function StatusBadge({ status }) {
  return (
    <span className={"badge " + (status || "").toLowerCase()}>
      {(status || "Unavailable").replaceAll("_", " ")}
    </span>
  );
}
export function ErrorMessage({ error }) {
  return error ? (
    <div className="alert error" role="alert">
      <strong>{error.message || String(error)}</strong>
      {Object.entries(error.fields || {}).map(([key, value]) => (
        <div key={key}>
          {key}: {value}
        </div>
      ))}
    </div>
  ) : null;
}
export function Loading() {
  return (
    <div className="loading" role="status">
      <span /> Loading your workspace…
    </div>
  );
}
export function Empty({ children }) {
  return <div className="empty">{children}</div>;
}
export function Field({ label, name, as = "input", children, ...props }) {
  const Tag = as;
  return (
    <label className="field">
      <span>{label}</span>
      <Tag name={name} {...props}>
        {children}
      </Tag>
    </label>
  );
}
export function PageHeader({ eyebrow = "YOUR WORKSPACE", title, children }) {
  return (
    <header className="page-header">
      <div>
        <div className="eyebrow">{eyebrow}</div>
        <h1>{title}</h1>
        {children && <p>{children}</p>}
      </div>
    </header>
  );
}
export function useLoad(loader, deps = []) {
  const [state, setState] = useState({
    data: null,
    error: null,
    loading: true,
  });
  const [version, setVersion] = useState(0);
  useEffect(() => {
    let active = true;
    setState((s) => ({ ...s, error: null, loading: true }));
    loader()
      .then((data) => active && setState({ data, error: null, loading: false }))
      .catch(
        (error) => active && setState({ data: null, error, loading: false }),
      );
    return () => {
      active = false;
    };
  }, [...deps, version]);
  return { ...state, reload: () => setVersion((v) => v + 1) };
}
export function ConfirmDialog({
  title,
  children,
  onConfirm,
  onClose,
  busy = false,
}) {
  const dialog = useRef(null),
    close = useRef(onClose);
  close.current = onClose;
  useEffect(() => {
    const prior = document.activeElement;
    const node = dialog.current;
    node.showModal();
    return () => {
      node.close();
      prior?.focus();
    };
  }, []);
  return (
    <dialog
      ref={dialog}
      className="modal"
      aria-label={title}
      onCancel={(e) => {
        e.preventDefault();
        if (!busy) close.current();
      }}
    >
      <h2>{title}</h2>
      <p>{children}</p>
      <div className="actions">
        <button
          autoFocus
          type="button"
          className="secondary"
          disabled={busy}
          onClick={onClose}
        >
          Go back
        </button>
        <button type="button" disabled={busy} onClick={onConfirm}>
          {busy ? "Saving…" : "Confirm"}
        </button>
      </div>
    </dialog>
  );
}
