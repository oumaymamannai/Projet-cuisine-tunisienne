const ADMIN_TOKEN_KEY = "adminToken";
const ADMIN_LOGIN_URL = "/admin";
const ADMIN_LOGIN_PAGE = "/adminLogin.html";
const ADMIN_NAME_KEY = "adminName";
const ADMIN_ROLE_KEY = "adminRole";

let adminSessionPromise = null;

function clearAdminSessionState() {
  localStorage.removeItem(ADMIN_TOKEN_KEY);
  localStorage.removeItem(ADMIN_NAME_KEY);
  localStorage.removeItem(ADMIN_ROLE_KEY);
  adminSessionPromise = null;
}

function persistAdminSession(session) {
  if (!session) return;

  localStorage.removeItem(ADMIN_TOKEN_KEY);

  if (session.fullName) {
    localStorage.setItem(ADMIN_NAME_KEY, session.fullName);
  }

  if (session.role) {
    localStorage.setItem(ADMIN_ROLE_KEY, session.role);
  }
}

function redirectToAdminLogin() {
  const currentPath = window.location.pathname;
  if (currentPath === ADMIN_LOGIN_URL || currentPath.endsWith(ADMIN_LOGIN_PAGE)) {
    return;
  }
  window.location.replace(ADMIN_LOGIN_URL);
}

function getAdminToken() {
  return localStorage.getItem(ADMIN_TOKEN_KEY);
}

function adminHeaders(extra = {}, hasBody = false) {
  return {
    ...(hasBody ? { "Content-Type": "application/json" } : {}),
    ...extra,
  };
}

async function ensureAdminAuthenticated(forceRefresh = false) {
  if (forceRefresh || !adminSessionPromise) {
    adminSessionPromise = fetch("/api/admin/auth/session", {
      method: "GET",
      credentials: "same-origin",
      headers: { Accept: "application/json" },
    })
      .then(async (response) => {
        const contentType = response.headers.get("content-type") || "";
        const body = contentType.includes("application/json")
          ? await response.json().catch(() => null)
          : null;

        if (!response.ok) {
          throw new Error((body && body.message) || "Session expirée");
        }

        persistAdminSession(body);
        return body;
      })
      .catch((error) => {
        clearAdminSessionState();
        redirectToAdminLogin();
        throw error;
      });
  }

  return adminSessionPromise;
}

async function adminFetch(url, options = {}) {
  await ensureAdminAuthenticated();

  const response = await fetch(url, {
    ...options,
    credentials: "same-origin",
    headers: adminHeaders(options.headers || {}, options.body != null),
  });

  if (response.status === 401 || response.status === 403) {
    clearAdminSessionState();
    redirectToAdminLogin();
    throw new Error("Session expirée");
  }

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    throw new Error((body && body.message) || "Erreur serveur");
  }

  return body;
}

window.clearAdminSessionState = clearAdminSessionState;
