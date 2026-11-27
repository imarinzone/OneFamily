# One Family

One Family is an open-source Android app for tracking personal and household finances. It combines manual transaction entry, bank/UPI SMS parsing, shared household data, budgets, analytics, and export tools in one Kotlin/Jetpack Compose application.

> **Project status:** One Family is under active development. APIs, data models, Firebase configuration, and UI details may change between releases.

## What it does

- Track personal and shared household income and expenses.
- Organize transactions by category, account, payment mode, tags, and notes.
- Import supported bank and UPI transaction alerts from Android SMS.
- Parse Indian bank/UPI messages through the bank parser package in `app/src/main/java/com/ritesh/parser`.
- Detect likely duplicate SMS/ledger entries and link them to one transaction.
- Browse transactions by month with animated date navigation.
- Create category budgets and overall monthly budgets with configurable alert thresholds.
- View household and category analytics.
- Manage personal and shared accounts, household members, and transaction visibility.
- Export transactions to CSV.
- Sync authenticated user and household data with Cloud Firestore.
- Authenticate with Firebase Authentication, including Google sign-in support through Android Credential Manager.
- Customize the light, system, and AMOLED-dark themes.
- Customize the finance wallpaper with icons/emojis, vector doodles, or a minimal grid, plus density, accent, and intensity controls.
- Receive local notifications for incoming SMS and spend summaries where Android permissions and device capabilities allow it.
- Run a mock-user flow in debug builds with realistic sample accounts and transaction history.
- Record local stability diagnostics and apply defensive guardrails around SMS parsing, persistence, and responsive typography.

## Screens and workflows

### Home and transactions

The Home tab provides the current ledger, month navigation, account balances, and personal/shared transaction views. Transactions can be created manually, edited, excluded from expense calculations, tagged, and linked to related SMS or ledger records.

### SMS import and parsing

On devices with telephony support, the app can request SMS access and scan supported bank/UPI messages. The first-time sync flow reports progress while messages are parsed and persisted. Incoming SMS can also be handled by the registered Android SMS receiver.

SMS support is intentionally conservative: bank message formats vary by sender, locale, and time. Unsupported messages should remain untouched rather than being guessed into financial records.

### Duplicate linking

Swipe a transaction from start to end, or use the link action on a transaction, to find likely duplicate records. Candidates are ranked using amount and time proximity. Linking preserves the primary ledger item while retaining linked SMS signatures so future scans do not re-import the same alert.

### Budgets and analytics

Create, edit, or clear monthly budgets for individual categories and the household overall. The app validates numeric inputs and displays usage against configured limits. The Analysis tab provides period-based summaries for income, expenses, categories, and accounts.

### Household sharing

Authenticated users can create or join a household, manage members, and mark transactions as personal or shared. Firestore security rules validate document shape, ownership, household membership, amounts, categories, timestamps, and other financial fields.

### Theme and wallpaper

Settings includes theme selection and an optional finance wallpaper. Wallpaper styles include **Icons + Emojis**, **Finance Doodles**, and **Minimal Grid**. Density, accent color, and opacity/intensity can be adjusted without changing the ledger.

## Technology stack

- **Language:** Kotlin 2.2.10
- **UI:** Jetpack Compose with Material 3
- **Build:** Gradle 9.3.1 wrapper and Android Gradle Plugin 9.1.1
- **Android:** `minSdk 24`, `targetSdk 36`, compile SDK 36.1
- **Java:** Java 11 source and target compatibility
- **Architecture:** Compose UI, lifecycle-aware ViewModels, coroutines, repository/data-store layers
- **Authentication and data sync:** Firebase Authentication and Cloud Firestore
- **Local test stack:** JUnit, Robolectric, AndroidX test, Compose UI test APIs
- **Backend rules/emulators:** Firebase CLI, Firestore rules, Auth and Firestore emulators

The dependency catalog is the source of truth for versions and lives in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Requirements

Install the following before building:

1. Git
2. Android Studio with Android SDK Platform 36 and the Android SDK build tools
3. A JDK supported by the installed Android Studio/AGP version; Java 11 is the project source/target level
4. An Android device or emulator running API 24 or newer
5. Firebase CLI if you want to run the local Auth/Firestore emulators

SMS import requires an Android device with SMS capability and the user granting the relevant runtime permissions. An emulator or tablet without telephony may still run the rest of the app, but SMS ingestion will not be available.

## Getting started

### 1. Clone the repository

```bash
git clone <repository-url>
cd OneFamily
```

Replace `<repository-url>` with the URL of your fork or the canonical repository.

### 2. Configure Firebase

The app uses Firebase Auth and Cloud Firestore. For a real Firebase project:

1. Create or select a Firebase project.
2. Register an Android app using the application ID from `app/build.gradle.kts`:
   `com.aistudio.onefamily.xpqrmk`.
3. Download `google-services.json` and place it in `app/google-services.json`.
4. Enable the Firebase Authentication providers you intend to use.
5. Create the Firestore database and deploy the checked-in rules:

   ```bash
   firebase deploy --only firestore:rules
   ```

`google-services.json` contains project-specific configuration and must not be committed unless the project explicitly chooses to publish it. Use a local copy for development. The Gradle configuration warns when the file is missing, which allows parts of the project to compile without a Firebase application configuration.

### 3. Optional secrets

The Secrets Gradle Plugin reads `.env` and uses `.env.example` as its template. Do not commit `.env` or real credentials.

```bash
cp .env.example .env
```

The current example contains an optional `GEMINI_API_KEY` placeholder for AI Studio workflows. Gemini integration is not an enabled runtime dependency in the current app build; do not add a key unless you also implement and review the corresponding feature.

### 4. Build and install

Use the checked-in Gradle wrapper so contributors get the project’s expected Gradle version:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

The debug build uses the repository’s configured debug signing setup. Release builds require local signing environment variables and a keystore; never put release passwords or keystores in source control.

## Local Firebase emulators

`firebase.json` configures these local services:

- Firestore: `127.0.0.1:8085`
- Authentication: `127.0.0.1:9099`

Start them with:

```bash
firebase emulators:start --only auth,firestore
```

Use emulator data only for development and tests. Before sharing emulator fixtures, review them for email addresses, phone numbers, financial records, and other personal information.

## Testing and verification

Run the JVM/Robolectric unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run Android instrumented tests on a connected device or emulator:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Run a broader verification pass before opening a pull request:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Tests are under `app/src/test` and `app/src/androidTest`. Firestore rules tests are in [`firestore.test.js`](firestore.test.js) and require the Firebase CLI/emulator setup described above.

## Repository layout

```text
.
├── app/
│   └── src/main/java/com/example/
│       ├── data/                 # local storage, repositories, SMS, CSV export
│       ├── ritesh/parser/        # bank and UPI message parsers
│       └── ui/                   # Compose screens, components, theme, ViewModels
├── firestore.rules               # Firestore validation and authorization rules
├── firestore.test.js              # Firestore rules tests
├── firebase.json                  # Emulator and rules configuration
├── gradle/libs.versions.toml      # Dependency and plugin versions
└── .env.example                   # Safe local-secrets template
```

## Privacy and security

This app handles highly sensitive information, including transaction history, SMS content, account names, and household membership. Contributors should:

- Request only the Android permissions needed for the feature being used.
- Explain SMS access clearly before requesting `READ_SMS` or `RECEIVE_SMS`.
- Never log full SMS bodies, authentication tokens, secrets, or payment details.
- Never commit `google-services.json`, `.env`, keystores, emulator exports, or real user data.
- Test Firestore changes against the rules and emulator before using a shared Firebase project.
- Treat parser fixtures as potentially sensitive; use synthetic messages with fake amounts and identifiers.
- Report suspected security issues privately to the repository maintainers rather than opening a public issue with exploitable details.

The Firestore rules default to deny-all and add narrowly scoped validation for supported documents. Review [`firestore.rules`](firestore.rules) whenever changing data models or repository writes.

## Contributing

Contributions are welcome. A good change should:

1. Explain the user problem and intended behavior in an issue or pull request.
2. Keep UI, persistence, parsing, and validation responsibilities separated.
3. Add or update tests for parsing, repository behavior, rules, or UI behavior as appropriate.
4. Run the relevant Gradle checks locally.
5. Avoid unrelated formatting or generated-file changes.
6. Update this README when setup, permissions, supported banks, or user-facing behavior changes.

For pull requests, include the Android version/device tested, whether Firebase emulators were used, and screenshots for meaningful UI changes. Please do not attach real SMS, account, or household data.

## Known limitations

- SMS parsing depends on supported sender formats and Android permission/device behavior.
- SMS ingestion is not available on devices without telephony support.
- Firebase project configuration is required for production authentication and cloud sync.
- The project currently provides local stability diagnostics and guardrails; Firebase Crashlytics and Firebase Cloud Messaging SDKs are not enabled in the current Gradle dependencies.
- There is no license file in this repository yet. Before distributing this project as an open-source package, the maintainers should add a license and document the accepted contribution terms.

## License

No license has been declared yet. Until a license is added, standard copyright restrictions apply and reuse should not be assumed. Add a `LICENSE` file before publishing a public open-source release.
