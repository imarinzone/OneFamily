# One Family — Smart Personal & Family Expense Tracker

**One Family** is a modern Android personal and family expense management application built with **Kotlin** and **Jetpack Compose (Material 3)**. It automatically detects and imports bank/UPI SMS transactions, supports **swiping right on any transaction to link duplicate SMS/transactions**, renders a **WhatsApp-style tiled finance icon & emoji wallpaper** that adapts to the app theme and is fully customizable in Settings, integrates **Firebase Cloud Messaging (FCM)** push notifications for shared family transactions and budget thresholds, enforces **Firebase Crashlytics & Stability Guardrails** for SMS auto-sync and responsive typography, and includes a 1-tap **"Login as Mock User"** pre-loaded with 3 months of realistic data.

---

## Key Features & Functionality

### 1. Swipe Right to Link Duplicate SMS / Transactions
- **Swipe-Right Gesture & Quick Link Button**:
  - Swiping right (`StartToEnd`) on any transaction row in the **Home** or **Analysis** tab reveals a **"Link Duplicate"** banner and opens the **Link Duplicate SMS / Transaction** dialog (`LinkDuplicateTransactionDialog`).
  - You can also tap the Link icon directly on any transaction card or inside the **Edit Transaction** sheet (`Link Duplicate SMS / Tx`).
- **Smart Duplicate Candidate Ranking**:
  - Automatically ranks candidate transactions in your ledger by matching amount and timestamp proximity (`SAME AMOUNT + NEAR TIME`), making it effortless to merge a duplicate UPI app alert and a bank debit SMS (or a manual entry and an incoming SMS) into a single ledger entry.
  - Allows choosing which transaction stays as the primary ledger entry while merging the duplicate's SMS signature (`linkedSmsSignatures`), notes, and tags.
- **Multi-SMS Viewer & Deduplication Protection**:
  - Linked transactions display a `"Linked (N SMS)"` badge on the ledger card, and opening **Edit Transaction** shows all linked original SMS messages with their respective senders and timestamps.
  - Future SMS scans automatically recognize all linked duplicate SMS signatures so merged duplicates are never re-imported.

### 2. WhatsApp-Style Finance Emoji & Icon Tiled Wallpaper Background
- **Theme-Adaptive Tiled Finance Doodle Canvas (`AppDecoratedBackground`)**:
  - Replaces static header images/emoji pills with a full-screen, staggered repeating **WhatsApp-style finance wallpaper** across all screens (`Auth`, `Home`, `Analysis`, `Budgets`, `Settings`, and `SMS Sync`).
  - Draws crisp vector line-art finance doodles (credit cards, zigzag receipts, currency coins, bar charts, shopping bags, wallets, family homes, price tags, donut charts, and trend arrows) interwoven with finance emojis/icons.
  - Automatically adapts its base canvas and doodle stroke tints when switching between **Light**, **AMOLED Dark** (`#000000`), and **System** themes.
- **Full Wallpaper Customization in Settings (`Settings` -> `App Theme & Finance Wallpaper`)**:
  - **On / Off Switch**: Toggle the finance wallpaper background on or off at any time.
  - **Live Wallpaper Preview Banner**: Preview changes in real time right inside Settings.
  - **Pattern Style**: Choose between **Icons + Emojis**, **Finance Doodles** (pure vector line art), or **Minimal Grid**.
  - **Color Accent**: Choose **Theme Default**, **Emerald Mint**, **Royal Gold**, **Ocean Cyan**, or **Rose Coral**.
  - **Pattern Density**: Switch between **Airy**, **Balanced**, and **Dense** tile spacing.
  - **Pattern Intensity / Opacity Slider**: Fine-tune watermark subtlety from `40%` to `200%`.

### 3. Firebase Cloud Messaging (FCM) — Family & Budget Push Notifications
- **Instant Family Transaction Push Alerts (`CHANNEL_ID_FCM_FAMILY`)**:
  - Sends instant high-priority push notifications when a shared household transaction is logged, keeping every family member informed in real time.
- **Shared Category & Household Budget Threshold Push Alerts (`CHANNEL_ID_FCM_BUDGET`)**:
  - Automatically evaluates category and household budget utilization whenever expenses or budgets change, dispatching an instant FCM push notification when a category budget reaches its alert threshold (e.g., `80%`) or exceeds `100%`.
- **FCM Push Controls in Settings (`Settings` -> `Firebase Cloud Messaging (FCM)`)**:
  - Toggle **Family Member Transaction Alerts** and **Category Budget Threshold Alerts** independently.
  - View the active FCM Household Topic subscription (`household_<id>`) and trigger a live **"Send Test FCM Budget Push Alert"** to verify push delivery on the device.

### 4. Firebase Crashlytics — Real-Time Stability & Crash Guardrails
- **Uncaught Exception Handler & Non-Fatal Breadcrumb Logger**:
  - Tracks and logs stability events across device configurations and persists diagnostic breadcrumbs in `LocalAccountSessionManager.stabilityDiagnostics`.
- **SMS Auto-Sync Guardrails**:
  - Wraps cursor iteration, per-row SMS regex parsing, and batch persistence in `SmsInboxReader` and `SmsNotificationSystem` with defensive try/catch telemetry so malformed carrier messages or locked databases never crash the app.
- **Responsive Typography & Screen Scaling Guardrails**:
  - Clamps extreme device screen dimensions and system font scales (`0.78f..1.35f`) in `Theme.kt` (`guardedTypographyScale`) so compact phones, foldables, and large accessibility font sizes render cleanly without layout overflow.
- **Stability Dashboard in Settings (`Settings` -> `Firebase Crashlytics & Stability`)**:
  - Toggle **Crashlytics Real-Time Reporting**, **SMS Auto-Sync Crash Guardrails**, and **Responsive Typography Guardrails**, inspect live diagnostic logs, and run **"Verify Stability"** on demand.

### 5. "Login as Mock User" with 3-Month Realistic Mock Data
- **1-Tap Quick Mock Login**:
  - Tapping **"Login as Mock User"** (`test` / `pass`) logs into a dedicated mock user account pre-seeded with 3+ months of realistic **Family Shared** and **Personal** transactions, raw bank/UPI SMS messages (including a ready-to-link duplicate UPI alert for Nature's Basket), and 4 financial accounts (`HDFC Salary A/c`, `ICICI Savings A/c`, `Axis Neo Credit Card`, and `Cash Pocket`).

### 6. First-Time SMS Sync Setup, Category Budgets & Comprehensive Validation
- **Dedicated First-Time SMS Sync Screen**:
  - Shows a live animated progress bar (`0%` – `100%`) with **SMS Read**, **Remaining**, **Total SMS**, and **Transactions Synced** counters using paced background batches (`Dispatchers.IO`).
- **Category-Based & Overall Monthly Budgets**:
  - Set, edit, or clear monthly limits and alert thresholds for every expense category on the **Budgets** tab.
- **Contextual Top Gesture Date Bar (`Home` & `Analysis` Only)**:
  - Displays the **Year** on top and **Month Name** below with a full-bar directional slide + fade swap animation when swiping or tapping Previous/Next month.
- **Inline Input Validation Across All Forms**:
  - Real-time numeric sanitization and descriptive error messages across all budget, transaction, account, household, and authentication dialogs.

---

## Testing & Verification

Run the Robolectric unit test suite with:

```bash
gradle :app:testDebugUnitTest
```
