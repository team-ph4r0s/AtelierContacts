# Security audit

## Scope

The audit covers the application manifest, vault engine, vCard boundary, phone/name normalization, and local persistence model.

## Network and permissions

`AndroidManifest.xml` contains no network permission. The support URL is opened only after an explicit button press with a safe browser fallback. Contact and calendar integrations are designed to use Android providers locally.

## Vault envelope

The binary envelope starts with `ATEL01`, a KDF identifier, a big-endian 600,000 iteration count, a 32-byte salt, and a 12-byte IV. The complete 55-byte header is authenticated as AES-GCM AAD. Authentication failures are surfaced as `DecryptionAuthException`.

## Input boundaries

vCard payloads are capped at 4 MiB, line values are escaped for RFC delimiters, and parser fields are read using exact property names. ContentResolver selections use placeholders and argument arrays. Phone values are normalized before deduplication.

## Residual review items

System provider writes and calendar sync should be exercised on a device with explicit runtime grants before distribution. Release signing must be performed outside the repository with a protected key store.
