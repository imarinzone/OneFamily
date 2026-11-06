const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read personal expenses or households", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("personal_expenses").get()
  );
  await assertFails(unauthDb.collection("households").get());
});

test("Personal privacy: Bob cannot read Alice's personal expenses even if in same household", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("personal_expenses")
      .doc("exp_1")
      .set({
        userId: ALICE_UID,
        userName: "Alice",
        title: "Personal Gift",
        amount: 1200,
        type: "EXPENSE",
        category: "Shopping",
        accountName: "HDFC Bank ••4821",
        paymentMode: "UPI",
        visibility: "PERSONAL",
        transactionDateMillis: 1730000000000,
        createdAt: new Date(1730000000000),
        updatedAt: new Date(1730000000000),
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("personal_expenses").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("personal_expenses").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("personal_expenses")
      .doc("exp_1")
      .get()
  );
});

test("Household shared expenses: Alice and Bob in same household can query shared expenses, Charlie cannot", async () => {
  const householdId = "house_ab";
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("households").doc(householdId).set({
      name: "Sharma Family",
      ownerUid: ALICE_UID,
      joinCode: "AXIO99",
      memberUids: [ALICE_UID, BOB_UID],
      monthlyBudgetLimit: 75000,
      currencySymbol: "₹",
      createdAt: new Date(1730000000000),
      updatedAt: new Date(1730000000000),
    });
    await db
      .collection("households")
      .doc(householdId)
      .collection("shared_expenses")
      .doc("shared_1")
      .set({
        userId: ALICE_UID,
        userName: "Alice",
        householdId: householdId,
        title: "BigBasket Groceries",
        amount: 2450,
        type: "EXPENSE",
        category: "Groceries",
        accountName: "ICICI Card ••9012",
        paymentMode: "CARD",
        visibility: "SHARED",
        transactionDateMillis: 1730000000000,
        memberUids: [ALICE_UID, BOB_UID],
        createdAt: new Date(1730000000000),
        updatedAt: new Date(1730000000000),
      });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  await assertSucceeds(
    bobDb
      .collection("households")
      .doc(householdId)
      .collection("shared_expenses")
      .where("memberUids", "array-contains", BOB_UID)
      .get()
  );

  // Unfiltered list or querying another member's array-contains must fail
  await assertFails(
    charlieDb
      .collection("households")
      .doc(householdId)
      .collection("shared_expenses")
      .get()
  );

  await assertFails(
    charlieDb
      .collection("households")
      .doc(householdId)
      .collection("shared_expenses")
      .where("memberUids", "array-contains", ALICE_UID)
      .get()
  );

  await assertFails(
    charlieDb
      .collection("households")
      .doc(householdId)
      .collection("shared_expenses")
      .doc("shared_1")
      .get()
  );
});

test("Shadow update test: rejecting unexpected ghost fields on UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      displayName: "Alice",
      personalMonthlyBudget: 30000,
      currencySymbol: "₹",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
