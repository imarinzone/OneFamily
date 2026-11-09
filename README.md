# One Family — Smart Personal & Family Expense Tracker

**One Family** is a modern Android personal and family expense management application built with **Kotlin** and **Jetpack Compose (Material 3)**. It automatically detects and imports bank/UPI SMS transactions, supports personal and shared family household ledgers, provides interactive category and payment-mode analytics, and adapts dynamically to every device's screen size.

---

## Key Features & Functionality

### 1. Top Month & Year Navigation Header & Gesture Controls
- **Clean Top Header Bar**:
  - **Left**: Previous month button (`< MMM`) for one-tap backward navigation.
  - **Middle**: Current selected **Month & Year** pill (or active custom date range badge with one-tap reset).
  - **Right**: Next month button (`MMM >`) for one-tap forward navigation.
  - All extra clutter buttons have been removed from the top bar.
- **Full-Screen Horizontal Swipe Gestures**:
  - **Swipe Left / Right near the top bar** to smoothly scroll between previous and next months.
  - **Swipe Left / Right on the main screen body** to smoothly switch between the 4 main navigation tabs (`Home`, `Analysis`, `Budgets`, `Settings`).

### 2. Home Screen (`Home`)
- **Personal & Family Scope Switcher (Exclusive to Home Screen)**:
  - Switch between **Family Shared** and **Personal** ledgers right from the top of the Home screen.
- **Floating Add (`+`) Button (Exclusive to Home Screen)**:
  - The primary `+` Floating Action Button in the floating bottom navigation bar appears only on the Home screen to log new expenses or income.
- **Monthly Budget Pulse Banner**:
  - Shows total spent, total income, net flow, and personal vs. shared breakdown for the selected month.
  - Displays **"Unassigned"** when a monthly budget has not yet been set, and prompts to assign a budget or auto-scales when category budgets are configured.
- **Search, Date Range & Category Filtering**:
  - Search transactions by merchant title, category, account, payment mode, or tag.
  - Filter by **Selected Month**, **Last 7 Days**, **Last 30 Days**, **Last 90 Days**, **All Time**, or pick a **Custom Date Range** (`Start Date` to `End Date`).
- **Transaction List with Exact Timestamps**:
  - Every transaction displays its exact date and time (`dd MMM yyyy, hh:mm a`) — no vague `"Today"`, `"Yesterday"`, or `"Day -1"` labels.
  - Tap any transaction card (or its edit button) to open the **Edit Transaction** modal.

### 3. Exact Timestamp Storage & Original SMS Viewer while Editing
- **Millisecond-Accurate Date + Time Storage**:
  - Both new and edited transactions store and preserve the full epoch millisecond timestamp (`transactionDateMillis`), including hours, minutes, and seconds.
  - `AddTransactionDialog` and `EditTransactionDialog` provide separate **Date (`dd MMM yyyy`)** and **Time (`hh:mm a`)** pickers so changing the date never resets the time-of-day.
- **Original Bank / UPI SMS Viewer in Edit Transaction Dialog**:
  - When editing any SMS-imported transaction, the **Original SMS** box automatically displays the sender address (e.g., `VM-HDFCBK`), the exact SMS timestamp (`dd MMM yyyy, hh:mm:ss a`), and the complete raw SMS message body.
- **Complete Transaction Editing**:
  - Edit merchant/title, amount, transaction type (`EXPENSE` / `INCOME`), exact date & time, category, bank/card/wallet account, payment mode (`UPI`, `Card`, `Bank Transfer`, `Cash`, `Wallet`), tags, notes, and `Family Shared` vs. `Personal Only` visibility.

### 4. Interactive Analysis Dashboard (`Analysis`)
- **Interactive Category Donut Chart**:
  - Tap any slice on the donut chart or any category chip to select or inspect a category.
- **Expandable Category Breakdown with Full Transaction List & Editing**:
  - Shows all categories with their spend amount, percentage of total spend, transaction count, and optional category budget progress.
  - **Tap any category card** to expand and view **all transactions for that category** during the active period.
  - **Tap any transaction inside the category breakdown** (or its edit icon) to open the full `EditTransactionDialog` and edit any field of the transaction (not just its category).
- **Daily Spend Velocity Bar Chart**:
  - Visualizes daily spending across the month with an optional daily budget target line.
- **Payment Method & Account Breakdown**:
  - Summarizes spending across UPI, Credit/Debit Cards, Bank Transfers, Wallets, and Cash.

### 5. Category & Monthly Budgets (`Budgets`)
- **Zero-Default Unassigned Budgets**:
  - Overall and category budgets remain **Unassigned (`0.0`)** until explicitly configured by the user.
- **Overall & Per-Category Limits**:
  - Set or clear the overall monthly household budget or individual limits for `Groceries`, `Food & Dining`, `Rent & Housing`, `Utilities & Bills`, `Transport & Fuel`, `Shopping`, `Healthcare`, `Entertainment`, `Education`, and `Travel`.

### 6. Settings, Profile, SMS Sync & Household (`Settings`)
- **Edit User Name**:
  - Update your display name directly from the **User Profile** card (`Your Name` input + `Save` button). Changes immediately update your session, profile, and family household member list.
- **SMS Inbox Auto-Scan (`Sync SMS`)**:
  - Dedicated **Sync SMS** button with a Sync icon (`Icons.Default.Sync`) in the Settings screen to scan device bank/UPI SMS messages and import new transactions.
  - Also supports real-time incoming SMS detection (`RECEIVE_SMS`) with instant high-priority notification alerts.
- **Appearance & Full AMOLED Black Theme**:
  - Choose between **System**, **Light**, and **AMOLED Dark** (`#000000` pure black background and surfaces for OLED power efficiency).
- **Bank Accounts, Cards & Wallets**:
  - Add or remove custom bank accounts, credit/debit cards, and wallets.
- **Family Household Sharing**:
  - Create a family household, share the 6-character invite code, or join an existing family household code.
- **Logout Section**:
  - Dedicated **Sign Out of Account** card at the bottom of the Settings screen.

### 7. Relative Screen Sizing & Google Default Typography
- **Screen-Relative Responsive Layout (`LocalAppDimens` & `buildResponsiveTypography`)**:
  - All typography, padding, card radii, icon sizes, chart dimensions, and floating navigation bar heights scale dynamically as percentages of the device's actual screen width and height (`screenWidthDp` / `screenHeightDp`) rather than fixed constant sizes.
- **Google Default Fonts**:
  - Uses Android's native Google default system font family (`FontFamily.Default` / Roboto) across all text styles.

---

## Testing & Verification

Run the Robolectric unit test suite with:

```bash
gradle :app:testDebugUnitTest
```
