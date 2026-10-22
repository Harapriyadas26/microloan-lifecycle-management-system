const base = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
export class ApiError extends Error {
  constructor(message, status, fields = {}) {
    super(message);
    this.status = status;
    this.fields = fields;
  }
}
export async function api(
  path,
  { method = "GET", body, blob = false, ...options } = {},
) {
  const session = readSession();
  const headers = { ...options.headers };
  if (session?.token) headers.Authorization = "Bearer " + session.token;
  if (body && !(body instanceof FormData))
    headers["Content-Type"] = "application/json";
  let response;
  try {
    response = await fetch(base + path, {
      ...options,
      method,
      headers,
      body:
        body instanceof FormData
          ? body
          : body
            ? JSON.stringify(body)
            : undefined,
    });
  } catch {
    throw new ApiError(
      "Cannot reach the server. Check that the backend is running.",
      0,
    );
  }
  if (!response.ok) {
    let error;
    try {
      error = await response.json();
    } catch {
      error = {};
    }
    if (response.status === 401 && session) {
      sessionStorage.removeItem("microloan.session");
      window.dispatchEvent(new Event("session-expired"));
    }
    throw new ApiError(
      error.message || "Unable to complete this request",
      response.status,
      error.validationErrors || {},
    );
  }
  return blob
    ? response.blob()
    : response.status === 204
      ? null
      : response.json();
}
export function readSession() {
  try {
    return JSON.parse(sessionStorage.getItem("microloan.session"));
  } catch {
    return null;
  }
}
export async function viewDocument(path, download = false) {
  const blob = await api(path + "?download=" + download, { blob: true }),
    url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  if (download)
    a.download =
      "kyc-document." +
      (blob.type === "application/pdf"
        ? "pdf"
        : blob.type === "image/png"
          ? "png"
          : "jpg");
  else {
    a.target = "_blank";
    a.rel = "noopener noreferrer";
  }
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 60000);
}
