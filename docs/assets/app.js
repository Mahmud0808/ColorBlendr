// App landing page: MCU-derived dynamic coloring (rotating seed), seed chip
// demo, community creations marquee. Same engine as the themes site.
import {
    Hct,
    SchemeContent,
    SchemeExpressive,
    SchemeFidelity,
    SchemeFruitSalad,
    SchemeMonochrome,
    SchemeNeutral,
    SchemeRainbow,
    SchemeTonalSpot,
    SchemeVibrant,
    argbFromHex,
    hexFromArgb
} from "https://esm.run/@material/material-color-utilities@0.4.0";

const WORKER = "https://colorblendr-themes.drdisagree.workers.dev";
const THEMES_INDEX =
    "https://raw.githubusercontent.com/Mahmud0808/ColorBlendr-Themes/main/index.json";
const HEX = /^#[0-9a-fA-F]{6}$/;

// App enum ordinal -> MCU spec. JS lib has no 2026 yet; nearest is 2025.
const SPEC_BY_VERSION = { 0: "2021", 1: "2025", 2: "2025" };
const DEFAULT_SPEC = "2025";

const SCHEME_BY_STYLE = {
    MONOCHROMATIC: SchemeMonochrome,
    TONAL_SPOT: SchemeTonalSpot,
    VIBRANT: SchemeVibrant,
    RAINBOW: SchemeRainbow,
    EXPRESSIVE: SchemeExpressive,
    FIDELITY: SchemeFidelity,
    CONTENT: SchemeContent,
    FRUIT_SALAD: SchemeFruitSalad,
    SPRITZ: SchemeNeutral,
    CMF: SchemeTonalSpot
};

const esc = (s) => String(s ?? "").replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
}[c]));
const alpha = (hex, a) =>
    hex + Math.round(a * 255).toString(16).padStart(2, "0");

// App CAM16 slider math ports (ColorUtil.adjustSaturation / shiftLightness).
function adjustSaturation(hex, saturation) {
    if (saturation === 100) return hex;
    const satF = (saturation - 100) / 100;
    const hct = Hct.fromInt(argbFromHex(hex));
    const target = Hct.from(hct.hue, 200, hct.tone);
    let chroma = hct.chroma;
    chroma += satF > 0 ? (target.chroma - chroma) * satF : chroma * satF;
    return hexFromArgb(Hct.from(hct.hue, chroma, hct.tone).toInt());
}

function shiftLightness(hex, lightness, minTone = 0, maxTone = 100) {
    const hct = Hct.fromInt(argbFromHex(hex));
    const tone = Math.max(minTone, Math.min(maxTone,
        hct.tone + (lightness - 100) / 10));
    if (tone === hct.tone) return hex;
    return hexFromArgb(Hct.from(hct.hue, hct.chroma, tone).toInt());
}

// ---- Site-wide dynamic coloring -------------------------------------------

function applySiteSeed(seedHex, style, spec, sliders) {
    const { accentSat = 100, bgSat = 100, bgLight = 100 } = sliders ?? {};
    const Ctor = SCHEME_BY_STYLE[style] ?? SchemeTonalSpot;
    const scheme = new Ctor(
        Hct.fromInt(argbFromHex(seedHex)), true, 0, spec ?? DEFAULT_SPEC
    );
    const role = (argb) => hexFromArgb(argb);
    const accent = (argb) => adjustSaturation(role(argb), accentSat);
    // Tone floors keep slider-shifted surfaces off pure black + separated.
    const surf = (argb, minTone, maxTone) =>
        shiftLightness(adjustSaturation(role(argb), bgSat), bgLight, minTone, maxTone);

    const vars = {
        "--bg": surf(scheme.surface, 4, 99),
        "--text": role(scheme.onSurface),
        "--subtle": alpha(role(scheme.onSurfaceVariant), 0.75),
        "--body2": role(scheme.onSurfaceVariant),
        "--accent": accent(scheme.primary),
        "--on-accent": role(scheme.onPrimary),
        "--accent-glow": alpha(accent(scheme.primary), 0.32),
        "--tonal": role(scheme.secondaryContainer),
        "--on-tonal": role(scheme.onSecondaryContainer),
        "--card": surf(scheme.surfaceContainer, 10, 96),
        "--card-high": surf(scheme.surfaceContainerHigh, 14, 93),
        "--card-highest": surf(scheme.surfaceContainerHighest, 16, 91),
        "--outline-v": role(scheme.outlineVariant),
        "--grad-a": role(scheme.onSurface),
        "--grad-b": accent(scheme.primary),
        "--grad-c": accent(scheme.tertiary)
    };
    for (const [k, v] of Object.entries(vars)) {
        document.documentElement.style.setProperty(k, v);
    }

    // Hero logo disc follows the seed (launcher gradient formula).
    const stops = document.querySelectorAll("#lg stop");
    if (stops.length === 2) {
        stops[0].style.setProperty("stop-color",
            adjustSaturation(hexFromArgb(scheme.primaryPalette.tone(70)), accentSat));
        stops[1].style.setProperty("stop-color",
            adjustSaturation(hexFromArgb(scheme.primaryPalette.tone(40)), accentSat));
    }
}

// Boot color; matches the :root CSS fallbacks so first paint = first seed.
const INITIAL_SEED = "#51BDFF";

// Theme the site rests on when nothing is hovered; rotation or the
// playground moves it.
let resting = { seed: INITIAL_SEED, style: undefined, spec: undefined };
let hoverHold = false;
let demoHold = false;

// Rotation cycles the playground's own seeds so the selected swatch always
// mirrors what the page is wearing.
let demoSync = null;

function startSeedRotation() {
    let i = 0;
    setInterval(() => {
        // Playground takes permanent control; hover pauses.
        if (hoverHold || demoHold) return;
        i = (i + 1) % DEMO_SEEDS.length;
        resting = { seed: DEMO_SEEDS[i] };
        applySiteSeed(resting.seed);
        demoSync?.(resting.seed);
    }, 7000);
}

// Engine playground: app-style swatches + Monet style + spec selectors.
// Any change re-derives the whole page, exactly like picking in-app.
const DEMO_SEEDS = [
    "#51BDFF", "#F44336", "#FFB300", "#4CAF50",
    "#26A69A", "#7C4DFF", "#EC407A"
];
const DEMO_STYLES = [
    ["TONAL_SPOT", "Tonal Spot"],
    ["VIBRANT", "Vibrant"],
    ["EXPRESSIVE", "Expressive"],
    ["RAINBOW", "Rainbow"],
    ["FRUIT_SALAD", "Fruit Salad"],
    ["SPRITZ", "Spritz"],
    ["MONOCHROMATIC", "Monochrome"]
];
const DEMO_SPECS = ["2021", "2025"];

function initDemo() {
    const seedsEl = document.getElementById("demoSeeds");
    const styleWrap = document.getElementById("styleWrap");
    const specWrap = document.getElementById("specWrap");
    if (!seedsEl || !styleWrap || !specWrap) return;

    let seed = INITIAL_SEED;
    let style = "TONAL_SPOT";
    let spec = DEFAULT_SPEC;
    const root = document.documentElement;
    let clearTimer = null;

    const swatch = (s) => {
        const scheme = new (SCHEME_BY_STYLE[style])(
            Hct.fromInt(argbFromHex(s)), true, 0, spec
        );
        return swatchSvg({
            square: hexFromArgb(scheme.neutralVariantPalette.tone(30)),
            halfCircle: hexFromArgb(scheme.primaryPalette.tone(80)),
            firstQuarter: hexFromArgb(scheme.tertiaryPalette.tone(70)),
            secondQuarter: hexFromArgb(scheme.secondaryPalette.tone(60)),
            center: s
        });
    };

    const renderSeeds = () => {
        seedsEl.innerHTML = DEMO_SEEDS.map((s) =>
            `<button class="demo-swatch" data-seed="${s}" aria-label="Seed ${s}">${swatch(s)}</button>`
        ).join("");
        updateSelection();
    };

    // Class toggle, not a rebuild: the ring cross-fades between swatches.
    const updateSelection = () => {
        seedsEl.querySelectorAll(".demo-swatch").forEach((b) =>
            b.classList.toggle("on", b.dataset.seed === seed));
    };

    const apply = (rebuildSwatches) => {
        demoHold = true;
        resting = { seed, style, spec };
        if (clearTimer) clearTimeout(clearTimer);
        root.style.setProperty("--recolor", ".5s");
        applySiteSeed(seed, style, spec);
        clearTimer = setTimeout(() => root.style.removeProperty("--recolor"), 600);
        if (rebuildSwatches) renderSeeds();
        else updateSelection();
    };

    seedsEl.addEventListener("click", (e) => {
        const btn = e.target.closest(".demo-swatch");
        if (!btn) return;
        seed = btn.dataset.seed;
        apply(false);
    });

    // Custom dropdowns: native select popups ignore theming.
    const dropdown = (wrap, items, getValue, onPick) => {
        const btn = wrap.querySelector(".sortbtn");
        const label = wrap.querySelector(".dlabel");
        const menu = wrap.querySelector(".menu");
        const setOpen = (open) => {
            if (open) {
                menu.innerHTML = items.map(([v, l]) =>
                    `<button class="menuitem${v === getValue() ? " selected" : ""}" role="option" data-value="${v}">${l}</button>`
                ).join("");
            }
            menu.hidden = !open;
            wrap.classList.toggle("open", open);
            btn.setAttribute("aria-expanded", String(open));
        };
        btn.addEventListener("click", () => setOpen(menu.hidden));
        menu.addEventListener("click", (e) => {
            const item = e.target.closest(".menuitem");
            if (!item) return;
            label.textContent = item.textContent;
            setOpen(false);
            onPick(item.dataset.value);
        });
        document.addEventListener("click", (e) => {
            if (!wrap.contains(e.target)) setOpen(false);
        });
        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") setOpen(false);
        });
    };
    dropdown(styleWrap, DEMO_STYLES, () => style,
        (v) => { style = v; apply(true); });
    dropdown(specWrap, DEMO_SPECS.map((v) => [v, `Spec ${v}`]), () => spec,
        (v) => { spec = v; apply(true); });

    // Ambient rotation drives the selection ring, not the other way around.
    demoSync = (s) => {
        seed = s;
        updateSelection();
    };

    renderSeeds();
}

// Hovered card retints the whole site with its seed; leave reverts.
// --recolor shortens every themed transition while the hover drives it.
function initHoverTheming(container) {
    if (!container || !matchMedia("(hover: hover)").matches) return;
    const root = document.documentElement;
    let activeSeed = null;
    let clearTimer = null;
    container.addEventListener("mouseover", (e) => {
        const card = e.target.closest?.(".tcard");
        const seed = card?.dataset.seed;
        if (!seed || seed === activeSeed) return;
        activeSeed = seed;
        hoverHold = true;
        if (clearTimer) clearTimeout(clearTimer);
        root.style.setProperty("--recolor", ".5s");
        applySiteSeed(seed, card.dataset.style, card.dataset.spec, {
            accentSat: +card.dataset.asat || 100,
            bgSat: +card.dataset.bsat || 100,
            bgLight: +card.dataset.blight || 100
        });
    });
    container.addEventListener("mouseout", (e) => {
        const card = e.target.closest?.(".tcard");
        if (!card || card.contains(e.relatedTarget)) return;
        if (e.relatedTarget?.closest?.(".tcard")) return;
        activeSeed = null;
        hoverHold = false;
        applySiteSeed(resting.seed, resting.style, resting.spec);
        clearTimer = setTimeout(() => root.style.removeProperty("--recolor"), 600);
    });
}

// ---- Community theme cards ---------------------------------------------------

// App ColorsScreen swatch: square = neutral2 tone30, top half = accent1
// tone80, bottom-left = accent3 tone70, bottom-right = accent2 tone60,
// center dot = seed. Overrides + sliders honored per cell.
function cardData(theme) {
    const seed = HEX.test(theme.seedColor ?? "") ? theme.seedColor : "#6750A4";
    const Ctor = SCHEME_BY_STYLE[theme.style] ?? SchemeTonalSpot;
    const spec = SPEC_BY_VERSION[theme.colorSpecVersion] ?? DEFAULT_SPEC;
    const scheme = new Ctor(Hct.fromInt(argbFromHex(seed)), true, 0, spec);
    const isMono = theme.style === "MONOCHROMATIC";
    const accentSat = isMono ? 100 : (theme.accentSaturation ?? 100);
    const bgSat = isMono ? 100 : (theme.backgroundSaturation ?? 100);
    const bgLight = isMono ? 100 : (theme.backgroundLightness ?? 100);

    const customPalette = (hex) => HEX.test(hex ?? "")
        ? new Ctor(Hct.fromInt(argbFromHex(hex)), true, 0, spec).primaryPalette
        : null;
    const secondaryPalette = customPalette(theme.secondaryColor) ?? scheme.secondaryPalette;
    const tertiaryPalette = customPalette(theme.tertiaryColor) ?? scheme.tertiaryPalette;

    const cell = (palette, overrideKey, tone, accent) => {
        const override = theme.colorOverrides?.[overrideKey];
        if (override && HEX.test(override)) return override;
        const base = hexFromArgb(palette.tone(tone));
        return accent
            ? adjustSaturation(base, accentSat)
            : shiftLightness(adjustSaturation(base, bgSat), bgLight);
    };

    return {
        spec,
        accentSat,
        bgSat,
        bgLight,
        halfCircle: cell(scheme.primaryPalette, "system_accent1_200", 80, true),
        firstQuarter: cell(tertiaryPalette, "system_accent3_300", 70, true),
        secondQuarter: cell(secondaryPalette, "system_accent2_400", 60, true),
        square: cell(scheme.neutralVariantPalette, "system_neutral2_700", 30, false),
        center: seed,
        container: shiftLightness(
            adjustSaturation(hexFromArgb(scheme.surfaceContainerHigh), bgSat),
            bgLight
        ),
        text: hexFromArgb(scheme.onSurface),
        subtle: hexFromArgb(scheme.onSurfaceVariant)
    };
}

// SVG twin of WallColorPreviewCanvas (64 box, pad 8, corner 16, dot r13).
function swatchSvg(c) {
    return `<svg class="tswatch" viewBox="0 0 64 64" aria-hidden="true">
        <rect width="64" height="64" rx="16" fill="${c.square}"/>
        <path d="M8 32 A24 24 0 0 1 56 32 Z" fill="${c.halfCircle}"/>
        <path d="M32 32 L32 56 A24 24 0 0 1 8 32 Z" fill="${c.firstQuarter}"/>
        <path d="M32 32 L56 32 A24 24 0 0 1 32 56 Z" fill="${c.secondQuarter}"/>
        <circle cx="32" cy="32" r="13" fill="${c.center}"/>
    </svg>`;
}

const thumbIcon = '<svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M13.12 2.06 7.58 7.6c-.37.37-.58.88-.58 1.41V19c0 1.1.9 2 2 2h9c.8 0 1.52-.48 1.84-1.21l3.26-7.61C23.94 10.2 22.49 8 20.34 8h-5.65l.95-4.58c.1-.5-.05-1.01-.41-1.37-.59-.58-1.53-.58-2.11.01ZM3 21c1.1 0 2-.9 2-2v-8c0-1.1-.9-2-2-2s-2 .9-2 2v8c0 1.1.9 2 2 2Z"/></svg>';
const downloadIcon = '<svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M16.59 9H15V4c0-.55-.45-1-1-1h-4c-.55 0-1 .45-1 1v5H7.41c-.89 0-1.34 1.08-.71 1.71l4.59 4.59c.39.39 1.02.39 1.41 0l4.59-4.59c.63-.63.19-1.71-.7-1.71ZM5 19c0 .55.45 1 1 1h12c.55 0 1-.45 1-1s-.45-1-1-1H6c-.55 0-1 .45-1 1Z"/></svg>';

function cardHtml(theme) {
    const c = cardData(theme);
    const seed = HEX.test(theme.seedColor ?? "") ? theme.seedColor : "";
    return `<a class="tcard" data-seed="${seed}" data-style="${esc(theme.style ?? "")}" data-spec="${c.spec}" data-asat="${c.accentSat}" data-bsat="${c.bgSat}" data-blight="${c.bgLight}" style="background:${c.container};color:${c.text}" href="${WORKER}/theme/${esc(theme.id)}">
        ${swatchSvg(c)}
        <span class="tinfo">
            <span class="tname">${esc(theme.name)}</span>
            <span class="tauthor" style="color:${c.subtle}">by ${esc(theme.author || "Anonymous")}</span>
            <span class="tstats" style="color:${c.subtle}">
                <span>${thumbIcon}${theme.upvotes ?? 0}</span>
                <span>${downloadIcon}${theme.downloads ?? 0}</span>
            </span>
        </span>
    </a>`;
}

const trendingScore = (t) => {
    const days = Math.max(0, Date.now() / 1000 - (t.createdAt ?? 0)) / 86400;
    return ((t.upvotes ?? 0) + (t.downloads ?? 0) * 0.5) / Math.pow(days + 2, 1.5);
};

// Scroll-in reveal for sections below the fold.
function initReveal() {
    const observer = new IntersectionObserver((entries) => {
        for (const entry of entries) {
            if (entry.isIntersecting) {
                entry.target.classList.add("in");
                observer.unobserve(entry.target);
            }
        }
    }, { threshold: 0.12 });
    document.querySelectorAll(".reveal").forEach((el) => observer.observe(el));
}

// Animated expand/collapse for FAQ details (native toggle snaps).
function initFaq() {
    const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
    document.querySelectorAll(".faq details").forEach((details) => {
        const summary = details.querySelector("summary");
        const answer = details.querySelector(".answer");
        if (!summary || !answer || reduced) return;
        let animation = null;
        summary.addEventListener("click", (e) => {
            e.preventDefault();
            animation?.cancel();
            if (details.open) {
                // Padding animated too: border-box height 0 still renders
                // the bottom padding, which snapped on [open] removal.
                animation = answer.animate(
                    [
                        { height: answer.offsetHeight + "px", paddingBottom: "20px", opacity: 1 },
                        { height: "0px", paddingBottom: "0px", opacity: 0 }
                    ],
                    { duration: 250, easing: "cubic-bezier(.2,.7,.2,1)", fill: "forwards" }
                );
                animation.onfinish = () => {
                    details.open = false;
                    animation.cancel();
                    animation = null;
                };
            } else {
                details.open = true;
                animation = answer.animate(
                    [
                        { height: "0px", paddingBottom: "0px", opacity: 0 },
                        { height: answer.scrollHeight + "px", paddingBottom: "20px", opacity: 1 }
                    ],
                    { duration: 300, easing: "cubic-bezier(.2,.7,.2,1)" }
                );
                animation.onfinish = () => { animation = null; };
            }
        });
    });
}

export async function initApp() {
    applySiteSeed(INITIAL_SEED);
    initReveal();
    initFaq();
    initDemo();
    startSeedRotation();
    try {
        const themes = await (await fetch(THEMES_INDEX)).json();
        const top = [...themes]
            .sort((a, b) => trendingScore(b) - trendingScore(a))
            .slice(0, 10);
        const set = top.map(cardHtml).join("");
        const setReversed = [...top].reverse().map(cardHtml).join("");
        // Loop = two identical halves shifted -50%; each half must cover the
        // viewport or blank space drifts in before the wrap. Rebuilt when the
        // viewport outgrows the built halves (maximize, zoom out).
        const setWidth = top.length * 296;
        let builtPerHalf = 0;
        const buildRail = () => {
            const perHalf = Math.max(1, Math.ceil(window.innerWidth / setWidth));
            if (perHalf <= builtPerHalf) return;
            builtPerHalf = perHalf;
            const half = set.repeat(perHalf);
            const halfReversed = setReversed.repeat(perHalf);
            // Second row: mobile only, reversed list, opposite drift.
            document.getElementById("rail").innerHTML =
                `<div class="marquee-track">${half}${half}</div>` +
                `<div class="marquee-track track2">${halfReversed}${halfReversed}</div>`;
        };
        buildRail();
        window.addEventListener("resize", buildRail);
        initHoverTheming(document.getElementById("rail"));
    } catch {
        document.getElementById("rail").innerHTML =
            '<div class="loading">Could not load community themes right now.</div>';
    }
}
