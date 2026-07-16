<div align="center">
  <img src="https://raw.githubusercontent.com/Mahmud0808/ColorBlendr/master/.github/resources/banner.png" width="100%" alt="Banner">

#
</div>
<p align="center">
  <a href="https://github.com/Mahmud0808/ColorBlendr"><img alt="Repo Size" src="https://img.shields.io/github/repo-size/Mahmud0808/ColorBlendr?style=for-the-badge"></a>
  <a href="https://github.com/Mahmud0808/ColorBlendr/releases"><img src="https://img.shields.io/github/downloads/Mahmud0808/ColorBlendr/total?color=%233DDC84&logo=android&logoColor=%23fff&style=for-the-badge" alt="Downloads"></a>
  <a href="https://t.me/DrDsProjects"><img src="https://img.shields.io/badge/Telegram-Join-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram"></a>
</p>
<div align="center">

# ColorBlendr

### Customize Material You colors of your device.
</div>
<p align="center">
Elevate your creativity with effortless material customization. Instantly tweak colors for a personalized touch in just a few taps.
</p>
<br>
<div align="center">
  <a href="https://f-droid.org/en/packages/com.drdisagree.colorblendr/"><img src="https://raw.githubusercontent.com/Mahmud0808/ColorBlendr/master/.github/resources/fdroid-button.png" width="30%" alt="Get it on F-Droid" /></a>
  <br>
  <a href="https://apt.izzysoft.de/packages/com.drdisagree.colorblendr/"><img src="https://raw.githubusercontent.com/Mahmud0808/ColorBlendr/master/.github/resources/izzyondroid-button.png" width="30%" alt="Get it on IzzyOnDroid" /></a>
  <br>
  <a href="https://www.buymeacoffee.com/DrDisagree"><img src="https://raw.githubusercontent.com/Mahmud0808/ColorBlendr/master/.github/resources/bmc-button.png" width="30%" alt="Buy me a coffee" /></a>
  <br><br>
  <img src="https://raw.githubusercontent.com/Mahmud0808/ColorBlendr/master/.github/resources/features.png" width="100%" alt="Features">
</div>

## Features 🔥

**Color sources**

- Generate palettes from your wallpaper
- Pick any seed color, or choose from a set of basic colors
- Live preview before applying — see changes as you tweak them

**Fine-tuning**

- Accent saturation, background saturation, and background lightness sliders
- Ready-made Monet style presets
- Pitch black theme for dark mode
- Tinted text color
- Custom secondary and tertiary colors

**Full control**

- Manual per-shade color overriding across the whole palette
- Separate configurations for light and dark mode
- Extra tweaks: color spec version, screen-off updates, darker / semi-transparent Pixel launcher icons

**Community & backup**

- Community creations — browse, apply, upvote, and share themes (see below)
- Backup and restore your entire setup to a file
- Works with **Root**, **Shizuku**, or **Wireless ADB**
- and many more...

## Requirements 🛠

- Android 12+ ROM with Material You support
- Working **Root**, **Shizuku**, or **Wireless ADB** environment

## How to Use 🚀

- Download and install the apk
- Allow permissions for the app
- Select **Root**, **Shizuku**, or **Wireless ADB** mode
- That's it. Now you are good to go!

## Note 📝

- Root is recommended if you want to have the full experience
- Shizuku and Wireless ADB are also supported but customizations are limited

## Community Creations 🎨

Share your favorite color setups and discover themes crafted by others, right inside the app.

- **Browse & search** — explore a growing gallery of community-made themes, sorted by popularity or recency
- **One-tap apply** — preview any creation live and apply it instantly (root mode)
- **Upvote** — like the themes you enjoy so the best ones rise to the top
- **Share your own** — publish your current palette with a name and description

Everything is anonymous — no account, sign-up, or personal data required. Submissions go through a quick human review before they appear for everyone.

> [!NOTE]
>
> Browsing and upvoting work on every mode. Applying a community creation requires root.

## FAQ 🤓

<details>
  <summary>How does ColorBlendr work without root access?</summary>

- ColorBlendr utilizes adb commands to change Material You colors, allowing users to modify these colors without needing root access.
</details>

<details>
  <summary>How does ColorBlendr work with root access?</summary>

- ColorBlendr leverages the [FabricatedOverlay](https://developer.android.com/reference/android/content/om/FabricatedOverlay) API to dynamically change Material You colors at runtime without creating any permanent files.
</details>

<details>
  <summary>Why doesn't ColorBlendr work properly on OneUI?</summary>

- OneUI uses its own color palette for system apps, not Material You colors. As a result, ColorBlendr's modifications only affect Google apps and other apps that support Material You, but not OneUI system apps.
</details>

<details>
  <summary>Why are some features grayed out or cannot be enabled?</summary>

- These features either require a specific Android version or higher, or you need root access to unlock all features.
</details>

<details>
  <summary>How do I properly uninstall ColorBlendr?</summary>

- First, disable the ColorBlendr Service from app settings. Then, uninstall the app and reboot your device.
</details>

<details>
  <summary>Is any personal data collected when I share a theme?</summary>

- No. Community creations are fully anonymous — no account or personal data is attached. Only the palette itself, along with the name and description you provide, is submitted.
</details>

<details>
  <summary>Why can't I apply a community creation?</summary>

- Applying a community creation modifies system colors, which requires root. You can still browse, search, and upvote in Shizuku and Wireless ADB modes.
</details>

<a id="tasker-integration"></a>
<details>
  <summary>Can I automate theme changes with Tasker?</summary>

- Yes. Enable **Tasker integration** in the app's advanced settings, then send a broadcast from Tasker (or MacroDroid / Automate) with:
  - **Action:** `com.drdisagree.colorblendr.action.APPLY_CONFIG`
  - **Package:** `com.drdisagree.colorblendr` (required — broadcasts without it are dropped by Android)
  - **Target:** Broadcast Receiver

- Supported extras — include any combination; only the ones you send are changed:

  | Extra | Type | Values | What it does |
  |-------|------|--------|--------------|
  | `seedColor` | String | `#RRGGBB`, e.g. `#3F51B5` | Sets a custom seed color and generates the palette from it |
  | `randomColor` | Boolean | `true` | Picks a random vivid seed color |
  | `wallpaperColors` | Boolean | `true` | Switches back to wallpaper-based colors |
  | `monetStyle` | String | `TONAL_SPOT`, `VIBRANT`, `EXPRESSIVE`, `RAINBOW`, `FRUIT_SALAD`, `SPRITZ`, `MONOCHROMATIC`, `FIDELITY`, `CONTENT`, `CMF` | Sets the Monet style preset |
  | `accentSaturation` | Integer | `0`–`200` (100 = default) | Accent saturation slider |
  | `backgroundSaturation` | Integer | `0`–`200` (100 = default) | Background saturation slider |
  | `backgroundLightness` | Integer | `0`–`200` (100 = default) | Background lightness slider |
  | `pitchBlack` | Boolean | `true` / `false` | Toggles the pitch black dark theme |
  | `accurateShades` | Boolean | `true` / `false` | Toggles accurate shades |
  | `config` | String | JSON with any of the keys above, e.g. `{"seedColor":"#3F51B5","pitchBlack":true}` | Sends everything in one extra — useful since Tasker's UI has limited extra fields |

- Values outside their allowed range or malformed colors are ignored. Extras sent both individually and inside `config` — the individual one wins.
- Sending any of the three slider extras turns off **Mode specific themes**, since the automated value replaces the separate light/dark slider setup.
- If **Update colors when screen off** is enabled, automated changes are held back and applied once the screen actually turns off.
- Example: a Tasker time profile repeating every hour + Send Intent with extra `randomColor:true` gives you a fresh random theme every hour.
</details>

## Translation 🌐

- Assist in translating ColorBlendr into your preferred language through [our Crowdin platform](https://crowdin.com/project/ColorBlendr). Your contribution will help make ColorBlendr accessible to a wider audience.

## Credits 🤝

- [@siavash79](https://github.com/siavash79) for helping me.
- [@fennifith](https://github.com/fennifith) for color picker.
- [@MuntashirAkon](https://github.com/MuntashirAkon) for lib adb.
- And everyone who [contributed](https://github.com/Mahmud0808/ColorBlendr/blob/master/docs/contributors.md) and [translated](https://github.com/Mahmud0808/ColorBlendr/blob/master/docs/translators.md)... :)
