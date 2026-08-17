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

const relLum = (hex) => {
	const c = [1, 3, 5].map((i) => {
		const v = parseInt(hex.slice(i, i + 2), 16) / 255;
		return v <= 0.03928 ? v / 12.92 : ((v + 0.055) / 1.055) ** 2.4;
	});
	return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
};

const contrast = (a, b) => {
	const [x, y] = [relLum(a), relLum(b)].sort((p, q) => q - p);
	return (x + 0.05) / (y + 0.05);
};

function ensureContrast(fg, bg, min = 4.5) {
	if (contrast(fg, bg) >= min) return fg;
	const hct = Hct.fromInt(argbFromHex(fg));
	const dir = relLum(bg) > 0.18 ? -1 : 1;
	let out = fg;
	for (let t = hct.tone + dir * 3; t >= 0 && t <= 100; t += dir * 3) {
		out = hexFromArgb(Hct.from(hct.hue, hct.chroma, t).toInt());
		if (contrast(out, bg) >= min) return out;
	}
	return out;
}

let siteDark = true;

// Port of the app's palette pipeline (ColorSchemeUtil.generateColorPalette,
// ColorModifiers.modifyColors, CommunityThemePalette.derive, DynamicColors).
// Order matters: palette, then modifiers, then the theme's own hexes, then
// pitch black. Roles read fixed indices out of the finished palette.
const TONES = [100, 99, 95, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0];
const TINTS = TONES.map((t) => t / 100);
const SHADES = [
	"0",
	"10",
	"50",
	"100",
	"200",
	"300",
	"400",
	"500",
	"600",
	"700",
	"800",
	"900",
	"1000",
];
const ROW_NAMES = [
	"system_accent1",
	"system_accent2",
	"system_accent3",
	"system_neutral1",
	"system_neutral2",
	"system_error",
];

// Tones the phone mockup's palette grid samples.
const PH_TONES = [95, 80, 60, 40, 20, 5];

// [row, darkIndex, lightIndex, darkLightnessAdjustment, lightLightnessAdjustment]
const ROLE_MAP = {
	primary: [0, 4, 8],
	primaryContainer: [0, 9, 3],
	onPrimaryContainer: [0, 3, 11],
	onPrimary: [0, 10, 0],
	secondaryContainer: [1, 9, 3],
	onSecondaryContainer: [1, 3, 11],
	tertiary: [2, 4, 8],
	surface: [3, 11, 1, -25, -1],
	onSurface: [3, 2, 10],
	surfaceContainer: [3, 10, 2, -42, -2],
	surfaceContainerHigh: [3, 10, 1, null, -4],
	surfaceContainerHighest: [3, 10, 1, 3, -5],
	surfaceBright: [3, 10, 1, 13, -2],
	onSurfaceVariant: [4, 2, 10],
	outlineVariant: [4, 9, 4],
};

function toneOf(hex) {
	return Hct.fromInt(argbFromHex(hex)).tone;
}

function atTone(hex, tone) {
	const h = Hct.fromInt(argbFromHex(hex));
	return hexFromArgb(
		Hct.from(h.hue, h.chroma, Math.max(0, Math.min(100, tone))).toInt(),
	);
}

function shiftLightness(hex, lightness, idx) {
	let f = (lightness - 100) / 1000;
	if (idx === 0 || idx === 12) f = 0;
	else if (idx === 1) f /= 10;
	else if (idx === 2) f /= 2;
	return atTone(hex, 100 * (TINTS[idx] + f));
}

function adjustLightness(hex, percent) {
	const tone = toneOf(hex);
	const pct = Math.max(-100, Math.min(100, percent));
	return atTone(hex, tone + tone * (pct / 100));
}

function buildPalette(seedHex, style, spec, dark, sliders, theme) {
	const Ctor = SCHEME_BY_STYLE[style] ?? SchemeTonalSpot;
	const toneList = (palette) => TONES.map((t) => hexFromArgb(palette.tone(t)));
	const scheme = new Ctor(Hct.fromInt(argbFromHex(seedHex)), dark, 0, spec);
	const rows = [
		scheme.primaryPalette,
		scheme.secondaryPalette,
		scheme.tertiaryPalette,
		scheme.neutralPalette,
		scheme.neutralVariantPalette,
		scheme.errorPalette,
	].map(toneList);

	const ownPalette = (hex) =>
		toneList(
			new Ctor(Hct.fromInt(argbFromHex(hex)), dark, 0, spec).primaryPalette,
		);
	if (HEX.test(theme?.secondaryColor ?? "")) {
		rows[1] = ownPalette(theme.secondaryColor);
	}
	if (HEX.test(theme?.tertiaryColor ?? "")) {
		rows[2] = ownPalette(theme.tertiaryColor);
	}

	const { accentSat, bgSat, bgLight } = sliders;
	const mono = style === "MONOCHROMATIC";
	const rainbow = style === "RAINBOW";
	const pitch = Boolean(theme?.pitchBlack);

	rows.forEach((row, i) => {
		const accent = i <= 2 || i === 5;
		const neutral = i === 3 || i === 4;
		// The app modifies shades 1..12; shade 0 is left alone.
		for (let j = 1; j < row.length; j++) {
			if (accent && accentSat !== 100 && !mono) {
				row[j] = adjustSaturation(row[j], accentSat);
			} else if (neutral) {
				if (bgLight !== 100 && !mono) {
					row[j] = shiftLightness(row[j], bgLight, j);
				}
				if (bgSat !== 100 && !mono && !rainbow) {
					row[j] = adjustSaturation(row[j], bgSat);
				}
			}
			if (mono) row[j] = shiftLightness(row[j], bgLight, j);
		}
		if (neutral && pitch) row[11] = "#000000";
	});

	for (const [name, hex] of Object.entries(theme?.colorOverrides ?? {})) {
		if (!HEX.test(hex)) continue;
		const cut = name.lastIndexOf("_");
		const row = ROW_NAMES.indexOf(name.slice(0, cut));
		const idx = SHADES.indexOf(name.slice(cut + 1));
		if (row >= 0 && idx >= 0) rows[row][idx] = hex;
	}

	if (pitch) {
		rows[3][11] = "#000000";
		rows[4][11] = "#000000";
	}

	return rows;
}

function roleReader(rows, dark) {
	return (name) => {
		const [row, darkIdx, lightIdx, darkAdj, lightAdj] = ROLE_MAP[name];
		const hex = rows[row][dark ? darkIdx : lightIdx];
		const adj = dark ? darkAdj : lightAdj;
		return adj == null ? hex : adjustLightness(hex, adj);
	};
}

// Slider values for the active mode; the app ignores them for MONOCHROMATIC.
function themeSliders(theme) {
	if (!theme || theme.style === "MONOCHROMATIC") {
		return { accentSat: 100, bgSat: 100, bgLight: 100 };
	}
	const light = !siteDark && theme.modeSpecificThemes;
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

function computeVars(seedHex, opts) {
	const { style, spec, sliders, theme, dark } = opts ?? {};
	const resolvedStyle = style ?? theme?.style ?? "TONAL_SPOT";
	const resolvedSpec =
		spec ?? SPEC_BY_VERSION[theme?.colorSpecVersion] ?? DEFAULT_SPEC;
	const darkMode = dark ?? siteDark;
	const rows = buildPalette(
		seedHex,
		resolvedStyle,
		resolvedSpec,
		darkMode,
		sliders ?? themeSliders(theme),
		theme,
	);
	const role = roleReader(rows, darkMode);

	const tint = (hex) => {
		if (!theme?.tintText) return hex;
		const h = Hct.fromInt(argbFromHex(hex));
		const a = Hct.fromInt(argbFromHex(role("primary")));
		return hexFromArgb(Hct.from(a.hue, Math.max(h.chroma, 12), h.tone).toInt());
	};

	const accentBg = role("primary");
	const tonalBg = role("primaryContainer");

	// Pitch black (and themes that override the neutral shades to #000) can
	// flatten surface and every container role onto the same black, which
	// erases the phone frame and the cards drawn on top of the page. Lift the
	// containers to a tone floor above the surface so the elevation stays
	// readable; themes with real separation already clear it untouched.
	const surfaceBg = role("surface");
	const surfaceTone = toneOf(surfaceBg);
	const elevated = (name, floor) => {
		const hex = role(name);
		if (!darkMode) return hex;
		const min = surfaceTone + floor;
		return toneOf(hex) >= min ? hex : atTone(hex, min);
	};

	const vars = {
		"--bg": surfaceBg,
		"--text": tint(role("onSurface")),
		// 0.9, not 0.75: --subtle carries real text (stat labels, table
		// headers) that has to clear 4.5:1 in light mode.
		"--subtle": alpha(tint(role("onSurfaceVariant")), 0.9),
		"--body2": tint(role("onSurfaceVariant")),
		"--accent": accentBg,
		// A theme is free to put red on red; a button still has to be read.
		"--on-accent": ensureContrast(role("onPrimary"), accentBg),
		"--tonal": tonalBg,
		"--on-tonal": ensureContrast(role("onPrimaryContainer"), tonalBg),
		"--card": elevated("surfaceContainer", 4),
		"--card-high": elevated("surfaceContainerHigh", 7),
		"--card-highest": elevated("surfaceBright", 11),
		"--outline-v": role("outlineVariant"),
		"--grad-c": role("tertiary"),
		"--ph-sat": `${(sliders ?? themeSliders(theme)).accentSat / 2}%`,
	};

	// Phone mockup palette grid reads the finished rows directly.
	const phRows = { p: 0, s: 1, t: 2, n: 4 };
	for (const [key, row] of Object.entries(phRows)) {
		PH_TONES.forEach((tone) => {
			vars[`--ph-${key}-${tone}`] = rows[row][TONES.indexOf(tone)];
		});
	}

	const logo = [4, 8].map((idx) => rows[0][idx]);

	return { vars, logo };
}

function paint(el, seedHex, opts) {
	const { vars } = computeVars(seedHex, opts);
	for (const [k, v] of Object.entries(vars)) el.style.setProperty(k, v);
	return vars;
}

function applySiteSeed(seedHex, opts) {
	const { vars, logo } = computeVars(seedHex, opts);
	const root = document.documentElement;
	for (const [k, v] of Object.entries(vars)) root.style.setProperty(k, v);
	root.style.setProperty("--frame", vars["--card-high"]);
	root.style.setProperty("--frame-text", vars["--body2"]);
	root.style.setProperty("--frame-accent", vars["--accent"]);

	document
		.querySelector('meta[name="theme-color"]')
		?.setAttribute("content", vars["--bg"]);

	// Hero logo disc follows the seed (launcher gradient formula).
	const stops = document.querySelectorAll("#lg stop");
	if (stops.length === 2) {
		stops.forEach((s, i) => s.style.setProperty("stop-color", logo[i]));
	}
	repaintProofs();
}

// ---- Proof pairs ------------------------------------------------------------

const BASELINE_SEED = "#1B6EF3";
const OVERRIDE_SLOT = "#FF7043";

const paletteRow = (key) =>
	`<span class="mrow">${PH_TONES.map(
		(t) => `<i style="background:var(--ph-${key}-${t})"></i>`,
	).join("")}</span>`;

const MINI = {
	palette: () =>
		`<span class="mcard">${["p", "s", "t", "n"].map(paletteRow).join("")}</span>`,
	shades: () =>
		`<span class="mcard">${["p", "s", "t", "n"]
			.map((key) =>
				key === "p"
					? `<span class="mrow marked">${PH_TONES.map(
							(t) => `<i style="background:var(--ph-p-${t})"></i>`,
						).join("")}</span>`
					: paletteRow(key),
			)
			.join("")}</span>`,
	slider: () =>
		`<span class="mcard">${paletteRow("p")}${paletteRow("s")}` +
		`<span class="mtrack"></span><span class="mtrack low"></span></span>`,
	backup: () =>
		`<span class="mcard mbackup">${paletteRow("p")}${paletteRow("s")}${paletteRow("t")}` +
		`<span class="marrow"><svg viewBox="0 0 24 24" aria-hidden="true">` +
		`<path d="M12 3a1 1 0 0 1 1 1v9.6l3.3-3.3a1 1 0 1 1 1.4 1.4l-5 5a1 1 0 0 1-1.4 0l-5-5a1 1 0 1 1 1.4-1.4l3.3 3.3V4a1 1 0 0 1 1-1z"/></svg></span>` +
		`<span class="mfile"><svg viewBox="0 0 24 24" aria-hidden="true">` +
		`<path d="M6 2h7l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm7 1.5V8h4.5L13 3.5z"/></svg>` +
		`<span class="mtext">Backup file</span></span></span>`,
	surface: () =>
		`<span class="mcard msurface"><span class="mhead">Settings</span>` +
		`<span class="mtile"><span class="mtext">Pitch black</span><span class="mswitch"></span></span>` +
		`<span class="mtile"><span class="mtext">Tint text</span><span class="mswitch"></span></span>` +
		`<span class="mtile"><span class="mtext">Accurate shades</span><span class="mswitch off"></span></span></span>`,
};

const PROOFS = {
	seed: {
		widget: "palette",
		before: () => [BASELINE_SEED, { sliders: DEFAULT_SLIDERS }],
		after: () => [resting.seed, resting],
	},
	sliders: {
		widget: "slider",
		before: () => [resting.seed, { ...resting, sliders: DEFAULT_SLIDERS }],
		after: () => [
			resting.seed,
			{ ...resting, sliders: { accentSat: 200, bgSat: 175, bgLight: 88 } },
		],
	},
	styles: {
		widget: "palette",
		before: () => [resting.seed, { ...resting, style: "MONOCHROMATIC" }],
		after: () => [
			resting.seed,
			{ ...resting, style: resting.style === "FRUIT_SALAD" ? "RAINBOW" : "FRUIT_SALAD" },
		],
	},
	shades: {
		widget: "shades",
		before: () => [resting.seed, resting],
		after: () => [resting.seed, { ...resting, slot: OVERRIDE_SLOT }],
	},
	backup: {
		widget: "backup",
		after: () => [resting.seed, resting],
	},
	dark: {
		widget: "surface",
		before: () => [resting.seed, { ...resting, dark: true }],
		after: () => [
			resting.seed,
			{
				...resting,
				dark: true,
				theme: { ...resting.theme, pitchBlack: true },
			},
		],
	},
};

const DEFAULT_SLIDERS = { accentSat: 100, bgSat: 100, bgLight: 100 };

function paintPane(pane, seed, opts) {
	const vars = paint(pane, seed, opts);
	if (opts?.slot) pane.style.setProperty("--ph-p-60", opts.slot);
}

function repaintProofs() {
	document.querySelectorAll(".pair[data-proof]").forEach((pair) => {
		const proof = PROOFS[pair.dataset.proof];
		if (!proof) return;
		pair.querySelectorAll(".pane").forEach((pane) => {
			const [seed, opts] = proof[pane.dataset.side]();
			paintPane(pane, seed, opts);
		});
	});
	const label = document.querySelector('[data-proof="styles"] .after-label');
	if (label) {
		label.textContent =
			resting.style === "FRUIT_SALAD" ? "Rainbow" : "Fruit Salad";
	}
}

function initProofs() {
	document.querySelectorAll(".pair[data-proof]").forEach((pair) => {
		const proof = PROOFS[pair.dataset.proof];
		if (!proof) return;
		pair.querySelectorAll(".mini").forEach((mini) => {
			mini.innerHTML = MINI[proof.widget]();
		});
	});
	repaintProofs();
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
const SEED_NAMES = {
	"#51BDFF": "Sky blue",
	"#F44336": "Red",
	"#FFB300": "Amber",
	"#4CAF50": "Green",
	"#26A69A": "Teal",
	"#7C4DFF": "Violet",
	"#EC407A": "Pink",
};
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
				`<button class="demo-swatch" data-seed="${s}" aria-pressed="false" aria-label="${SEED_NAMES[s] ?? s} seed">${swatch(s)}</button>`,
		).join("");
		updateSelection();
	};

	// Class toggle, not a rebuild: the ring cross-fades between swatches.
	const updateSelection = () => {
		seedsEl.querySelectorAll(".demo-swatch").forEach((b) => {
			const on = b.dataset.seed === seed;
			b.classList.toggle("on", on);
			b.setAttribute("aria-pressed", String(on));
		});
	};

	let resumeTimer = null;
	const apply = (rebuildSwatches, pause) => {
		if (pause) {
			demoHold = true;
			seedsEl.classList.remove("rotating");
		}
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
		if (resumeTimer) clearTimeout(resumeTimer);
		if (!matchMedia("(prefers-reduced-motion: reduce)").matches) {
			resumeTimer = setTimeout(() => {
				demoHold = false;
				seedsEl.classList.add("rotating");
			}, 45000);
		}
	};

	seedsEl.addEventListener("click", (e) => {
		const btn = e.target.closest(".demo-swatch");
		if (!btn) return;
		seed = btn.dataset.seed;
		apply(false, true);
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
						return `<button class="menuitem${on ? " selected" : ""}"${on ? ' aria-current="true"' : ""} data-value="${v}">${l}</button>`;
					})
					.join("");
			}
			menu.hidden = !open;
			wrap.classList.toggle("open", open);
			btn.setAttribute("aria-expanded", String(open));
			if (open) menu.querySelector(".selected, .menuitem")?.focus();
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
		siteDark = stored
			? stored === "dark"
			: !matchMedia("(prefers-color-scheme: light)").matches;
	} catch {
		siteDark = true;
	}
	document.documentElement.classList.toggle("light", !siteDark);
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
	const syncLabel = () => {
		btn.setAttribute("aria-pressed", String(siteDark));
		btn.setAttribute("aria-label", siteDark ? "Dark theme" : "Light theme");
	};
	syncLabel();
	initToggleTuck(btn);
	btn.addEventListener("click", () => {
		siteDark = !siteDark;
		syncLabel();
		try {
			localStorage.cbMode = siteDark ? "dark" : "light";
		} catch {
			/* private mode */
		}
		const root = document.documentElement;
		root.classList.toggle("light", !siteDark);
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
	const rows = buildPalette(
		seed,
		theme.style ?? "TONAL_SPOT",
		SPEC_BY_VERSION[theme.colorSpecVersion] ?? DEFAULT_SPEC,
		siteDark,
		themeSliders(theme),
		theme,
	);
	const role = roleReader(rows, siteDark);
	const at = (row, tone) => rows[row][TONES.indexOf(tone)];
	const container = role("surfaceContainerHigh");

	return {
		halfCircle: at(0, 80),
		firstQuarter: at(2, 70),
		secondQuarter: at(1, 60),
		square: at(4, 30),
		center: seed,
		container,
		text: ensureContrast(role("onSurface"), container),
		subtle: ensureContrast(role("onSurfaceVariant"), container),
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

// Live stars + release download totals, counted up when they arrive;
// markup values stay for no-JS / fetch failure.
function initStats() {
	const dlEl = document.getElementById("statDownloads");
	const starsEl = document.getElementById("statStars");
	const ossEl = document.getElementById("statOss");
	if (!dlEl || !starsEl || !ossEl) return;
	const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
	const restore = (el) => {
		el.textContent = "";
		el.removeAttribute("aria-hidden");
	};
	for (const el of [dlEl, starsEl]) el.setAttribute("aria-hidden", "true");
	if (!reduced) {
		dlEl.textContent = "0";
		starsEl.textContent = "0";
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
			el.removeAttribute("aria-hidden");
			return;
		}
		const t0 = performance.now();
		const tick = (now) => {
			const p = Math.min(1, (now - t0) / 1400);
			const eased = 1 - (1 - p) ** 3;
			el.textContent = fmt(Math.round(target * eased));
			if (p < 1) requestAnimationFrame(tick);
			else el.removeAttribute("aria-hidden");
		};
		requestAnimationFrame(tick);
	};
	// Hold count-up until the hero entrance settles.
	const settled = new Promise((resolve) => setTimeout(resolve, 500));
	ossEl.textContent = "100%";
	const REPO = "https://api.github.com/repos/Mahmud0808/ColorBlendr";
	const TTL = 6 * 3600 * 1000;

	const cached = () => {
		try {
			const c = JSON.parse(localStorage.cbStats);
			return Date.now() - c.at < TTL ? c : null;
		} catch {
			return null;
		}
	};
	const store = (key, value) => {
		try {
			const c = cached() ?? { at: Date.now() };
			localStorage.cbStats = JSON.stringify({ ...c, [key]: value, at: Date.now() });
		} catch {
			/* private mode */
		}
	};
	const show = async (el, value, key) => {
		if (!value) return restore(el);
		store(key, value);
		await settled;
		countTo(el, value, compact);
	};

	// Cached figures spare the 60/hour rate limit and a 157KB releases page.
	const hit = cached();
	if (hit?.stars && hit?.downloads) {
		show(starsEl, hit.stars, "stars");
		show(dlEl, hit.downloads, "downloads");
		return;
	}

	fetch(REPO)
		.then((r) => (r.ok ? r.json() : null))
		.then((repo) => show(starsEl, repo?.stargazers_count, "stars"))
		.catch(() => restore(starsEl));
	fetch(`${REPO}/releases?per_page=100`)
		.then((r) => (r.ok ? r.json() : null))
		.then((releases) =>
			show(
				dlEl,
				Array.isArray(releases)
					? releases
							.flatMap((rel) => rel.assets ?? [])
							.reduce((sum, a) => sum + (a.download_count ?? 0), 0)
					: 0,
				"downloads",
			),
		)
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

async function loadRail() {
	const rail = document.getElementById("rail");
	if (!rail) return;
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
			const untab = (h) => h.replaceAll('<a class="tcard"', '<a tabindex="-1" class="tcard"');
			const clone = (h) =>
				`<div class="mq-half" aria-hidden="true">${untab(h)}</div>`;
			// Second row: mobile only, reversed list, opposite drift.
			rail.innerHTML =
				`<div class="marquee-track">${half}${clone(half)}</div>` +
				`<div class="marquee-track track2" aria-hidden="true">${untab(halfReversed)}${untab(halfReversed)}</div>`;
		};
		buildRail();
		window.addEventListener("resize", buildRail);
		railRefresh = () => {
			builtPerHalf = 0;
			buildRail();
		};
		initHoverTheming(rail, new Map(themes.map((t) => [t.id, t])));
	} catch {
		rail.innerHTML =
			'<div class="loading" role="status">' +
			"Could not load community themes right now. " +
			'<button class="retry" type="button">Try again</button> or ' +
			'<a href="https://mahmud0808.github.io/ColorBlendr-Themes/">browse the gallery</a>.' +
			"</div>";
		rail.querySelector(".retry")?.addEventListener("click", () => {
			rail.innerHTML =
				'<div class="loading" role="status">Loading themes…</div>';
			loadRail();
		});
	}
}

export async function initApp() {
	initMode();
	initProofs();
	applySiteSeed(INITIAL_SEED);
	initFaq();
	initDemo();
	startSeedRotation();
	initModeToggle();
	initStats();
	await loadRail();
}
