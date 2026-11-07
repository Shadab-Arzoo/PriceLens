Add the truthful price-history and target-price alert vertical slice on top of the approved comparison provider. Price observations must be recorded data, never seeded as commerce facts.

## Acceptance criteria

- Backend monitoring records immutable timestamped normalized price observations and exposes current/low/high/average only from recorded data.
- The app shows chart/statistics only when qualifying history exists, otherwise explains that history is not available yet.
- A user can create/manage a target alert; idempotent server-side evaluation sends FCM and retains notification history when the target is reached.

## Verification

- Tests cover empty/insufficient history, statistics, target crossing, and duplicate evaluation.
- Demonstrate an end-to-end test alert using a controlled provider/test product and device/emulator notification.

## Dependencies

Approved comparison integration.
