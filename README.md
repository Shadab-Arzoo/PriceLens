# PriceLens 🔍

**PriceLens** is a modern, native Android application designed to empower shoppers with truthful, transparent product price comparisons across major Indian retailers (Amazon, Flipkart, Croma, Reliance Digital, Vijay Sales). Built with Kotlin, Jetpack Compose, Material 3, and Clean Architecture.

[![GitHub Release](https://img.shields.io/github/v/release/Shadab-Arzoo/PriceLens?include_prereleases&color=blue&logo=github)](https://github.com/Shadab-Arzoo/PriceLens/releases)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)

---

## 📱 Download & Try the App

You can download the pre-built, installable Android APK directly from our GitHub Releases:

👉 **[Download PriceLens APK (v1.4.0)](https://github.com/Shadab-Arzoo/PriceLens/releases/latest)**

### Installation Instructions
1. Download `pricelens-v1.4.0.apk` onto your Android device (or transfer via USB / adb).
2. Tap the downloaded APK to install. If prompted by Android, allow "Install unknown apps" for your browser or file manager.
3. Open **PriceLens** and enjoy comparing prices!

*(Alternatively install via adb: `adb install -r pricelens-v1.4.0.apk`)*

---

## ✨ Features

- **🌓 Pitch Black Dark Mode & Clean Light Mode**: Premium OLED pitch black ground (`#000000`), deep surface cards (`#0B0B0B`), hairline borders (`#1D1D1D`), high-contrast typography, and Brand Mint (`#6EE3AD`) accents with an instant persistent theme toggle switch.
- **🌐 Universal Store Price Tracking**: Paste product URLs from ANY web store on the internet (Nike, Zara, Apple, Myntra, etc.) with automatic OpenGraph/schema metadata extraction.
- **🎯 In-App Interactive Web Price Picker (`WebPricePickerScreen`)**: Clean in-app browser view allowing users to tap on the exact price element on any webpage, highlighting it with a glowing Mint box and capturing the CSS selector for background tracking.
- **🔔 Actionable Price Drop Notifications**: Enriched notifications featuring calculated price savings badges, high priority heads-up alerts, and direct "View Deal" and "Open PriceLens" action buttons.
- **🚀 Animated Onboarding & Hero Illustrations**: 4 engaging first-launch slides with custom resolution-independent vector illustrations, gentle floating spring physics, and feature cards.
- **🔍 Truthful Multi-Retailer Comparison**: Real-time normalized offers across Amazon, Flipkart, Croma, Reliance Digital, and Vijay Sales with lowest-price badges and explicit out-of-stock / unavailable retailer indicators.
- **👆 Interactive Price History Touch Scrubbing**: Real-time touch and drag gestures on the Canvas price sparkline with tactile haptic feedback (`HapticFeedbackType.TextHandleMove`), dashed vertical guide, and instant observation callouts showing retailer, timestamp, and historical price.
- **🔗 Smart Native Shortlinks & Share Text Parsing**: Seamlessly parses short links (`amzn.to`, `amzn.in`, `fkrt.it`) and extracts URLs from full app share messages when sharing directly from Amazon or Flipkart apps.
- **📲 Android System "Share via PriceLens" (`ACTION_SEND`)**: Tap "Share" directly within Amazon, Flipkart, Myntra, or Chrome and select **PriceLens** from Android's native share sheet for instantaneous comparison without manual copying.
- **📋 Smart Clipboard Auto-Detection**: Instant floating banner when a supported retailer link is detected in your clipboard for one-tap comparison.
- **📌 Dedicated Tracked Watchlist Screen**: Comprehensive alert dashboard with live price tracker stats, real-time savings badges ("Target met! Price dropped by ₹..."), and instant product navigation.
- **🔔 Background Price Monitoring Worker (`WorkManager`)**: Periodic background checks for tracked products with native high-priority Android notifications when prices drop at or below target thresholds.
- **🔗 Link-First Comparison Engine**: Paste any retailer product link or tap "Paste from Clipboard" to instantly compare competitor offers and identify authorized models.
- **📈 Interactive Animated Price History**: Progressive Canvas sparkline charting price fluctuations with deal confidence scoring and price drop alerts.
- **💫 Fluid Motion & Micro-Interactions**: Shared-axis screen elevation transitions, spring physics on buttons and cards, and `Modifier.animateItem()` list reordering.
- **🎨 Modern Adaptive Icon**: Optical prism lens design featuring a geometric Rupee emblem, specular bezel glint, and an emerald savings focal gem.
- **💾 Local Caching & Sync**: Room-backed persistence keeping price tracking alerts, watchlist items, and search history automatically synchronized.
- **🔒 Privacy & Security First**: Zero account or invasive permissions required; 100% private on-device processing.

---

## 🏗️ Architecture & Tech Stack

PriceLens follows strict Android **Clean Architecture** patterns:

- **Presentation Layer**: 100% Jetpack Compose with Material 3, Navigation Compose, unidirectional data flow (UDF) via `ViewModel` and `StateFlow`.
- **Domain Layer**: Pure business logic models, use cases (`SearchProductsUseCase`, `GetProductComparisonUseCase`, `ValidateProductUrlUseCase`).
- **Data Layer**: Clean repository implementation separating local persistence (`Room`) and remote providers (`RetailerPricingAdapter`).
- **Dependency Injection**: Dagger Hilt for modular, testable components.
- **Asynchronous Flow**: Kotlin Coroutines & Reactive StateFlow.
- **Quality Assurance**: Comprehensive unit and integration test suite covering viewmodels, use cases, and repository adapters.

---

## 🛠️ Building from Source

### Prerequisites
- JDK 17 (Temurin recommended)
- Android SDK with Platform 35 and Build Tools 35.0.0
- Android Studio (Ladybug / Koala or newer)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/Shadab-Arzoo/PriceLens.git
cd PriceLens

# Run tests
./gradlew test

# Assemble Debug APK
./gradlew assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

This project is licensed under the MIT License.
