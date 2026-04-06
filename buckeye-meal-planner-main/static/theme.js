// Dark mode toggle — persists preference in localStorage
(function () {
  var toggle = document.getElementById("themeToggle");
  var icon = document.getElementById("themeIcon");
  var html = document.documentElement;

  // Load saved preference (fallback to OS preference)
  var saved = localStorage.getItem("theme");
  if (
    saved === "dark" ||
    (!saved && window.matchMedia("(prefers-color-scheme: dark)").matches)
  ) {
    html.setAttribute("data-theme", "dark");
    if (icon) icon.innerHTML = "\u2600"; // sun
  }

  if (toggle) {
    toggle.addEventListener("click", function () {
      var isDark = html.getAttribute("data-theme") === "dark";
      if (isDark) {
        html.removeAttribute("data-theme");
        localStorage.setItem("theme", "light");
        if (icon) icon.innerHTML = "\u263E"; // moon
      } else {
        html.setAttribute("data-theme", "dark");
        localStorage.setItem("theme", "dark");
        if (icon) icon.innerHTML = "\u2600"; // sun
      }
    });
  }
})();
