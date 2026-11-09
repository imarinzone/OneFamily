# Test User Mock Data Restoration & Real User Data Isolation Plan

## Summary of Fixes & Architecture

1. **Test User Mock Data Restoration (`test` / `pass`, `DEFAULT_TEST_UID`)**:
   - Restored realistic mock transactions across past months (salary, rent, groceries, dining, coffee, fuel, shopping, utilities, health) when logging in as the demo Test User.
   - Preserved default financial accounts (HDFC Salary, ICICI Savings, Axis Neo Card, Cash Pocket).
   - Cached realistic raw bank SMS bodies for every mock transaction so tapping/editing any mock transaction displays the raw SMS alert in the Edit dialog.

2. **Strict User Data Isolation**:
   - Real users (Google Sign-In or registered local users) start with a clean slate: strictly ZERO mock transactions and ZERO inherited accounts.
   - `LocalAccountDataStore.observePersonalExpenses` and `observeSharedExpenses` strictly filter by `userId` and `householdId`. Blank household IDs return `emptyList()`.
   - `OneFamilyViewModel.coreDataFlow` strictly excludes `DEFAULT_TEST_UID` and `DEFAULT_TEST_HOUSEHOLD_ID` data from real user sessions.

3. **Elimination of Duplicate Data During Real User SMS Sync**:
   - In `SmsInboxReader.scanDeviceSmsInboxWithProgress`, rows from the telephony provider are uniquely fingerprinted by `(sender, timestamp, body)` before chunking into parallel batches.
   - Single URI query discipline: once `Telephony.Sms.Inbox` yields rows, `Telephony.Sms` is not redundantly re-queried.
   - Concurrent in-flight signatures set (`inFlightSignatures`) in `OneFamilyRepository` and `LocalAccountDataStore` prevents concurrent reader and writer threads from double-inserting the same transaction.
   - `SmsInboxContentObserver` initializes `lastProcessedSmsDateMillis` to the observer attachment timestamp, preventing it from racing with `scanDeviceSmsInbox` on setup.
   - Document IDs for SMS imports are deterministically keyed (`sms_${signature}`), preventing multiple documents for the same transaction.

