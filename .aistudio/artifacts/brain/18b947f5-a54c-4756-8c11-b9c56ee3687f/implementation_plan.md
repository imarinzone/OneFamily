# Full-App Subtext & Icon Label Removal Plan

## Goal
Perform a complete, screen-by-screen cleanup across the entire application to remove the app name from the top bar, remove all text labels under icons, and eliminate secondary subtext/descriptions so every row and card is clean and single-line.

---

## 1. Top Bar & Bottom Navigation (`MainActivity.kt`)
- **Minimal Icon-Only Top Bar**:
  - Remove `"One Family"` and the household badge from `TopAppBar(title = {})`, leaving a clean top bar containing only the action icon buttons (`Sync SMS`, `Theme`, and `Sign Out`).
- **Icon-Only Bottom Navigation Bar**:
  - Remove `label = { Text(tab.label) }` and set `alwaysShowLabel = false` on all `NavigationBarItem` entries (`Home`, `Receipts`, `Budgets`, `Charts`, `Family`) so icons never have text underneath them.

---

## 2. Sign-In & Sign-Up Screen (`AuthScreen.kt`)
- **Remove Header Title & Subtitle**:
  - Remove the `"One Family"` heading and `"Simple receipts for your household & personal spending"` subtext under the top illustration.
- **Single-Line Quick Demo Pill**:
  - Remove the second-line subtext (`"Instant family home demo without signup"`) from the `test / pass` quick-login button so it renders as a single crisp line.
- **Remove Receipt Footer Subtext**:
  - Remove the `"HOUSEHOLD • PERSONAL"` footer label at the bottom of the login receipt card.

---

## 3. Shared Visual Components & Rows (`AxioVisualComponents.kt`)
- **Single-Line Transaction Row (`ExpenseTransactionItemCard`)**:
  - Remove the second line (the stacked category pill below the merchant title).
  - Display **only** the Category Icon (tappable for quick category change), Merchant Name (`expense.title`), and Amount (`+/-₹...`) on a single horizontal line.
- **Streamlined Hero Budget Ring Card (`MonthlyBudgetPulseCard`)**:
  - Remove the stacked `periodLabel` caption and `"Budget ₹..."` subtext around the main spent amount; keep the total spent, an edit icon button, and the animated percentage ring.
  - Convert the bottom summary pills into single-line badges without stacked sub-labels.
- **Single-Line Account Pills (`AccountSummaryCard`)**:
  - Format each bank/card chip as a single horizontal row (Icon + Name + Balance) with no second line of text under the account name.

---

## 4. Home, Receipts, Budgets, Charts & Family Screens (`DashboardAndLedgerScreens.kt` & `BudgetsAnalyticsHouseholdScreens.kt`)
- **Home & Receipts Empty State (`EmptyLedgerPlaceholder`)**:
  - Remove the caption text stacked directly underneath the receipt icon, leaving only the icon and action buttons.
- **Budgets Tab (`BudgetsTabScreen`)**:
  - **Monthly Budgets Card**: Format Household and Personal budget pills as single-line items without stacked sub-labels.
  - **Category Budget Rows**: Remove the second line of text (`"₹spent / ₹limit"`) under the category name so each row is a single line with Category Icon, Category Name, and Amount / Percentage on the right (plus the progress bar when a limit is set).
- **Family Tab (`HouseholdTabScreen`)**:
  - **Appearance Card**: Remove `"Follow Material 3 System, Light, or Dark mode"` subtext under `"Appearance"`.
  - **Active Household Receipt**: Remove `"₹75,000 • 1 Member(s)"` subtext under the household title, and remove the `"Family Join Code"` sub-label above the join code so the code and copy button sit on one clean line.
  - **Invitations Card**: Remove `"Code: ..."` subtext under the household name.

---

## 5. Interactive Receipt & Modal Dialogs (`ModalsAndSheets.kt`)
- **Interactive Receipt Sheet (`InteractiveReceiptSheet`)**:
  - Remove `"NEW RECEIPT"` / `"EXPENSE RECEIPT"` (`headerBadgeText`) and date subtext stacked underneath the top category icon.
  - Remove the `"* * * ONE FAMILY RECEIPT * * *"` footer subtext at the bottom of the receipt.
- **Theme Settings Dialog (`ThemeSettingsDialog`)**:
  - Remove `"Choose Light, Dark, or follow your phone's System theme"` subtext under `"App Theme"`.
