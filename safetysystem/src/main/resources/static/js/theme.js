function applySavedTheme() {
    const savedTheme = localStorage.getItem("dashboard-theme");
    if (savedTheme === "dark") {
        document.body.classList.add("dark-mode");
    } else {
        document.body.classList.remove("dark-mode");
    }
    updateThemeButtonText();
    updateLogoByTheme();

    if (typeof refreshChartTheme === "function") {
        refreshChartTheme();
    }
}

function toggleTheme() {
    document.body.classList.toggle("dark-mode");
    const isDark = document.body.classList.contains("dark-mode");
    localStorage.setItem("dashboard-theme", isDark ? "dark" : "light");
    updateThemeButtonText();
    updateLogoByTheme();

    if (typeof refreshChartTheme === "function") {
        refreshChartTheme();
    }
}

function updateThemeButtonText() {
    const btn = document.getElementById("theme-toggle-btn");
    if (!btn) return;

    btn.innerText = document.body.classList.contains("dark-mode")
        ? "☀️ 라이트모드"
        : "🌙 다크네이비";
}

function updateLogoByTheme() {
    const logo = document.getElementById("main-logo");
    if (!logo) return;

    if (document.body.classList.contains("dark-mode")) {
        logo.src = logo.dataset.dark;
    } else {
        logo.src = logo.dataset.light;
    }
}

document.addEventListener("DOMContentLoaded", applySavedTheme);