const ADMIN_TOKEN_KEY = "adminToken";
const DASHBOARD_URL = "DashboradAdmin.html";

const form = document.getElementById("adminLoginForm");
const errorEl = document.getElementById("adminLoginError");

async function redirectIfSessionExists() {
  try {
    const response = await fetch("/api/admin/auth/session", {
      method: "GET",
      credentials: "same-origin",
      headers: { Accept: "application/json" },
    });

    if (!response.ok) {
      return;
    }

    const session = await response.json().catch(() => null);
    localStorage.removeItem(ADMIN_TOKEN_KEY);

    if (session?.fullName) {
      localStorage.setItem("adminName", session.fullName);
    }

    if (session?.role) {
      localStorage.setItem("adminRole", session.role);
    }

    window.location.replace(DASHBOARD_URL);
  } catch (_) {
  }
}

if (form && errorEl) {
  redirectIfSessionExists();

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorEl.textContent = "";

    const email = document.getElementById("adminEmail").value.trim();
    const password = document.getElementById("adminPassword").value;

    try {
      const response = await fetch("/api/admin/auth/login", {
        method: "POST",
        credentials: "same-origin",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.message || "Email ou mot de passe invalide");
      }

      const data = await response.json();
      localStorage.removeItem(ADMIN_TOKEN_KEY);
      localStorage.setItem("adminName", data.fullName || "Admin");
      localStorage.setItem("adminRole", data.role || "ADMIN");
      window.location.replace(DASHBOARD_URL);
    } catch (error) {
      errorEl.textContent = error.message;
    }
  });
}
