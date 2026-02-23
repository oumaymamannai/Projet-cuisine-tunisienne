const ADMIN_TOKEN_KEY = "adminToken";

const form = document.getElementById("adminLoginForm");
const errorEl = document.getElementById("adminLoginError");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  errorEl.textContent = "";

  const email = document.getElementById("adminEmail").value.trim();
  const password = document.getElementById("adminPassword").value;

  try {
    const response = await fetch("/api/admin/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      throw new Error(body.message || "Email ou mot de passe invalide");
    }

    const data = await response.json();
    localStorage.setItem(ADMIN_TOKEN_KEY, data.token);
    localStorage.setItem("adminName", data.fullName || "Admin");
    localStorage.setItem("adminRole", data.role || "ADMIN");
    window.location.href = "DashboradAdmin.html";
  } catch (error) {
    errorEl.textContent = error.message;
  }
});
