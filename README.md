<div align="center">

# Atelier Contacts

**Privacy-first contact cleanup for Android**

<a href="#русская-версия"><img src="https://img.shields.io/badge/Русский-2E7D6F?style=for-the-badge" alt="Русская версия"></a>
<a href="#english-version"><img src="https://img.shields.io/badge/English-1D1D1F?style=for-the-badge" alt="English version"></a>
<a href="https://github.com/team-ph4r0s/AtelierContacts/actions"><img src="https://img.shields.io/github/actions/workflow/status/team-ph4r0s/AtelierContacts/android.yml?branch=main&style=for-the-badge&label=CI" alt="CI"></a>
<a href="https://github.com/team-ph4r0s/AtelierContacts/releases"><img src="https://img.shields.io/github/v/release/team-ph4r0s/AtelierContacts?style=for-the-badge&label=Releases" alt="Releases"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/license-GPL--3.0--or--later-315A9D?style=for-the-badge" alt="GPL-3.0-or-later"></a>

<p>
  <a href="https://github.com/team-ph4r0s/AtelierContacts/actions"><strong>CI</strong></a>
  · <a href="https://github.com/team-ph4r0s/AtelierContacts/releases"><strong>Download APK</strong></a>
  · <a href="SECURITY_AUDIT.md"><strong>Security audit</strong></a>
  · <a href="PRIVACY_POLICY.md"><strong>Privacy</strong></a>
</p>

</div>

<a id="русская-версия"></a>

## Русская версия

Atelier Contacts — спокойное локальное рабочее место для адресной книги. Оно помогает найти дубликаты, привести имена и телефоны в порядок, сохранить контекст, не забыть дни рождения и сделать переносимый или зашифрованный бэкап.

Проект разработан [Pharos Team](https://github.com/team-ph4r0s) и рассчитан на F-Droid, RuStore и воспроизводимые сборки сообщества.

### Возможности

| Раздел | Что умеет |
| --- | --- |
| Контакты | Поиск по имени, телефону, заметке и тегам с русско-английской транслитерацией |
| Очистка | Поиск неаккуратных имён, пропущенного контекста, дней рождения и неформатированных номеров |
| Дубликаты | Кластеры по E.164, суффиксу номера, переставленным словам и нечёткому совпадению |
| Дни рождения | Ежегодные события календаря с устойчивым маркером без повторов |
| Перенос | UTF-8 vCard с экранированием полей RFC |
| Vault | AES-256-GCM + PBKDF2-HMAC-SHA256, 600 000 итераций, authenticated header |
| Интерфейс | 10 бутиковых тем, плавные переходы, haptic feedback и Compose UI |

В приложении нет разрешений `INTERNET` и `ACCESS_NETWORK_STATE`, рекламы, аналитики, Firebase, Google Play Services и облачной синхронизации контактов. Внешняя ссылка только одна — добровольная поддержка Pharos Team.

### Экраны

- **Контакты** — поиск, избранное, health score и локальный demo fallback.
- **Поток** — triage-карточки с подсказками очистки и прогрессом.
- **Дубликаты** — просмотр кластеров с сохранением уникальных телефонов, заметок, тегов и дат.
- **Календарь** — лента дней рождения и идемпотентная синхронизация.
- **Бутик** — темы, privacy status, vCard export и support link.

### Сборка

Нужны JDK 17+, Android SDK platform 35+ и рабочий Gradle Wrapper:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew assembleRelease
```

Для локальной подписи задаются только через переменные окружения:

```text
SIGNING_KEYSTORE_PATH
SIGNING_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
```

Keystore и пароли запрещено коммитить. GitHub Actions получает их из Repository Secrets и публикует APK только для тегов `v*`.

<a href="#english-version"><img src="https://img.shields.io/badge/English%20version-1D1D1F?style=for-the-badge" alt="English version"></a>

<a id="english-version"></a>

## English version

Atelier Contacts is a calm, local-first workspace for turning a messy address book into something you can trust. It combines cleanup, duplicate review, birthday context, vCard portability, and an encrypted vault in a boutique Compose interface.

The project is built by [Pharos Team](https://github.com/team-ph4r0s) for F-Droid, RuStore, and reproducible community builds.

### Features

| Area | Capability |
| --- | --- |
| Contacts | Search by name, phone, note, tag, or transliterated Russian/English text |
| Cleanup | Detect messy names, missing context, missing birthdays, and unnormalized numbers |
| Duplicates | Group exact E.164 matches, suffix matches, and reversed/fuzzy names before merging |
| Birthdays | Upsert annual calendar events with stable markers and no duplicate sync entries |
| Portability | Import/export UTF-8 vCard data with escaped RFC fields |
| Vault | AES-256-GCM with PBKDF2-HMAC-SHA256 at 600,000 iterations and authenticated metadata |
| Design | Ten Atelier palettes, animated transitions, haptics, and adaptive Compose UI |

Atelier Contacts has no `INTERNET` or `ACCESS_NETWORK_STATE` permission, advertising ID, telemetry, Firebase, Google Play Services, or cloud contact sync. The optional Pharos Team support action is the only external link.

### Security design

```text
ATEL01 | KDF id | iterations | 32-byte salt | 12-byte IV | AES-GCM ciphertext + tag
```

The complete header is bound as AES-GCM additional authenticated data. Changing KDF metadata, salt, IV, or ciphertext fails authentication. Passwords enter the API as `CharArray` and are wiped in `finally` blocks. vCard input is size-bounded, escaped, and parsed without dynamic SQL or provider selections.

Read [SECURITY_AUDIT.md](SECURITY_AUDIT.md) and [PRIVACY_POLICY.md](PRIVACY_POLICY.md) for the full review.

### Build and CI

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew assembleRelease
```

`.github/workflows/android.yml` runs tests, lint, debug/release assembly, secret-material checks, and artifact upload for pushes and pull requests. A tag such as `v0.1.0` additionally creates a GitHub Release with the APKs and generated notes.

Release signing uses:

```text
SIGNING_KEYSTORE_PATH
SIGNING_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
```

Never commit a keystore, private key, or password.

### Project structure

```text
app/src/main/java/team/pharos/ateliercontacts/
├── core/   crypto, vCard, normalization, duplicate and health engines
├── data/   Room journal, provider writes, repository and calendar sync
└── ui/     themes, haptics and preferences
```

<a href="#русская-версия"><img src="https://img.shields.io/badge/Русская%20версия-2E7D6F?style=for-the-badge" alt="Русская версия"></a>

## License

Atelier Contacts is licensed under [GPL-3.0-or-later](LICENSE).
