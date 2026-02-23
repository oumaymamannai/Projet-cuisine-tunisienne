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

// Mobile hamburger
document.getElementById("hamburgerAdmin").addEventListener("click", () => {
  document.getElementById("sidebar").classList.toggle("open");
});

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
