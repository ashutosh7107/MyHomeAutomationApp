# B-502 Automation — Living Room Switchboard (Native Android)

A clean, single-screen native Kotlin + Jetpack Compose smart touch switchboard matching your physical 2×6 Living Room wall plate:

```text
Row 1: [ + ] | [ #1 ] | [ #2 ] | [ All On  ] | [ #5 ] | [ #6 ]
Row 2: [ - ] | [ #3 ] | [ #4 ] | [ All Off ] | [ #7 ] | [ #8 ]
```

## Features
- **Exact 2×6 Touch Plate**: Switches `#1`–`#8`, `[+]`/`[-]` level steppers (1–5), `[All On]`, and `[All Off]`
- **Built-in Android IR Blaster**: Transmits 38 kHz NEC (or raw microsecond timings) via `ConsumerIrManager` when your phone has an IR emitter
- **Edit Labels Mode**: Tap **Edit Labels** in the top bar, then tap any switch (`#1`–`#8`), `+`/`-`, or `All On`/`All Off` to rename it for your home's lights/appliances, pick an icon (`Light`, `Fan`, `AC`, `Outlet`), customize its IR hex code, and optionally attach local Wi-Fi HTTP ON/OFF URLs

## Build the `.apk` on GitHub Actions
1. Push the contents of this folder to a GitHub repository.
2. Open the **Actions** tab and run **Build Native Android APK**.
3. Download **`b502-automation-debug-apk`** (`app-debug.apk`) and install it on your Android phone.
