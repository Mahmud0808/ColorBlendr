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
	hexFromArgb,
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
	CMF: SchemeTonalSpot,
};

const esc = (s) =>
	String(s ?? "").replace(
		/[&<>"']/g,
		(c) =>
			({
				"&": "&amp;",
				"<": "&lt;",
				">": "&gt;",
				'"': "&quot;",
				"'": "&#39;",
			})[c],
	);
const alpha = (hex, a) =>
	hex +
	Math.round(a * 255)
		.toString(16)
		.padStart(2, "0");

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
	const tone = Math.max(
		minTone,
		Math.min(maxTone, hct.tone + (lightness - 100) / 10),
	);
	if (tone === hct.tone) return hex;
	return hexFromArgb(Hct.from(hct.hue, hct.chroma, tone).toInt());
}

// ---- Shade overrides --------------------------------------------------------

// colorOverrides keys are Android palette resources, system_<row>_<step>.
// Steps are fixed tonal stops; MCU roles land between them (dark surface is
// tone 6, surfaceContainer 12), so overrides act as hue/chroma anchors and
// the role keeps its own tone. Exact stops are used verbatim.
const STEP_TONE = {
	0: 100,
	10: 99,
	50: 95,
	100: 90,
	200: 80,
	300: 70,
	400: 60,
	500: 50,
	600: 40,
	700: 30,
	800: 20,
	900: 10,
	1000: 0,
};

const ROW_PALETTE = {
	system_accent1: "primaryPalette",
	system_accent2: "secondaryPalette",
	system_accent3: "tertiaryPalette",
	system_neutral1: "neutralPalette",
	system_neutral2: "neutralVariantPalette",
};

// MCU role -> the Android palette row the app draws it from.
const ROLE_ROW = {
	surface: "system_neutral1",
	surfaceContainer: "system_neutral1",
	surfaceContainerHigh: "system_neutral1",
	surfaceContainerHighest: "system_neutral1",
	onSurface: "system_neutral1",
	onSurfaceVariant: "system_neutral2",
	outlineVariant: "system_neutral2",
	primary: "system_accent1",
	onPrimary: "system_accent1",
	secondaryContainer: "system_accent2",
	onSecondaryContainer: "system_accent2",
	tertiary: "system_accent3",
};

function anchorsByRow(overrides) {
	const rows = {};
	for (const [key, hex] of Object.entries(overrides ?? {})) {
		if (!HEX.test(hex)) continue;
		const cut = key.lastIndexOf("_");
		const row = key.slice(0, cut);
		const tone = STEP_TONE[key.slice(cut + 1)];
		// system_error has no palette on the site; skip it.
		if (tone == null || !ROW_PALETTE[row]) continue;
		(rows[row] ??= []).push({ tone, hex });
	}
	for (const list of Object.values(rows)) list.sort((a, b) => a.tone - b.tone);
	return rows;
}

// Hue/chroma interpolated between the surrounding anchors, tone untouched so
// role contrast survives. Past the last anchor the nearest one is extended.
// `snap` takes an anchor verbatim when the wanted tone is that close to it,
// so a role sitting near an Android step shows the theme's literal hex.
function toneFromAnchors(anchors, tone, snap = 0) {
	let lo = null;
	let hi = null;
	for (const a of anchors) {
		if (a.tone <= tone) lo = a;
		if (a.tone >= tone && !hi) hi = a;
	}
	if (lo?.tone === tone) return lo.hex;
	if (snap) {
		const near = [lo, hi]
			.filter(Boolean)
			.sort((a, b) => Math.abs(a.tone - tone) - Math.abs(b.tone - tone))[0];
		if (near && Math.abs(near.tone - tone) <= snap) return near.hex;
	}
	const hct = (a) => Hct.fromInt(argbFromHex(a.hex));
	if (!lo || !hi) {
		const edge = hct(lo ?? hi);
		return hexFromArgb(Hct.from(edge.hue, edge.chroma, tone).toInt());
	}
	const a = hct(lo);
	const b = hct(hi);
	const f = (tone - lo.tone) / (hi.tone - lo.tone);
	const dh = ((b.hue - a.hue + 540) % 360) - 180;
	return hexFromArgb(
		Hct.from(
			(a.hue + dh * f + 360) % 360,
			a.chroma + (b.chroma - a.chroma) * f,
			tone,
		).toInt(),
	);
}

// Resolves a palette shade to the theme's real color: overrides win, then a
// custom secondary/tertiary seed, then the scheme. `fixed` marks values that
// already carry the theme's own numbers, so the saturation and lightness
// sliders must not be applied to them a second time.
function makeResolver(scheme, theme, spec, Ctor) {
	const rows = anchorsByRow(theme?.colorOverrides);
	const customPalette = (hex) =>
		HEX.test(hex ?? "")
			? new Ctor(Hct.fromInt(argbFromHex(hex)), true, 0, spec).primaryPalette
			: null;
	const swapped = {
		system_accent2: customPalette(theme?.secondaryColor),
		system_accent3: customPalette(theme?.tertiaryColor),
	};
	const shade = (row, tone, snap) => {
		if (rows[row]?.length) {
			return { hex: toneFromAnchors(rows[row], tone, snap), fixed: true };
		}
		const palette = swapped[row] ?? scheme[ROW_PALETTE[row]];
		return { hex: hexFromArgb(palette.tone(tone)), fixed: false };
	};
	// A role's tone comes from the scheme itself, so this tracks whichever
	// spec (2021/2025) built it instead of a hardcoded tone table. Accent
	// roles land within a few tones of an Android step, so they snap to the
	// theme's literal hex; neutrals must not, or the surface elevation tiers
	// (tone 4/9/12/15 in dark) would all collapse onto one override.
	const role = (name) => {
		const base = hexFromArgb(scheme[name]);
		const row = ROLE_ROW[name];
		if (!row) return { hex: base, fixed: false };
		const tone = Math.round(Hct.fromInt(argbFromHex(base)).tone);
		return shade(row, tone, row.startsWith("system_accent") ? 5 : 0);
	};
	return { shade, role };
}

// A flat override ramp can leave two elevation tiers on the same tone (Nova
// Ember pins neutral1 90 and 95 to near-identical grays), which reads as one
// sheet. Nudge each tier away from the one below it. dir: +1 dark, -1 light.
function separate(hex, belowHex, dir, min = 3) {
	const hct = Hct.fromInt(argbFromHex(hex));
	const below = Hct.fromInt(argbFromHex(belowHex)).tone;
	const need = below + dir * min;
	if (dir > 0 ? hct.tone >= need : hct.tone <= need) return hex;
	const tone = Math.max(0, Math.min(100, need));
	return hexFromArgb(Hct.from(hct.hue, hct.chroma, tone).toInt());
}

// Slider values for the active mode; the app ignores them for MONOCHROMATIC.
function themeSliders(theme) {
	if (!theme || theme.style === "MONOCHROMATIC") {
		return { accentSat: 100, bgSat: 100, bgLight: 100 };
	}
	const light = !darkMode && theme.modeSpecificThemes;
	return {
		accentSat:
			(light ? theme.accentSaturationLight : theme.accentSaturation) ?? 100,
		bgSat:
			(light
				? theme.backgroundSaturationLight
				: theme.backgroundSaturation) ?? 100,
		bgLight:
			(light
				? theme.backgroundLightnessLight
				: theme.backgroundLightness) ?? 100,
	};
}

// ---- Site-wide dynamic coloring -------------------------------------------

let darkMode = true;

// Phone mockup palette rows; one CSS var per cell.
const PH_TONES = [95, 80, 60, 40, 20, 5];

// `opts.theme` is the full catalog entry when a card drives the tint; the
// playground passes style/spec on their own and the page just renders the
// seed with library defaults.
function applySiteSeed(seedHex, opts) {
	const { style, spec, sliders, theme } = opts ?? {};
	const Ctor = SCHEME_BY_STYLE[style ?? theme?.style] ?? SchemeTonalSpot;
	const resolvedSpec =
		spec ?? SPEC_BY_VERSION[theme?.colorSpecVersion] ?? DEFAULT_SPEC;
	const scheme = new Ctor(
		Hct.fromInt(argbFromHex(seedHex)),
		darkMode,
		0,
		resolvedSpec,
	);
	const { accentSat, bgSat, bgLight } = sliders ?? themeSliders(theme);
	const { shade, role } = makeResolver(scheme, theme, resolvedSpec, Ctor);

	const plain = (name) => role(name).hex;
	const accent = (name) => {
		const r = role(name);
		return r.fixed ? r.hex : adjustSaturation(r.hex, accentSat);
	};
	// Tone clamps keep slider-shifted surfaces off pure black/white and
	// separated; ranges flip per mode. Overridden shades skip the sliders:
	// the theme already baked them in.
	const surf = (name, minTone, maxTone) => {
		const r = role(name);
		return r.fixed
			? r.hex
			: shiftLightness(
					adjustSaturation(r.hex, bgSat),
					bgLight,
					minTone,
					maxTone,
				);
	};
	const dm = darkMode;
	const dir = dm ? 1 : -1;

	const bg = dm ? surf("surface", 4, 99) : surf("surface", 70, 100);
	const card = separate(
		dm
			? surf("surfaceContainer", 10, 96)
			: surf("surfaceContainer", 66, 98),
		bg,
		dir,
	);
	const cardHigh = separate(
		dm
			? surf("surfaceContainerHigh", 14, 93)
			: surf("surfaceContainerHigh", 62, 97),
		card,
		dir,
	);
	const cardHighest = separate(
		dm
			? surf("surfaceContainerHighest", 16, 91)
			: surf("surfaceContainerHighest", 58, 96),
		cardHigh,
		dir,
	);

	const vars = {
		"--bg": bg,
		"--text": plain("onSurface"),
		// 0.9, not 0.75: --subtle carries real text (stat labels, table
		// headers) that has to clear 4.5:1 in light mode.
		"--subtle": alpha(plain("onSurfaceVariant"), 0.9),
		"--body2": plain("onSurfaceVariant"),
		"--accent": accent("primary"),
		"--on-accent": plain("onPrimary"),
		"--accent-glow": alpha(accent("primary"), dm ? 0.32 : 0.24),
		"--tonal": plain("secondaryContainer"),
		"--on-tonal": plain("onSecondaryContainer"),
		"--card": card,
		"--card-high": cardHigh,
		"--card-highest": cardHighest,
		"--outline-v": plain("outlineVariant"),
		"--grad-c": accent("tertiary"),
		"--blob-a": accent("primary"),
		"--blob-b": accent("tertiary"),
		"--ph-sat": `${accentSat / 2}%`,
	};
	const root = document.documentElement;
	for (const [k, v] of Object.entries(vars)) {
		root.style.setProperty(k, v);
	}

	// Phone mockup palette grid (accent rows honor accentSat, neutral bgSat).
	const phRows = {
		p: ["system_accent1", true],
		s: ["system_accent2", true],
		t: ["system_accent3", true],
		n: ["system_neutral2", false],
	};
	for (const [key, [row, isAccent]] of Object.entries(phRows)) {
		for (const t of PH_TONES) {
			const s = shade(row, t);
			root.style.setProperty(
				`--ph-${key}-${t}`,
				s.fixed
					? s.hex
					: adjustSaturation(s.hex, isAccent ? accentSat : bgSat),
			);
		}
	}

	document
		.querySelector('meta[name="theme-color"]')
		?.setAttribute("content", vars["--bg"]);

	// Hero logo disc follows the seed (launcher gradient formula).
	const stops = document.querySelectorAll("#lg stop");
	if (stops.length === 2) {
		[70, 40].forEach((tone, i) => {
			const s = shade("system_accent1", tone);
			stops[i].style.setProperty(
				"stop-color",
				s.fixed ? s.hex : adjustSaturation(s.hex, accentSat),
			);
		});
	}
}

// Boot color; matches the :root CSS fallbacks so first paint = first seed.
const INITIAL_SEED = "#51BDFF";

// Theme the site rests on when nothing is hovered; rotation or the
// playground moves it.
let resting = {
	seed: INITIAL_SEED,
	style: undefined,
	spec: undefined,
	sliders: undefined,
	theme: undefined,
};
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
	"#51BDFF",
	"#F44336",
	"#FFB300",
	"#4CAF50",
	"#26A69A",
	"#7C4DFF",
	"#EC407A",
];
const DEMO_STYLES = [
	["TONAL_SPOT", "Tonal Spot"],
	["VIBRANT", "Vibrant"],
	["EXPRESSIVE", "Expressive"],
	["RAINBOW", "Rainbow"],
	["FRUIT_SALAD", "Fruit Salad"],
	["SPRITZ", "Spritz"],
	["MONOCHROMATIC", "Monochrome"],
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
		const scheme = new SCHEME_BY_STYLE[style](
			Hct.fromInt(argbFromHex(s)),
			true,
			0,
			spec,
		);
		return swatchSvg({
			square: hexFromArgb(scheme.neutralVariantPalette.tone(30)),
			halfCircle: hexFromArgb(scheme.primaryPalette.tone(80)),
			firstQuarter: hexFromArgb(scheme.tertiaryPalette.tone(70)),
			secondQuarter: hexFromArgb(scheme.secondaryPalette.tone(60)),
			center: s,
		});
	};

	const renderSeeds = () => {
		seedsEl.innerHTML = DEMO_SEEDS.map(
			(s) =>
				`<button class="demo-swatch" data-seed="${s}" aria-label="Seed ${s}">${swatch(s)}</button>`,
		).join("");
		updateSelection();
	};

	// Class toggle, not a rebuild: the ring cross-fades between swatches.
	const updateSelection = () => {
		seedsEl
			.querySelectorAll(".demo-swatch")
			.forEach((b) => b.classList.toggle("on", b.dataset.seed === seed));
	};

	const apply = (rebuildSwatches) => {
		demoHold = true;
		seedsEl.classList.remove("rotating");
		resting = { seed, style, spec };
		if (clearTimer) clearTimeout(clearTimer);
		root.style.setProperty("--recolor", ".5s");
		applySiteSeed(seed, { style, spec });
		clearTimer = setTimeout(
			() => root.style.removeProperty("--recolor"),
			600,
		);
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
				menu.innerHTML = items
					.map(([v, l]) => {
						const on = v === getValue();
						return `<button class="menuitem${on ? " selected" : ""}" role="option" aria-selected="${on}" data-value="${v}">${l}</button>`;
					})
					.join("");
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
			btn.focus();
			onPick(item.dataset.value);
		});
		document.addEventListener("click", (e) => {
			if (!wrap.contains(e.target)) setOpen(false);
		});
		// Escape returns focus to the trigger; closing under the caret would
		// otherwise drop focus to the body.
		document.addEventListener("keydown", (e) => {
			if (e.key !== "Escape" || menu.hidden) return;
			setOpen(false);
			if (wrap.contains(document.activeElement)) btn.focus();
		});
	};
	dropdown(
		styleWrap,
		DEMO_STYLES,
		() => style,
		(v) => {
			style = v;
			apply(true);
		},
	);
	dropdown(
		specWrap,
		DEMO_SPECS.map((v) => [v, `Spec ${v}`]),
		() => spec,
		(v) => {
			spec = v;
			apply(true);
		},
	);

	// Ambient rotation drives the selection ring, not the other way around.
	demoSync = (s) => {
		seed = s;
		updateSelection();
	};

	renderSeeds();
}

// Hovered card retints the whole site with its seed; leave reverts.
// --recolor shortens every themed transition while the hover drives it.
function initHoverTheming(container, byId) {
	if (!container || !matchMedia("(hover: hover)").matches) return;
	const root = document.documentElement;
	const seedsEl = document.getElementById("demoSeeds");
	let activeId = null;
	let clearTimer = null;
	container.addEventListener("mouseover", (e) => {
		const card = e.target.closest?.(".tcard");
		const seed = card?.dataset.seed;
		const id = card?.dataset.id;
		if (!seed || id === activeId) return;
		activeId = id;
		hoverHold = true;
		seedsEl?.classList.add("paused");
		if (clearTimer) clearTimeout(clearTimer);
		root.style.setProperty("--recolor", ".5s");
		applySiteSeed(seed, { theme: byId.get(id) });
	});
	container.addEventListener("mouseout", (e) => {
		const card = e.target.closest?.(".tcard");
		if (!card || card.contains(e.relatedTarget)) return;
		if (e.relatedTarget?.closest?.(".tcard")) return;
		activeId = null;
		hoverHold = false;
		seedsEl?.classList.remove("paused");
		applySiteSeed(resting.seed, resting);
		clearTimer = setTimeout(
			() => root.style.removeProperty("--recolor"),
			600,
		);
	});
}

// ---- Light/dark -----------------------------------------------------------

function initMode() {
	try {
		const stored = localStorage.cbMode;
		darkMode = stored
			? stored === "dark"
			: !matchMedia("(prefers-color-scheme: light)").matches;
	} catch {
		darkMode = true;
	}
	document.documentElement.classList.toggle("light", !darkMode);
}

// Phones only: the toggle has no gutter to sit in, so hide it on the way
// down and bring it back on the way up. Never while it holds focus.
function initToggleTuck(btn) {
	if (!matchMedia("(max-width: 620px)").matches) return;
	let last = window.scrollY;
	window.addEventListener(
		"scroll",
		() => {
			const y = window.scrollY;
			if (Math.abs(y - last) < 8) return;
			const tuck = y > last && y > 220 && document.activeElement !== btn;
			btn.classList.toggle("tucked", tuck);
			last = y;
		},
		{ passive: true },
	);
}

function initModeToggle() {
	const btn = document.getElementById("modeToggle");
	if (!btn) return;
	initToggleTuck(btn);
	btn.addEventListener("click", () => {
		darkMode = !darkMode;
		try {
			localStorage.cbMode = darkMode ? "dark" : "light";
		} catch {
			/* private mode */
		}
		const root = document.documentElement;
		root.classList.toggle("light", !darkMode);
		root.style.setProperty("--recolor", ".5s");
		applySiteSeed(resting.seed, resting);
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
	const { accentSat, bgSat, bgLight } = themeSliders(theme);
	const { shade, role } = makeResolver(scheme, theme, spec, Ctor);

	const accentCell = (row, tone) => {
		const s = shade(row, tone);
		return s.fixed ? s.hex : adjustSaturation(s.hex, accentSat);
	};
	const surfCell = (s) =>
		s.fixed ? s.hex : shiftLightness(adjustSaturation(s.hex, bgSat), bgLight);

	return {
		halfCircle: accentCell("system_accent1", 80),
		firstQuarter: accentCell("system_accent3", 70),
		secondQuarter: accentCell("system_accent2", 60),
		square: surfCell(shade("system_neutral2", 30)),
		center: seed,
		container: surfCell(role("surfaceContainerHigh")),
		text: role("onSurface").hex,
		subtle: role("onSurfaceVariant").hex,
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

const thumbIcon =
	'<svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M13.12 2.06 7.58 7.6c-.37.37-.58.88-.58 1.41V19c0 1.1.9 2 2 2h9c.8 0 1.52-.48 1.84-1.21l3.26-7.61C23.94 10.2 22.49 8 20.34 8h-5.65l.95-4.58c.1-.5-.05-1.01-.41-1.37-.59-.58-1.53-.58-2.11.01ZM3 21c1.1 0 2-.9 2-2v-8c0-1.1-.9-2-2-2s-2 .9-2 2v8c0 1.1.9 2 2 2Z"/></svg>';
const downloadIcon =
	'<svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><path d="M16.59 9H15V4c0-.55-.45-1-1-1h-4c-.55 0-1 .45-1 1v5H7.41c-.89 0-1.34 1.08-.71 1.71l4.59 4.59c.39.39 1.02.39 1.41 0l4.59-4.59c.63-.63.19-1.71-.7-1.71ZM5 19c0 .55.45 1 1 1h12c.55 0 1-.45 1-1s-.45-1-1-1H6c-.55 0-1 .45-1 1Z"/></svg>';

function cardHtml(theme) {
	const c = cardData(theme);
	const seed = HEX.test(theme.seedColor ?? "") ? theme.seedColor : "";
	// Hover theming looks the entry up by id; colorOverrides are far too big
	// to bake into a data attribute on every (repeated) card.
	return `<a class="tcard" data-seed="${seed}" data-id="${esc(theme.id)}" style="background:${c.container};color:${c.text}" href="${WORKER}/theme/${esc(theme.id)}">
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
	return (
		((t.upvotes ?? 0) + (t.downloads ?? 0) * 0.5) / Math.pow(days + 2, 1.5)
	);
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
		const blob = await (
			await fetch(SHOTS_SPRITE, { cache: "no-cache" })
		).blob();
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
			if (!on && start !== null) {
				runs.push([start, x]);
				start = null;
			}
		}
		if (runs.length !== shots.length) return;
		// shared vertical band, probed on the first panel
		const mx = Math.floor((runs[0][0] + runs[0][1]) / 2);
		let top = 0,
			bottom = c.height;
		while (top < c.height && !opaque(mx, top)) top++;
		while (bottom > top && !opaque(mx, bottom - 1)) bottom--;
		const h = bottom - top;
		shots.forEach((el, i) => {
			const [x0, x1] = runs[i];
			const w = x1 - x0;
			el.style.aspectRatio = `${w} / ${h}`;
			el.style.backgroundImage = `url("${url}")`;
			el.style.backgroundSize = `${(c.width / w) * 100}% auto`;
			el.style.backgroundPosition = `${(x0 / (c.width - w)) * 100}% ${(top / (c.height - h)) * 100}%`;
		});
	} catch {
		/* keep CSS fallback */
	}
}

// M3 Expressive decorative shapes: polar cosine-modulated outlines
// (cookie/sunny/flower/clover), generated once per placeholder.
const DECO_SHAPES = {
	cookie: { k: 12, a: 0.09 },
	sunny: { k: 8, a: 0.22 },
	flower: { k: 6, a: 0.18 },
	clover: { k: 4, a: 0.28 },
};

function decoPath(k, a, steps = 240) {
	let d = "";
	for (let i = 0; i <= steps; i++) {
		const th = (i / steps) * Math.PI * 2;
		const r = (50 * (1 + a * Math.cos(k * th))) / (1 + a);
		const x = 50 + r * Math.cos(th);
		const y = 50 + r * Math.sin(th);
		d += `${i ? "L" : "M"}${x.toFixed(2)} ${y.toFixed(2)}`;
	}
	return d + "Z";
}

function initDeco() {
	document.querySelectorAll(".deco").forEach((el) => {
		const { k, a } = DECO_SHAPES[el.dataset.shape] ?? DECO_SHAPES.cookie;
		el.innerHTML = `<svg viewBox="0 0 100 100" aria-hidden="true"><path d="${decoPath(k, a)}"/></svg>`;
	});
}

// Scroll-in reveal for sections below the fold.
function initReveal() {
	const observer = new IntersectionObserver(
		(entries) => {
			for (const entry of entries) {
				if (entry.isIntersecting) {
					entry.target.classList.add("in");
					observer.unobserve(entry.target);
				}
			}
		},
		{ threshold: 0.12 },
	);
	document.querySelectorAll(".reveal").forEach((el) => observer.observe(el));
}

// Live stars + release download totals, counted up when they arrive;
// markup values stay for no-JS / fetch failure.
function initStats() {
	const dlEl = document.getElementById("statDownloads");
	const starsEl = document.getElementById("statStars");
	const ossEl = document.getElementById("statOss");
	if (!dlEl || !starsEl || !ossEl) return;
	const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
	// Markup holds final-looking values for no-JS; zero for the build-up.
	// Stash them first: the API is rate limited to 60/hour per IP, and a
	// 403 must fall back to the markup rather than leave a live "0".
	const fallback = new Map([
		[dlEl, dlEl.textContent],
		[starsEl, starsEl.textContent],
	]);
	const restore = (el) => {
		el.textContent = fallback.get(el);
	};
	if (!reduced) {
		dlEl.textContent = "0";
		starsEl.textContent = "0";
		ossEl.textContent = "0%";
	}
	const compact = (n) => {
		if (n >= 10000) return `${Math.floor(n / 1000)}K+`;
		if (n >= 1000)
			return `${(Math.floor(n / 100) / 10).toFixed(1).replace(/\.0$/, "")}K+`;
		return String(n);
	};
	const countTo = (el, target, fmt) => {
		if (reduced) {
			el.textContent = fmt(target);
			return;
		}
		const t0 = performance.now();
		const tick = (now) => {
			const p = Math.min(1, (now - t0) / 1400);
			const eased = 1 - (1 - p) ** 3;
			el.textContent = fmt(Math.round(target * eased));
			if (p < 1) requestAnimationFrame(tick);
		};
		requestAnimationFrame(tick);
	};
	// Hold count-up until the hero entrance settles.
	const settled = new Promise((resolve) => setTimeout(resolve, 500));
	settled.then(() => countTo(ossEl, 100, (n) => `${n}%`));
	const REPO = "https://api.github.com/repos/Mahmud0808/ColorBlendr";
	fetch(REPO)
		.then((r) => (r.ok ? r.json() : null))
		.then(async (repo) => {
			if (!repo?.stargazers_count) return restore(starsEl);
			await settled;
			countTo(starsEl, repo.stargazers_count, compact);
		})
		.catch(() => restore(starsEl));
	fetch(`${REPO}/releases?per_page=100`)
		.then((r) => (r.ok ? r.json() : null))
		.then(async (releases) => {
			if (!Array.isArray(releases)) return restore(dlEl);
			const total = releases
				.flatMap((rel) => rel.assets ?? [])
				.reduce((sum, a) => sum + (a.download_count ?? 0), 0);
			if (!total) return restore(dlEl);
			await settled;
			countTo(dlEl, total, compact);
		})
		.catch(() => restore(dlEl));
}

// Animated expand/collapse for FAQ details (native toggle snaps).
function initFaq() {
	const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
	document.querySelectorAll(".faq details").forEach((details) => {
		const summary = details.querySelector("summary");
		const answer = details.querySelector(".answer");
		// Reveal after a mode/seed change while closed: content was
		// render-skipped, so pending recolors would transition late. Snap them.
		details.addEventListener("toggle", () => {
			if (!details.open) return;
			details.classList.add("no-recolor");
			requestAnimationFrame(() =>
				requestAnimationFrame(() =>
					details.classList.remove("no-recolor"),
				),
			);
		});
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
						{
							height: answer.offsetHeight + "px",
							paddingBottom: "20px",
							opacity: 1,
						},
						{ height: "0px", paddingBottom: "0px", opacity: 0 },
					],
					{
						duration: 250,
						easing: "cubic-bezier(.2,.7,.2,1)",
						fill: "forwards",
					},
				);
				animation.onfinish = () => {
					details.open = false;
					animation.cancel();
					animation = null;
				};
			} else {
				details.classList.add("no-recolor");
				details.open = true;
				animation = answer.animate(
					[
						{ height: "0px", paddingBottom: "0px", opacity: 0 },
						{
							height: answer.scrollHeight + "px",
							paddingBottom: "20px",
							opacity: 1,
						},
					],
					{ duration: 300, easing: "cubic-bezier(.2,.7,.2,1)" },
				);
				animation.onfinish = () => {
					animation = null;
				};
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
	initModeToggle();
	initStats();
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
			const perHalf = Math.max(
				1,
				Math.ceil(window.innerWidth / setWidth),
			);
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
		railRefresh = () => {
			builtPerHalf = 0;
			buildRail();
		};
		initHoverTheming(
			document.getElementById("rail"),
			new Map(themes.map((t) => [t.id, t])),
		);
	} catch {
		document.getElementById("rail").innerHTML =
			'<div class="loading">Could not load community themes right now.</div>';
	}
}
