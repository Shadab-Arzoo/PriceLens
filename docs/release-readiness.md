# PriceLens release readiness

## Completed in the Android client

- Release builds enable R8 shrinking and resource shrinking.
- Android backup and device transfer exclude the local database and preferences.
- Session credentials have a Keystore-backed AES-GCM storage boundary in
  `SecureSessionStore`; plaintext tokens are never written to app preferences.
- Sync orchestration rejects signed-out and expired sessions before sending
  records to a gateway, and represents offline and conflict outcomes explicitly.
- No retailer credentials or other secrets are included in source or resources.

## Required before production

The client does not ship an account provider or backend sync implementation.
Before release, provide an authenticated HTTPS backend and bind
`AccountSyncGateway` to it. The backend must authorize every record by account,
use server revisions for conflict resolution, and retain notification history
idempotently. Add integration tests for authorization, refresh/revocation,
offline retry, and cross-session synchronization before enabling account UI.

Run the release checks with:

```bash
./gradlew testDebugUnitTest assembleRelease
```
