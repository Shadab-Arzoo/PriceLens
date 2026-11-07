Create the initial native Android application foundation for PriceLens. Establish a buildable Kotlin/Jetpack Compose project with clean presentation, domain, and data boundaries; do not add mock or fabricated product prices/history.

## Acceptance criteria

- The project builds and launches a Compose dashboard with search, scan, and paste-URL entry points plus clear empty/unavailable states.
- Hilt, navigation, Coroutines/StateFlow, and Room are wired through small, testable boundaries; no credentials are present in source or app resources.
- Light and dark Material 3 themes implement the agreed accessible, Liquid Glass-inspired visual system without relying on blur for content.

## Verification

- Run the repository's debug build and focused unit tests.
- Verify dashboard loading, empty, and error states on an emulator/device in light and dark themes.

## Dependencies

None.
