Expand the proven comparison path with permitted URL ingestion, camera/image OCR product confirmation, and alternatives/specification comparison. Each input must lead to an explicit confirmed product identity before retailer offers are shown.

## Acceptance criteria

- Permitted pasted URLs are parsed through supported/provider-approved paths; unsupported URLs explain the limitation instead of scraping.
- Camera/image OCR yields editable identification suggestions and requires user confirmation before comparison.
- Alternatives are based on normalized catalog/spec data and visibly explain meaningful differences and lower available price when applicable.

## Verification

- Tests cover unsupported URLs, OCR confirmation/correction, and alternatives ranking/explanation.
- Manual tests validate permission denial, image selection, and a confirmed comparison flow.

## Dependencies

Approved comparison integration and a source of permitted product/catalog metadata.
