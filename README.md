# Obsidian Lumina — Native Android App (Kotlin + Jetpack Compose)

This is a **100% native Android application** written in Kotlin and Jetpack Compose (Material 3), designed with the Obsidian Light Architectural visual system for home automation.

## Features
- **Control Screen**: Segmented room zone switcher, quick scenes, Master Cutoff, and tactile Touch-Tile switch plates with Cyan/Amber/Emerald LED states.
- **Native Bottom Sheet Controls**: Recessed linear dimmer slider, 2700K/3500K/5200K color temperature presets, 3-speed ceiling fan selector, climate +/- steppers, and motorized blind sliders.
- **Scenes & Automations**: Multi-room scene execution, scheduled routines, and custom schedule creation.
- **Power & Node Telemetry**: Live kW/V/A draw, daily kWh cost projections, per-room load bars, relay dispatch log, and IP/MAC hardware node registry.
- **Real Home Hardware Bridges**:
  - **Home Assistant REST API** (`/api/services/...` with Bearer Token)
  - **Direct LAN HTTP Webhooks** (Tasmota, Shelly, ESPHome on your local Wi-Fi)
  - **Local Persistent State** (`SharedPreferences` offline-first storage)

## How to Build the `.apk`
### Option A — Android Studio (Local)
1. Open the `android/` folder in **Android Studio**.
2. Wait for Gradle Sync to finish.
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Copy `app/build/outputs/apk/debug/app-debug.apk` to your Android phone and install.

### Option B — Cloud APK Build (GitHub Actions, no local SDK needed)
1. Push the `android/` directory to a GitHub repository.
2. Open the **Actions** tab and run **Build Native Android APK**.
3. Download the generated `obsidian-lumina-debug-apk` artifact directly onto your Android phone.
