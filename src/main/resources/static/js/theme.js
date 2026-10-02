(() => {
    "use strict";

    const STORAGE_KEY = "kwizz-theme";
    const root = document.documentElement;

    function getPreferredTheme() {
        const saved = localStorage.getItem(STORAGE_KEY);
        if (saved === "light" || saved === "dark") {
            return saved;
        }

        return window.matchMedia("(prefers-color-scheme: dark)").matches
            ? "dark"
            : "light";
    }

    function applyTheme(theme) {
        root.dataset.theme = theme;

        const button = document.querySelector(".kwizz-theme-toggle");
        if (!button) return;

        const dark = theme === "dark";
        button.textContent = dark ? "☀" : "☾";
        button.setAttribute(
            "aria-label",
            dark ? "Switch to light theme" : "Switch to dark theme"
        );
        button.title = dark ? "Light theme" : "Dark theme";
    }

    function createThemeToggle() {
        if (document.querySelector(".kwizz-theme-toggle")) return;

        const button = document.createElement("button");
        button.type = "button";
        button.className = "kwizz-theme-toggle";
        button.addEventListener("click", () => {
            const next = root.dataset.theme === "dark" ? "light" : "dark";
            localStorage.setItem(STORAGE_KEY, next);
            applyTheme(next);
        });

        document.body.appendChild(button);
        applyTheme(root.dataset.theme);
    }

    // Apply before the page becomes interactive to minimize theme flashing.
    applyTheme(getPreferredTheme());

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", createThemeToggle);
    } else {
        createThemeToggle();
    }

    // Keep the UI in sync with system preference when the user has not
    // explicitly selected a theme.
    const media = window.matchMedia("(prefers-color-scheme: dark)");
    media.addEventListener?.("change", event => {
        if (!localStorage.getItem(STORAGE_KEY)) {
            applyTheme(event.matches ? "dark" : "light");
        }
    });
})();
