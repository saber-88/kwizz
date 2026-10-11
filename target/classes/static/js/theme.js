(() => {
    "use strict";

    const STORAGE_KEY = "kwizz-theme";
    const root = document.documentElement;
    const media = window.matchMedia("(prefers-color-scheme: dark)");

    /* ---------- Theme ---------- */

    function stored() {
        try { return localStorage.getItem(STORAGE_KEY); } catch (e) { return null; }
    }

    function preferred() {
        const saved = stored();
        if (saved === "light" || saved === "dark") return saved;
        return media.matches ? "dark" : "light";
    }

    function applyTheme(theme) {
        root.dataset.theme = theme;
        const dark = theme === "dark";
        document.querySelectorAll("[data-theme-toggle]").forEach(btn => {
            const label = dark ? "Switch to light theme" : "Switch to dark theme";
            btn.setAttribute("aria-label", label);
            btn.setAttribute("aria-pressed", String(dark));
            btn.title = label;
        });
    }

    applyTheme(preferred());

    document.addEventListener("click", event => {
        const btn = event.target.closest("[data-theme-toggle]");
        if (!btn) return;
        const next = root.dataset.theme === "dark" ? "light" : "dark";
        try { localStorage.setItem(STORAGE_KEY, next); } catch (e) { /* ignore */ }
        applyTheme(next);
    });

    // Follow the OS setting until the user picks a theme themselves.
    media.addEventListener?.("change", event => {
        if (!stored()) applyTheme(event.matches ? "dark" : "light");
    });

    /* ---------- Mobile menu ---------- */

    const header = document.querySelector(".navbar");
    const burger = header && header.querySelector(".nav-burger");

    function setMenu(open) {
        if (!header || !burger) return;
        header.classList.toggle("menu-open", open);
        burger.setAttribute("aria-expanded", String(open));
        burger.setAttribute("aria-label", open ? "Close menu" : "Open menu");
    }

    if (burger) {
        burger.addEventListener("click", () => setMenu(!header.classList.contains("menu-open")));
        document.addEventListener("keydown", e => { if (e.key === "Escape") setMenu(false); });
        document.addEventListener("click", e => { if (!header.contains(e.target)) setMenu(false); });
    }
})();
