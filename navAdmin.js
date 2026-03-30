/* ===========================
  MONDÉLYS – nav.js
  Shared navigation logic
  (sidebar highlight + clock)
  =========================== */

// Highlight active nav link based on current page filename
(function highlightNav() {
  const currentPage = window.location.pathname.split("/").pop() || "index.html";
  document.querySelectorAll("[data-page]").forEach((link) => {
    if (link.dataset.page === currentPage) link.classList.add("active");
  });
})();

(function setupSiteLink() {
  document.querySelectorAll(".btn-sm.outline").forEach((button) => {
    if (button.textContent && button.textContent.includes("Voir le Site")) {
      button.setAttribute("href", "/");
      button.setAttribute("target", "_blank");
      button.setAttribute("rel", "noopener noreferrer");
    }
  });
})();

(function applyAdminIdentity() {
  const storedName = localStorage.getItem("adminName");
  const storedRole = localStorage.getItem("adminRole");

  if (storedName) {
    document.querySelectorAll(".sidebar-user strong").forEach((node) => {
      node.textContent = storedName;
    });
  }

  if (storedRole) {
    const roleLabel = storedRole === "ADMIN" ? "Administrateur" : storedRole;

    document.querySelectorAll(".sidebar-user span").forEach((node) => {
      node.textContent = roleLabel;
    });

    document.querySelectorAll(".sidebar-role").forEach((node) => {
      node.textContent = roleLabel;
    });
  }
})();

// Mobile hamburger
const hamburgerAdmin = document.getElementById("hamburgerAdmin");
if (hamburgerAdmin) {
  hamburgerAdmin.addEventListener("click", () => {
    const sidebar = document.getElementById("sidebar");
    if (sidebar) sidebar.classList.toggle("open");
  });
}

// Clock
function updateClock() {
  const now = new Date();
  const el = document.getElementById("clock");
  if (el)
    el.textContent =
      now.toLocaleDateString("fr-FR", {
        weekday: "short",
        day: "2-digit",
        month: "short",
      }) +
      " · " +
      now.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
}
updateClock();
setInterval(updateClock, 1000);

// Toast utility (shared across all pages)
function showToast(msg, success = false) {
  const el = document.createElement("div");
  el.className = "toast" + (success ? " success" : "");
  el.innerHTML = `<i class="fas ${success ? "fa-check-circle" : "fa-exclamation-circle"}"></i> ${msg}`;
  document.getElementById("toastContainer").appendChild(el);
  requestAnimationFrame(() =>
    requestAnimationFrame(() => el.classList.add("show")),
  );
  setTimeout(() => {
    el.classList.remove("show");
    setTimeout(() => el.remove(), 400);
  }, 3200);
}

// Modal utilities (shared)
function openModal(id) {
  document.getElementById(id).classList.add("open");
}
function closeModal(id) {
  document.getElementById(id).classList.remove("open");
}
document.querySelectorAll(".modal-overlay").forEach((m) => {
  m.addEventListener("click", (e) => {
    if (e.target === m) m.classList.remove("open");
  });
});

document.querySelectorAll(".logout-btn").forEach((button) => {
  button.addEventListener("click", async () => {
    try {
      await fetch("/api/admin/auth/logout", {
        method: "POST",
        credentials: "same-origin",
      });
    } catch (_) {
    }
    if (typeof window.clearAdminSessionState === "function") {
      window.clearAdminSessionState();
    } else {
      localStorage.removeItem("adminToken");
      localStorage.removeItem("adminName");
      localStorage.removeItem("adminRole");
    }
    window.location.replace("/admin");
  });
});
