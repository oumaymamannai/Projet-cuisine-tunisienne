/* ===========================
   CARTHÉLYS – nav.js
   Shared navigation logic
   (sidebar highlight + clock)
   =========================== */

// Highlight active nav link based on current page filename
(function highlightNav() {
  const page = window.location.pathname.split("/").pop() || "index.html";
  document.querySelectorAll("[data-page]").forEach((link) => {
    if (link.dataset.page === page) link.classList.add("active");
  });
})();

(function protectAdminPages() {
  const adminPages = ["DashboradAdmin.html", "resAdmin.html", "reviwAdmin.html", "contactAdmin.html", "settingsAdmin.html"];
  const currentPage = window.location.pathname.split("/").pop();
  if (!adminPages.includes(currentPage)) {
    return;
  }

  if (!localStorage.getItem("adminToken")) {
    window.location.href = "/admin";
    return;
  }

  const adminName = localStorage.getItem("adminName");
  if (adminName) {
    const userNameEl = document.querySelector(".sidebar-user strong");
    if (userNameEl) userNameEl.textContent = adminName;
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
  button.addEventListener("click", () => {
    localStorage.removeItem("adminToken");
    localStorage.removeItem("adminName");
    localStorage.removeItem("adminRole");
    window.location.href = "/admin";
  });
});
