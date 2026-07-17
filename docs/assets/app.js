// App landing page: MCU-derived dynamic coloring (rotating seed), seed chip
// demo, light/dark, community creations marquee. Same engine as the
// themes site.
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

let darkMode = true;

// Phone mockup palette rows; one CSS var per cell.
const PH_TONES = [95, 80, 60, 40, 20, 5];

function applySiteSeed(seedHex, style, spec, sliders) {
    const { accentSat = 100, bgSat = 100, bgLight = 100 } = sliders ?? {};
    const Ctor = SCHEME_BY_STYLE[style] ?? SchemeTonalSpot;
    const scheme = new Ctor(
        Hct.fromInt(argbFromHex(seedHex)), darkMode, 0, spec ?? DEFAULT_SPEC
    );
    const role = (argb) => hexFromArgb(argb);
    const accent = (argb) => adjustSaturation(role(argb), accentSat);
    // Tone clamps keep slider-shifted surfaces off pure black/white and
    // separated; ranges flip per mode.
    const surf = (argb, minTone, maxTone) =>
        shiftLightness(adjustSaturation(role(argb), bgSat), bgLight, minTone, maxTone);
    const dm = darkMode;

    const vars = {
        "--bg": dm ? surf(scheme.surface, 4, 99) : surf(scheme.surface, 70, 100),
        "--text": role(scheme.onSurface),
        "--subtle": alpha(role(scheme.onSurfaceVariant), 0.75),
        "--body2": role(scheme.onSurfaceVariant),
        "--accent": accent(scheme.primary),
        "--on-accent": role(scheme.onPrimary),
        "--accent-glow": alpha(accent(scheme.primary), dm ? 0.32 : 0.24),
        "--tonal": role(scheme.secondaryContainer),
        "--on-tonal": role(scheme.onSecondaryContainer),
        "--card": dm ? surf(scheme.surfaceContainer, 10, 96) : surf(scheme.surfaceContainer, 66, 98),
        "--card-high": dm ? surf(scheme.surfaceContainerHigh, 14, 93) : surf(scheme.surfaceContainerHigh, 62, 97),
        "--card-highest": dm ? surf(scheme.surfaceContainerHighest, 16, 91) : surf(scheme.surfaceContainerHighest, 58, 96),
        "--outline-v": role(scheme.outlineVariant),
        "--grad-a": role(scheme.onSurface),
        "--grad-b": accent(scheme.primary),
        "--grad-c": accent(scheme.tertiary),
        "--blob-a": accent(scheme.primary),
        "--blob-b": accent(scheme.tertiary),
        "--blob-c": role(scheme.secondaryContainer),
        "--ph-sat": `${accentSat / 2}%`
    };
    const root = document.documentElement;
    for (const [k, v] of Object.entries(vars)) {
        root.style.setProperty(k, v);
    }

    // Phone mockup palette grid (accent rows honor accentSat, neutral bgSat).
    const phPalettes = {
        p: [scheme.primaryPalette, true],
        s: [scheme.secondaryPalette, true],
        t: [scheme.tertiaryPalette, true],
        n: [scheme.neutralVariantPalette, false]
    };
    for (const [key, [palette, isAccent]] of Object.entries(phPalettes)) {
        for (const t of PH_TONES) {
            const base = hexFromArgb(palette.tone(t));
            root.style.setProperty(`--ph-${key}-${t}`,
                adjustSaturation(base, isAccent ? accentSat : bgSat));
        }
    }

    document.querySelector('meta[name="theme-color"]')
        ?.setAttribute("content", vars["--bg"]);

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
let resting = { seed: INITIAL_SEED, style: undefined, spec: undefined, sliders: undefined };
let hoverHold = false;
let demoHold = false;

// Rotation cycles the playground's own seeds so the selected swatch always
// mirrors what the page is wearing.
let demoSync = null;
let railRefresh = null;

// Rotation is DRIVEN by the selection ring's 7s CSS animation: each
// completed lap advances the seed. Pausing the ring (rail hover) pauses
// rotation with it - no timer to desync; reduced-motion kills both.
function startSeedRotation() {
    const seedsEl = document.getElementById("demoSeeds");
    if (!seedsEl) return;
    seedsEl.classList.add("rotating");
    seedsEl.addEventListener("animationiteration", (e) => {
        if (e.animationName !== "ringfill") return;
        if (hoverHold || demoHold) return;
        const i = DEMO_SEEDS.indexOf(resting.seed);
        const next = DEMO_SEEDS[(i + 1) % DEMO_SEEDS.length];
        resting = { seed: next };
        applySiteSeed(next);
        demoSync?.(next);
    });
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
        seedsEl.classList.remove("rotating");
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
    const seedsEl = document.getElementById("demoSeeds");
    let activeSeed = null;
    let clearTimer = null;
    container.addEventListener("mouseover", (e) => {
        const card = e.target.closest?.(".tcard");
        const seed = card?.dataset.seed;
        if (!seed || seed === activeSeed) return;
        activeSeed = seed;
        hoverHold = true;
        seedsEl?.classList.add("paused");
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
        seedsEl?.classList.remove("paused");
        applySiteSeed(resting.seed, resting.style, resting.spec, resting.sliders);
        clearTimer = setTimeout(() => root.style.removeProperty("--recolor"), 600);
    });
}

// ---- Light/dark -----------------------------------------------------------

function initMode() {
    try {
        const stored = localStorage.cbMode;
        darkMode = stored
            ? stored === "dark"
            : !matchMedia("(prefers-color-scheme: light)").matches;
    } catch { darkMode = true; }
    document.documentElement.classList.toggle("light", !darkMode);
}

function initModeToggle() {
    const btn = document.getElementById("modeToggle");
    if (!btn) return;
    btn.addEventListener("click", () => {
        darkMode = !darkMode;
        try { localStorage.cbMode = darkMode ? "dark" : "light"; } catch { /* private mode */ }
        const root = document.documentElement;
        root.classList.toggle("light", !darkMode);
        root.style.setProperty("--recolor", ".5s");
        applySiteSeed(resting.seed, resting.style, resting.spec, resting.sliders);
        setTimeout(() => root.style.removeProperty("--recolor"), 600);
        railRefresh?.();
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
    const scheme = new Ctor(Hct.fromInt(argbFromHex(seed)), darkMode, 0, spec);
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

const SHOTS_SPRITE =
    "https://raw.githubusercontent.com/Mahmud0808/ColorBlendr/master/.github/resources/features.png";

// Slice the screenshot sprite by measuring panel bounds from the actual
// bitmap - hand-tuned percentages break whenever the sprite is re-exported
// or a stale copy sits in a cache. The same blob feeds the CSS background,
// so measurement and render can't diverge. CSS values stay as fallback.
async function initShots() {
    const shots = [...document.querySelectorAll(".shot")];
    if (!shots.length) return;
    try {
        const blob = await (await fetch(SHOTS_SPRITE, { cache: "no-cache" })).blob();
        const url = URL.createObjectURL(blob);
        const img = new Image();
        img.src = url;
        await img.decode();
        const c = document.createElement("canvas");
        c.width = img.naturalWidth;
        c.height = img.naturalHeight;
        const ctx = c.getContext("2d");
        ctx.drawImage(img, 0, 0);
        const data = ctx.getImageData(0, 0, c.width, c.height).data;
        const opaque = (x, y) => data[(y * c.width + x) * 4 + 3] > 10;
        // horizontal panel runs along the middle row
        const midY = Math.floor(c.height / 2);
        const runs = [];
        let start = null;
        for (let x = 0; x <= c.width; x++) {
            const on = x < c.width && opaque(x, midY);
            if (on && start === null) start = x;
            if (!on && start !== null) { runs.push([start, x]); start = null; }
        }
        if (runs.length !== shots.length) return;
        // shared vertical band, probed on the first panel
        const mx = Math.floor((runs[0][0] + runs[0][1]) / 2);
        let top = 0, bottom = c.height;
        while (top < c.height && !opaque(mx, top)) top++;
        while (bottom > top && !opaque(mx, bottom - 1)) bottom--;
        const h = bottom - top;
        shots.forEach((el, i) => {
            const [x0, x1] = runs[i];
            const w = x1 - x0;
            el.style.aspectRatio = `${w} / ${h}`;
            el.style.backgroundImage = `url("${url}")`;
            el.style.backgroundSize = `${(c.width / w) * 100}% auto`;
            el.style.backgroundPosition =
                `${(x0 / (c.width - w)) * 100}% ${(top / (c.height - h)) * 100}%`;
        });
    } catch { /* keep CSS fallback */ }
}

// M3 Expressive decorative shapes: polar cosine-modulated outlines
// (cookie/sunny/flower/clover), generated once per placeholder.
const DECO_SHAPES = {
    cookie: { k: 12, a: 0.09 },
    sunny: { k: 8, a: 0.22 },
    flower: { k: 6, a: 0.18 },
    clover: { k: 4, a: 0.28 }
};

function decoPath(k, a, steps = 240) {
    let d = "";
    for (let i = 0; i <= steps; i++) {
        const th = (i / steps) * Math.PI * 2;
        const r = 50 * (1 + a * Math.cos(k * th)) / (1 + a);
        const x = 50 + r * Math.cos(th);
        const y = 50 + r * Math.sin(th);
        d += `${i ? "L" : "M"}${x.toFixed(2)} ${y.toFixed(2)}`;
    }
    return d + "Z";
}

function initDeco() {
    document.querySelectorAll(".deco").forEach((el) => {
        const { k, a } = DECO_SHAPES[el.dataset.shape] ?? DECO_SHAPES.cookie;
        el.innerHTML =
            `<svg viewBox="0 0 100 100" aria-hidden="true"><path d="${decoPath(k, a)}"/></svg>`;
    });
}

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

// Accent spotlight trailing the cursor inside feature cards.
function initSpotlight() {
    if (!matchMedia("(hover: hover)").matches) return;
    document.querySelectorAll(".info").forEach((card) => {
        card.addEventListener("mousemove", (e) => {
            const r = card.getBoundingClientRect();
            card.style.setProperty("--mx", `${e.clientX - r.left}px`);
            card.style.setProperty("--my", `${e.clientY - r.top}px`);
        });
    });
}

// Screenshots tilt toward the cursor.
function initShotTilt() {
    if (!matchMedia("(hover: hover)").matches) return;
    if (matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    document.querySelectorAll(".shot").forEach((el) => {
        el.addEventListener("mousemove", (e) => {
            const r = el.getBoundingClientRect();
            const x = (e.clientX - r.left) / r.width - 0.5;
            const y = (e.clientY - r.top) / r.height - 0.5;
            el.style.transform =
                `perspective(800px) rotateY(${(x * 10).toFixed(2)}deg) rotateX(${(-y * 10).toFixed(2)}deg) translateY(-4px)`;
        });
        el.addEventListener("mouseleave", () => { el.style.transform = ""; });
    });
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
    initMode();
    applySiteSeed(INITIAL_SEED);
    initDeco();
    initShots();
    initReveal();
    initFaq();
    initDemo();
    startSeedRotation();
    initSpotlight();
    initShotTilt();
    initModeToggle();
    try {
        const themes = await (await fetch(THEMES_INDEX)).json();
        const top = [...themes]
            .sort((a, b) => trendingScore(b) - trendingScore(a))
            .slice(0, 10);
        // Loop = two identical halves shifted -50%; each half must cover the
        // viewport or blank space drifts in before the wrap. Rebuilt when the
        // viewport outgrows the built halves (maximize, zoom out) or the
        // light/dark mode flips (cards re-derive per mode).
        const setWidth = top.length * 296;
        let builtPerHalf = 0;
        const buildRail = () => {
            const perHalf = Math.max(1, Math.ceil(window.innerWidth / setWidth));
            if (perHalf <= builtPerHalf) return;
            builtPerHalf = perHalf;
            const set = top.map(cardHtml).join("");
            const setReversed = [...top].reverse().map(cardHtml).join("");
            const half = set.repeat(perHalf);
            const halfReversed = setReversed.repeat(perHalf);
            // Second row: mobile only, reversed list, opposite drift.
            document.getElementById("rail").innerHTML =
                `<div class="marquee-track">${half}${half}</div>` +
                `<div class="marquee-track track2">${halfReversed}${halfReversed}</div>`;
        };
        buildRail();
        window.addEventListener("resize", buildRail);
        railRefresh = () => { builtPerHalf = 0; buildRail(); };
        initHoverTheming(document.getElementById("rail"));
    } catch {
        document.getElementById("rail").innerHTML =
            '<div class="loading">Could not load community themes right now.</div>';
    }
}
