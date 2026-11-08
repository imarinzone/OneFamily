package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LocalAccountSessionManager
import com.example.data.repository.AxioRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("One Family", appName)
  }

  @Test
  fun `test user login with test and pass succeeds and persists transactions`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val loginResult = LocalAccountSessionManager.signInWithUsernamePassword(
      context = context,
      rawUsername = "test",
      rawPassword = "pass"
    )
    assertTrue(loginResult.isSuccess)
    val user = loginResult.getOrNull()
    assertNotNull(user)
    assertEquals("test", user?.username)

    val repo = AxioRepository(context)
    val profile = repo.ensureUserProfile(user!!.displayName, user.email).getOrNull()
    assertNotNull(profile)
    val household = repo.getHouseholdOnce(profile!!.activeHouseholdId)
    assertNotNull(household)

    val txId = repo.addExpense(
      userName = user.displayName,
      household = household,
      title = "Morning Coffee",
      amount = 180.0,
      type = "EXPENSE",
      category = "Food & Dining",
      accountName = "Cash",
      paymentMode = "UPI",
      tags = listOf("coffee"),
      notes = "Test receipt",
      isShared = true
    ).getOrNull()
    assertNotNull(txId)

    val sharedExpenses = repo.observeSharedExpenses(household!!.id, user.uid).first()
    assertTrue(sharedExpenses.any { it.title == "Morning Coffee" && it.amount == 180.0 })

    LocalAccountSessionManager.signOut(context)
  }
}
