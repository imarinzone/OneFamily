# One Family — Smart Personal & Family Expense Tracker

**One Family** is a modern Android personal and family expense management application built with **Kotlin** and **Jetpack Compose (Material 3)**. It automatically detects and imports bank/UPI SMS transactions, provides a dedicated first-time SMS sync setup screen with live progress tracking and paced non-blocking background processing, supports personal and shared family household ledgers, enforces comprehensive input validation across all forms, offers category-based and overall budgeting, sends instant notifications when new SMS transactions arrive, and adapts dynamically to every device's screen size.

---

## Key Features & Functionality

### 1. First-Time SMS Sync Setup with Live Progress Bar & Non-Blocking Processing
- **Dedicated First-Time SMS Sync Prompt**:
  - When setting up an account for the first time (or when manually triggering **Sync SMS** from Settings or Home), the app presents the dedicated **Sync Bank & UPI SMS** screen and stays on that page until all SMS messages are processed.
- **Live Progress Bar & Detailed Counters**:
  - Shows a smooth animated progress bar (`0%` – `100%`) alongside live metrics for **SMS Read**, **Remaining**, **Total SMS**, and **Transactions Synced**.
- **Paced Background Processing (No App Hangs or Crashes)**:
  - Runs entirely on background coroutines (`Dispatchers.IO`) with paced batch intervals (`65ms` per batch of 10 SMS messages read and `110ms` per batch of transactions saved) and batched disk persistence so the UI stays 60fps responsive and never hangs or crashes on devices with large SMS inboxes.

### 2. Real-Time Incoming SMS Transaction Parsing & Notifications
- **Dual Real-Time Detection (`BroadcastReceiver` + `ContentObserver`)**:
  - Listens for `android.provider.Telephony.SMS_RECEIVED` broadcasts via `IncomingSmsBroadcastReceiver` (even when the app is in the background) and observes `content://sms` via `SmsInboxContentObserver` while the app is open.
- **Instant High-Priority Transaction Notifications**:
  - Whenever a new bank or UPI SMS arrives and is parsed into a transaction, a high-priority system notification is posted showing the debit/credit amount, merchant/category, and payment mode. Tapping the notification opens the app directly into the **Edit Transaction** sheet for that transaction.

### 3. Category-Based & Overall Monthly Budgets (`Budgets`)
- **Set Budget per Category**:
  - Every expense category (`Groceries`, `Food & Dining`, `Rent & Housing`, `Utilities & Bills`, `Transport & Fuel`, `Shopping`, `Healthcare`, `Entertainment`, `Education`, `Travel`, plus any custom category) is listed on the **Budgets** tab with its current monthly spend, assigned category budget limit, utilization progress bar, alert threshold indicator, and a **Set Budget** / **Edit** button.
  - Supports creating custom category budgets and clearing any category limit (`Clear Limit`).
- **Overall Monthly Budget & Auto-Scaling**:
  - Supports setting or clearing both Personal and Family Household monthly budget limits, with automatic upward scaling when combined category budgets exceed the overall limit.

### 4. Comprehensive Input Validation Across All Forms
- **Inline Validation & Sanitization**:
  - Every text input field across the app includes real-time input sanitization, `isError` highlighting, and descriptive `supportingText` error messages:
    - **Overall & Category Budget Dialogs**: Validates numeric decimal formatting, non-negative/positive limits, maximum amount bounds, custom category names (`2–40` characters), and currency symbols (`1–5` characters).
    - **Add & Edit Transaction Sheet**: Validates required title/merchant (`1–80` characters), positive numeric amount, custom category name, custom account name, tags length, and notes length.
    - **Add Financial Account Dialog**: Validates account nickname (`2–50` characters), bank/issuer name (`2–50` characters), exact 4-digit card/account suffix, numeric current balance, and credit limit.
    - **Settings & Household Sharing**: Validates user display name (`2–50` characters), household name (`2–50` characters), optional monthly budget, 6-character alphanumeric invite code, and email address format.
    - **Authentication Screen**: Validates username, password, and display name length and required fields.

### 5. Contextual Top Gesture Date Bar & Full-Bar Swap Animation
- **Shown Only on Applicable Screens (`Home` & `Analysis`)**:
  - Automatically appears on date-driven screens (`Home` and `Analysis`) and hides on configuration screens (`Budgets` and `Settings`).
- **Stacked Year-on-Top & Month-Below Gesture Bar**:
  - **Left**: Previous month's **Year** on top and `< MMM` below.
  - **Middle**: Active **Year** on top and full **Month Name** (`MMMM`, e.g., `November`) below, with a bottom gesture handle pill.
  - **Right**: Next month's **Year** on top and `MMM >` below.
- **Full-Bar Directional Swapping Animation**:
  - Swiping horizontally or tapping Previous/Next animates and swaps the **entire bar** (Left, Center, and Right together) with directional horizontal slide + fade transitions and live finger-tracking drag offset.

### 6. Home Screen, Exact Timestamps & Original SMS Viewer
- **Personal & Family Scope Switcher & Floating Add (`+`) Button (Exclusive to Home Screen)**:
  - Switch between **Family Shared** and **Personal** ledgers and tap `+` to add transactions directly from the Home screen.
- **Millisecond-Accurate Date + Time Storage & Original SMS Viewer**:
  - Transactions store and display exact timestamps (`dd MMM yyyy, hh:mm a`).
  - Editing any SMS-imported transaction displays the **Original SMS** box with sender address, exact SMS timestamp, and full raw SMS body.

### 7. Interactive Analysis Dashboard (`Analysis`)
- **Interactive Category Donut Chart & Expandable Category Transactions**:
  - Tap any category to inspect all transactions in that category and tap any transaction to open the full `EditTransactionDialog`.

### 8. Relative Screen Sizing, Google Default Typography & Full AMOLED Black Theme
- **Screen-Relative Responsive Layout (`LocalAppDimens` & `buildResponsiveTypography`)**:
  - Scales all text, padding, radii, and component dimensions dynamically relative to each phone's screen width and height using Google's default font family (`FontFamily.Default`).
- **Full AMOLED Black Theme**:
  - Supports **System**, **Light**, and **AMOLED Dark** (`#000000` pure black background and surfaces).

---

## Testing & Verification

Run the Robolectric unit test suite with:

```bash
gradle :app:testDebugUnitTest
```
