Add account-backed synchronization for watchlists/saved searches/preferences and complete production readiness for the validated MVP.

## Acceptance criteria

- Authentication and account sync protect user data with secure session handling and clear signed-out/offline behavior.
- Watchlists, saved searches, alert preferences, and notification history synchronize correctly across sessions.
- Accessibility, privacy/security review, crash/operational observability, release documentation, and build/release checks meet the agreed launch bar.

## Verification

- Tests cover authentication states, authorization boundaries, sync conflict/offline behavior, and alert preference changes.
- A release-candidate build is verified for accessible light/dark UI, notification delivery, and no embedded secrets.

## Dependencies

Real comparison, price-history/alerts, and discovery work.
