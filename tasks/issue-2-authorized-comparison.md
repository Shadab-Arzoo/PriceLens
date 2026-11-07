After Shadab selects and approves an authorized pricing/affiliate data source, deliver one real end-to-end product search and comparison path. Research may identify options, but do not sign up for, pay for, or integrate a paid service without explicit approval.

## Acceptance criteria

- A server-owned adapter uses the approved provider's documented, permitted access method; provider credentials never enter the Android app.
- Search results and product details display normalized real offers with retailer/source, timestamp, currency, and explicit unavailable states.
- Product matching does not merge uncertain products silently; malformed/provider failures produce safe UI errors.

## Verification

- Automated mapping/repository tests cover success, unavailable, and provider-error responses.
- An integration/manual test demonstrates comparison for a product actually returned by the approved source.

## Dependencies

Foundation Android app and Shadab's data-source approval.
