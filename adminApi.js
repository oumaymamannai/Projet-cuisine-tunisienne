const ADMIN_TOKEN_KEY = "adminToken";

function getAdminToken() {
  return localStorage.getItem(ADMIN_TOKEN_KEY);
}

function adminHeaders(extra = {}) {
  const token = getAdminToken();
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...extra,
  };
}

async function adminFetch(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: adminHeaders(options.headers || {}),
  });

  if (response.status === 401 || response.status === 403) {
    localStorage.removeItem(ADMIN_TOKEN_KEY);
    window.location.href = "/admin";
    throw new Error("Session expirée");
  }

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    throw new Error((body && body.message) || "Erreur serveur");
  }

  return body;
}

function ensureAdminAuthenticated() {
  if (!getAdminToken()) {
    window.location.href = "/admin";
  }
}
