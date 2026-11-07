# PriceLens 🔍

**PriceLens** is a modern, native Android application designed to empower shoppers with truthful, transparent product price comparisons across major retailers (Amazon, Flipkart, Croma, Reliance Digital, Vijay Sales). Built with Kotlin, Jetpack Compose, Material 3, and Clean Architecture.

[![GitHub Release](https://img.shields.io/github/v/release/Shadab-Arzoo/PriceLens?include_prereleases&color=blue&logo=github)](https://github.com/Shadab-Arzoo/PriceLens/releases)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)

---

## 📱 Download & Try the App

You can download the pre-built, installable Android APK directly from our GitHub Releases:

👉 **[Download PriceLens APK (v1.0.0)](https://github.com/Shadab-Arzoo/PriceLens/releases/latest)**

### Installation Instructions
1. Download `pricelens-v1.0.0.apk` onto your Android device (or transfer via USB / adb).
2. Tap the downloaded APK to install. If prompted by Android, allow "Install unknown apps" for your browser or file manager.
3. Open **PriceLens** and enjoy comparing prices!

*(Alternatively install via adb: `adb install -r pricelens-v1.0.0.apk`)*

---

## ✨ Features

- **🔍 Truthful Multi-Retailer Comparison**: Real-time normalized offers across Amazon, Flipkart, Croma, Reliance Digital, and Vijay Sales with lowest-price badges and explicit out-of-stock / unavailable retailer indicators.
- **🛡️ Strict Product Matching**: Safe threshold matching prevents misleading product merges.
- **🎨 Liquid Glass Visual Design**: Material 3 theming with dynamic dark and light mode, accessible contrast, and fluid motion.
- **📷 Barcode & QR Scanner**: Instant camera scanner entry point for in-store price checks.
- **🔗 Paste URL Search**: Paste any retailer product link to instantly lookup comparison alternatives.
- **💾 Local Caching & Watchlist**: Room-backed persistence for search history and watchlists.
- **🔒 Privacy & Security First**: Zero embedded secrets; isolated server-owned adapter architecture.

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
