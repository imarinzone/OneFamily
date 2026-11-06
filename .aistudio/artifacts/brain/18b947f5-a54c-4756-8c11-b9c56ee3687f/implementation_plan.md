# Axio Family — Shared Household Expense & Budget Tracker

An Axio-inspired personal and family expense manager for Android that combines automated on-device bank SMS tracking (powered by the **Cashiro `com.ritesh.parser.core.bank` parser architecture**), multi-account balance insights, and category budget planning with real-time multi-user household synchronization.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions incorporate your confirmed preferences and latest revisions:

- **Confirmed Revision — Cashiro Bank SMS Parser Engine (`com.ritesh.parser.core.bank`)**: On-device SMS parsing will use the modular **Cashiro Bank Parser architecture** (`BankParser` abstract base class, `BankParserFactory` sender/DLT header router, `ParsedTransaction` model, and dedicated bank implementations for HDFC, SBI, ICICI, Axis, Kotak, IDFC First, IndusInd, Yes Bank, PNB, Bank of Baroda, Federal Bank, OneCard, Amex, Paytm/PhonePe/UPI, and generic bank fallbacks).
- **Confirmed Revision — No AI Assistant / AI Budget Features**: All AI budget advisor and generative assistant features are excluded. All SMS parsing, categorization, burn-rate forecasting, and analytics are 100% deterministic, fast, and rule-driven.
- **Confirmed Decision — Household Linking**: Family members can create a household and link accounts using either a **6-character shareable Join Code** or a **direct Email Invite** that automatically links when the invited member signs in with Google.
- **Confirmed Decision — Expense Privacy Model**: Supports **Shared Household + Private Personal Expenses**. Users can toggle any transaction (or default an account/category) between **Shared** (synced in real time to the family household budget and visible to linked members) and **Personal** (strictly private to the individual user in both UI and Firestore security rules).
- **Confirmed Decision — Expense Capture Methods**: Includes an **On-Device SMS Inbox Reader** (`READ_SMS` / `RECEIVE_SMS`) backed by the Cashiro parser engine, paired with **Manual Entry with Custom Tags (`#tags`), Notes, and Categories**, plus a built-in SMS Simulator/Paste parser for testing bank SMS formats on emulators without live carrier SIMs.
- **Confirmed Scope Exclusion**: Strictly omits Axio's personal loan, Buy Now Pay Later (BNPL), peer-to-peer bill-splitting, and any AI assistant modules.

---

### 1. Overview & Core Concept

- **What It Does**:
  - **Cashiro-Powered Automated SMS & Account Tracking**: Uses the `com.ritesh.parser.core.bank` multi-bank parser architecture to inspect DLT sender IDs (e.g., `AD-HDFCBK`, `VM-SBIINB`, `AX-ICICIT`, `JD-AXISBK`, `JM-KOTAKB`) and message bodies, extracting transaction amount, `DEBIT`/`CREDIT`/`TRANSFER`/`INVESTMENT` type, merchant/VPA payee, account/card last-4 digits, updated available balance, credit card limit, and UPI reference numbers—alongside rich manual entry with `#tags` and notes.
  - **Unified Household & Personal Budgeting**: Enables couples and families to link their Google accounts into a shared Household Space. Members see a real-time unified dashboard of household spending against monthly overall and category-level budgets, while keeping personal discretionary expenses private.
  - **Rich Deterministic Analytics**: Provides category breakdown donut charts, daily/monthly spending trend bars, `#tag` spend summaries, per-member household contribution breakdowns, and merchant frequency rankings.
- **Target Audience / Persona**: Individuals, couples, and families who want effortless Axio-style daily expense tracking combined with a single source of truth for shared household budgets (groceries, rent, utilities, dining, travel) without losing personal financial privacy.
- **Key Value**: Eliminates end-of-month spreadsheet reconciliation between partners by synchronizing shared transactions and category budget progress bars instantaneously across linked devices.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Google Sign-In & Household Setup**: Users authenticate via Google Sign-In (`CredentialManager`). On first launch, they can start solo, create a named Household (generating a 6-character code and email invite form), or join a partner's household via code or pending email invitation.
  2. **Unified Home Dashboard (Axio Pulse)**:
     - Top **Scope Switcher Pill** (`Household Shared` | `My Personal` | `Combined View`) dynamically filters all metrics, charts, and feeds.
     - **Monthly Spend & Budget Ring**: Shows total spent this month, remaining budget, daily safe-spend allowance, and a visual burn-rate pace indicator (On Track / Caution / Over Budget).
     - **Accounts & Cards Carousel**: Consolidates Bank Accounts, Credit Cards, UPI, and Cash balances/outflows at a glance, auto-updated from `ParsedTransaction` balance and card limit extractions.
  3. **On-Device Cashiro SMS Sync & Manual Transaction Logging**:
     - Tapping **Sync SMS** requests runtime `READ_SMS` permission, scans transactional bank/UPI alerts through `BankParserFactory`, deduplicates by `smsSignature` hash, maps merchants to categories, updates bank/card balances, and lets the user review or publish items as Shared or Personal.
     - Tapping **Test / Paste Bank SMS** lets users test any raw SMS sender + body (or select from pre-loaded HDFC, SBI, ICICI, Axis, Kotak, and UPI templates) to inspect the live `BankParser` extraction before saving.
     - Tapping **+ Add Expense / Income** opens a bottom sheet with quick amount keypad, Expense/Income toggle, Visibility selector (`Household Shared` vs. `Personal Only`), Category grid, Account/Mode picker, custom `#tags` chips, merchant name, and notes.
  4. **Category Budgets & Visual Progress**:
     - Dedicated **Budgets** tab where household members set overall monthly limits and per-category caps (e.g., Groceries, Rent & Bills, Dining Out, Fuel & Travel, Shopping).
     - Color-coded progress bars (Emerald `<75%`, Amber `75–99%`, Coral `100%+`) with member contribution pills showing who spent what within shared categories.
  5. **Search, Filters & Analytics**:
     - Instant search and multi-filter bar across transactions by keyword, merchant, `#tag`, category, bank/card account, member avatar, or date range.
     - Dedicated **Analytics** tab featuring category breakdown donut charts, daily spend velocity bars, top merchants, `#tag` totals, and member-by-member household split visuals.

- **Visual Identity & Theme**:
  - **Aesthetic Direction**: *Precision Fintech Editorial* — inspired by Axio's crisp financial clarity, pairing deep obsidian-teal surfaces with high-contrast numerals, tactile elevation cards, and semantic budget health accents.
  - **Color Palette & Mood**:
    - **Primary (`#006C51` Light / `#4EE2B8` Dark)**: Deep Emerald Teal representing financial growth, clarity, and verified sync.
    - **Secondary (`#1E293B` / `#94A3B8`)**: Slate Navy for structured account cards, filter chips, and secondary metrics.
    - **Tertiary / Household Accent (`#6366F1` / `#A5B4FC`)**: Indigo badge accent specifically distinguishing **Shared Household** items and partner avatars from personal transactions.
    - **Semantic Budget Accents**: Safe Emerald (`#10B981`), Warning Amber (`#F59E0B`), Over-Budget Coral (`#EF4444`).
    - **Background & Surface Hierarchy**: Warm Alabaster (`#F8FAFC`) in Light mode and Deep Obsidian (`#0B111E`) in Dark mode, with layered `surfaceContainerLow` and `surfaceContainerHigh` cards.
  - **Typography & Hierarchy**:
    - **Display & Numeric Header Font**: *Plus Jakarta Sans* (bundled locally via `font-util`) for crisp geometric currency totals, budget percentages, and dashboard headers.
    - **Body & Transaction Ledger Font**: *Inter* (bundled locally via `font-util`) for high legibility in dense transaction rows, SMS snippets, tags, and filter pills.
  - **Component Styling & Layout**:
    - Edge-to-edge Material 3 layout with `Scaffold`, top scope-switcher bar, and a 5-destination `NavigationBar` (`Dashboard`, `Transactions`, `Budgets`, `Analytics`, `Household`).
    - Custom Canvas budget pace gauge and category breakdown charts that adapt smoothly across portrait and tablet (`BoxWithConstraints`).

- **Interactive Feedback & Motion**:
  - Animated progress bars and spend counters on scope switch (`Shared` <-> `Personal`).
  - Live sync indicator badge pulsing when real-time Firestore snapshots arrive from a family member's device.
  - Clear empty states with one-tap actions (e.g., *"Scan SMS Inbox"*, *"Test Cashiro Bank SMS Parser"*, or *"Invite Family Member"*).

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Cashiro Modular Bank Parser Architecture (`com.ritesh.parser.core.bank`)**
  - *Chosen Approach*: Implement the Cashiro `BankParser` hierarchy and `BankParserFactory` pattern:
    - **Base `BankParser`**: Provides shared extraction utilities (`extractAmount`, `extractTransactionType`, `extractMerchant`, `extractAccountNumber`, `extractBalance`, `extractReference`, `cleanMerchantName`, `isTransactionMessage` filtering out OTPs/promos/payment requests) that specific banks can override with custom regexes.
    - **`BankParserFactory`**: strips DLT prefixes (e.g., `AD-HDFCBK-S` -> `HDFCBK`) and routes messages to specialized bank parsers (`HDFCBankParser`, `SBIBankParser`, `ICICIBankParser`, `AxisBankParser`, `KotakBankParser`, `IDFCFirstBankParser`, `IndusIndBankParser`, `YesBankParser`, `PNBBankParser`, `BankOfBarodaParser`, `FederalBankParser`, `CanaraBankParser`, `UnionBankParser`, `OneCardParser`, `AmexBankParser`, `PaytmBankParser`, `AirtelPaymentsBankParser`, `JioPaymentsBankParser`, and `GenericBankParser` fallback).
  - *Why*: Bank SMS formats vary significantly across Indian and international issuers (e.g., HDFC multi-line UPI vs. SBI `debited by` vs. Axis/ICICI card spend vs. Kotak VPA strings). Using Cashiro's dedicated per-bank parser architecture yields far higher accuracy for merchant names, card vs. account detection, and balance extraction than a single generic regex.
  - *Alternatives Considered*: A single monolithic regex parser was replaced per your direction to use the Cashiro `parser-core` bank parser design.

- **Decision 2: Dual-Collection Privacy Architecture for Shared vs. Personal Data**
  - *Chosen Approach*: Store strictly private transactions and personal budgets under `/users/{userId}/personal_expenses` and `/users/{userId}/personal_budgets`, while storing shared household transactions, shared category budgets, and member rosters under `/households/{householdId}/...` guarded by `request.auth.uid in resource.data.memberUids`.
  - *Why*: Guarantees zero-trust cryptographic isolation at the Firestore Security Rules layer so personal transactions are never readable over the network by other household members, while shared household items stream in real time to all members.
  - *Alternatives Considered*: Storing all transactions in a single collection with an `isPrivate` boolean flag was rejected because Firestore rules cannot safely allow broad household queries without risking accidental exposure.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Edge-to-Edge M3)                        │
│        AuthGate: CredentialManager Google Sign-In -> MainAppShell            │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
         ┌─────────────────────────────┼─────────────────────────────┐
         ▼                             ▼                             ▼
┌─────────────────────┐     ┌─────────────────────┐     ┌──────────────────────┐
│  Dashboard & Ledger │     │ Budgets & Analytics │     │  Household & Accounts│
│  - Scope Switcher   │     │ - Monthly & Cat Cap │     │  - 6-Char Join Code  │
│  - Spend Pace Gauge │     │ - Category Donut    │     │  - Email Invites     │
│  - Accounts Strip   │     │ - Daily Burn Bars   │     │  - Member Roster     │
│  - Search & #Tags   │     │ - Member Breakdown  │     │  - Bank/Card/UPI     │
│  - SMS Inbox Sync   │     │ - Top Merchants/Tags│     │    Balance Tracker   │
└──────────┬──────────┘     └──────────┬──────────┘     └──────────┬───────────┘
           │                           │                           │
           └───────────────────────────┼───────────────────────────┘
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     Expense & Household ViewModel Layer                      │
│  - Combines Household Shared Flows + Personal Private Flows                  │
│  - Computes Category Progress, Daily Safe-Spend, Account Balances, #Tag Sums │
│  - Orchestrates READ_SMS Sync & Deduplication via Cashiro BankParserFactory  │
└───────────────────┬──────────────────────────────────┬───────────────────────┘
                    │                                  │
                    ▼                                  ▼
┌──────────────────────────────────────┐  ┌────────────────────────────────────┐
│     FirestoreHouseholdRepository     │  │  Cashiro Bank Parser Engine        │
│  Custom DB: firestore_database_id    │  │  (com.ritesh.parser.core.bank)     │
│  - /users/{uid} (Profile & Config)   │  │  - BankParserFactory.getParser()   │
│  - /users/{uid}/personal_expenses    │  │  - BankParser (abstract base)      │
│  - /users/{uid}/personal_budgets     │  │  - HDFCBankParser, SBIBankParser,  │
│  - /users/{uid}/accounts             │  │    ICICIBankParser, AxisBankParser,│
│  - /households/{hid} (Members/Limit) │  │    KotakBankParser, IDFCFirst,     │
│  - /households/{hid}/shared_expenses │  │    IndusInd, Yes, BoB, PNB, Federal│
│  - /households/{hid}/category_budgets│  │    OneCard, Amex, GenericUpiParser │
│  - /household_invites/{inviteId}     │  │  - Output: ParsedTransaction       │
└──────────────────────────────────────┘  └────────────────────────────────────┘
```

- **Data Model & State**:
  - **Cashiro `ParsedTransaction` (`com.ritesh.parser.core`)**: `amount` (`BigDecimal`), `type` (`TransactionType`: `INCOME`, `EXPENSE`, `TRANSFER`, `INVESTMENT`, `CREDIT`), `merchant`, `reference`, `accountNumber` (last 4 digits), `balance` (`BigDecimal?`), `creditLimit` (`BigDecimal?`), `smsBody`, `sender`, `timestamp`, `bankName`, `isFromCard`.
  - **UserProfile (`/users/{userId}`)**: `uid`, `email`, `displayName`, `photoUrl`, `activeHouseholdId`, `personalMonthlyBudget`, `currencySymbol`, `createdAt`, `updatedAt`.
  - **Household (`/households/{householdId}`)**: `id`, `name`, `ownerUid`, `joinCode` (indexed 6-char uppercase code), `memberUids` (list of UIDs), `memberProfiles` (map of UID to name/email), `monthlyBudgetLimit`, `createdAt`, `updatedAt`.
  - **HouseholdInvite (`/household_invites/{inviteId}`)**: `id`, `householdId`, `householdName`, `joinCode`, `inviterUid`, `inviterName`, `inviteeEmail` (lowercased), `status` (`PENDING`, `ACCEPTED`), `createdAt`, `updatedAt`.
  - **ExpenseTransaction (`/users/{userId}/personal_expenses/{txId}` & `/households/{householdId}/shared_expenses/{txId}`)**: `id`, `userId`, `userName`, `householdId`, `title` (merchant/payee), `amount`, `type` (`EXPENSE`, `INCOME`), `category` (Food, Groceries, Rent, Utilities, Shopping, Travel, Health, Entertainment, Investment, Transfer, Other), `accountName` (e.g., `HDFC Bank ••4821`, `SBI Card ••9012`, `UPI`, `Cash`), `paymentMode` (`UPI`, `BANK`, `CARD`, `WALLET`, `CASH`), `tags` (list of `#tags`), `notes`, `visibility` (`SHARED`, `PERSONAL`), `smsSignature` (hash for SMS deduplication), `referenceNumber`, `transactionDateMillis`, `createdAt`, `updatedAt`.
  - **CategoryBudget (`/households/{householdId}/category_budgets/{catId}` & `/users/{userId}/personal_budgets/{catId}`)**: `id`, `category`, `monthlyLimit`, `alertThresholdPercent` (default 80), `userId`, `householdId`, `memberUids`, `createdAt`, `updatedAt`.
  - **FinancialAccount (`/users/{userId}/accounts/{accId}`)**: `id`, `userId`, `name`, `bankName`, `accountType` (`BANK`, `CREDIT_CARD`, `WALLET`, `CASH`), `lastFourDigits`, `balance`, `creditLimit`, `isSharedVisible`, `createdAt`, `updatedAt`.

- **Interactive Component & State Mapping**:
  - **Scope Switcher (`Household Shared` | `My Personal` | `Combined`)**: Updates `selectedScope` in `ExpenseViewModel`, dynamically recomputing total spend, category progress bars, account summaries, and transaction list items via `combine()` over real-time Firestore `.snapshots()` flows.
  - **Transaction Creation / Privacy Flip**: Submitting the Add/Edit Expense bottom sheet validates fields (`amount > 0`, non-blank merchant/category), writes to `/households/{hid}/shared_expenses` when `SHARED` is selected (or `/users/{uid}/personal_expenses` when `PERSONAL` is selected), and supports one-tap moving between Personal and Shared scopes.
  - **Household Join & Email Linking**: Entering a 6-character code or accepting a matching email invite atomically adds the user's UID to the household's `memberUids`, updates the user's `activeHouseholdId`, and immediately begins streaming all shared expenses and budgets.
  - **Cashiro SMS Sync & Deterministic Parser**: Requests `Manifest.permission.READ_SMS` via `rememberLauncherForActivityResult`. Reads SMS messages (or user-submitted SMS text from the built-in SMS Tester), routes each `(sender, body, timestamp)` through `BankParserFactory.getParser(sender)` (with fallback to body-based bank detection), converts each `ParsedTransaction` into an `ExpenseTransaction` + automatic `FinancialAccount` balance/card-limit update, filters out already-imported `smsSignature` hashes, and presents a batch review sheet where each item can be tagged `Shared` or `Personal` before saving.
