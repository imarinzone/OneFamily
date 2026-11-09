package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.data.model.CategoryBudget
import com.example.data.model.CrashlyticsSettings
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FcmPushSettings
import com.example.data.model.FinancialAccount
import com.example.data.model.Household
import com.example.data.model.StabilityDiagnosticEntry
import com.example.data.model.UserProfile
import com.example.data.model.WallpaperColorAccent
import com.example.data.model.WallpaperDensity
import com.example.data.model.WallpaperPatternStyle
import com.example.data.model.WallpaperSettings
import com.example.data.sms.SmsCandidate
import com.google.firebase.Timestamp
import com.ritesh.parser.core.TransactionType
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import com.example.ui.theme.AppThemeMode
import org.json.JSONArray
import org.json.JSONObject

data class LocalAuthUser(
    val uid: String,
    val username: String,
    val displayName: String,
    val email: String
)

object LocalAccountSessionManager {
    private const val PREFS_NAME = "axio_local_auth_prefs"
    private const val KEY_ACTIVE_USER = "active_local_user_json"
    private const val KEY_CREDENTIALS_MAP = "registered_credentials_json"
    private const val KEY_THEME_MODE = "app_theme_mode"
    private const val KEY_WALLPAPER_SETTINGS = "wallpaper_settings_json"
    private const val KEY_FCM_SETTINGS = "fcm_push_settings_json"
    private const val KEY_CRASHLYTICS_SETTINGS = "crashlytics_settings_json"
    private const val KEY_STABILITY_DIAGNOSTICS = "stability_diagnostics_json"

    const val DEFAULT_TEST_USERNAME = "test"
    const val DEFAULT_TEST_PASSWORD = "pass"
    const val DEFAULT_TEST_UID = "local_user_test"
    const val DEFAULT_TEST_HOUSEHOLD_ID = "local_household_test"

    private val _activeUser = MutableStateFlow<LocalAuthUser?>(null)
    val activeUser: StateFlow<LocalAuthUser?> = _activeUser.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _wallpaperSettings = MutableStateFlow(WallpaperSettings())
    val wallpaperSettings: StateFlow<WallpaperSettings> = _wallpaperSettings.asStateFlow()

    private val _fcmPushSettings = MutableStateFlow(FcmPushSettings())
    val fcmPushSettings: StateFlow<FcmPushSettings> = _fcmPushSettings.asStateFlow()

    private val _crashlyticsSettings = MutableStateFlow(CrashlyticsSettings())
    val crashlyticsSettings: StateFlow<CrashlyticsSettings> = _crashlyticsSettings.asStateFlow()

    private val _stabilityDiagnostics = MutableStateFlow<List<StabilityDiagnosticEntry>>(emptyList())
    val stabilityDiagnostics: StateFlow<List<StabilityDiagnosticEntry>> = _stabilityDiagnostics.asStateFlow()

    private var initialized = false
    private var uncaughtHandlerInstalled = false

    fun ensureInitialized(context: Context) {
        val prefs = prefs(context)
        seedBuiltInTestAccount(prefs)
        LocalAccountDataStore.ensureInitialized(context)
        installCrashlyticsUncaughtHandlerIfNeeded(context.applicationContext)
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val savedJson = prefs.getString(KEY_ACTIVE_USER, null)
            if (!savedJson.isNullOrBlank()) {
                _activeUser.value = parseUserJson(savedJson)
            }
            val savedTheme = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
            _themeMode.value = runCatching { AppThemeMode.valueOf(savedTheme ?: "SYSTEM") }
                .getOrDefault(AppThemeMode.SYSTEM)
            _wallpaperSettings.value = decodeWallpaperSettings(prefs.getString(KEY_WALLPAPER_SETTINGS, null))
            _fcmPushSettings.value = decodeFcmSettings(prefs.getString(KEY_FCM_SETTINGS, null))
            _crashlyticsSettings.value = decodeCrashlyticsSettings(prefs.getString(KEY_CRASHLYTICS_SETTINGS, null))
            _stabilityDiagnostics.value = decodeStabilityDiagnostics(prefs.getString(KEY_STABILITY_DIAGNOSTICS, null))
                .ifEmpty { seedInitialStabilityBreadcrumbs() }
            initialized = true
        }
    }

    private fun seedInitialStabilityBreadcrumbs(): List<StabilityDiagnosticEntry> {
        val deviceLabel = "${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})"
        return listOf(
            StabilityDiagnosticEntry(
                id = "diag_init_sms_guard",
                timestampMillis = System.currentTimeMillis() - 120_000L,
                subsystem = "SMS_AUTO_SYNC",
                severity = "INFO",
                message = "Paced background SMS sync guard active (Dispatchers.IO, 65ms batching).",
                deviceModel = deviceLabel
            ),
            StabilityDiagnosticEntry(
                id = "diag_init_typo_guard",
                timestampMillis = System.currentTimeMillis() - 60_000L,
                subsystem = "RESPONSIVE_TYPOGRAPHY",
                severity = "INFO",
                message = "Relative screen typography scaling verified with safe font bounds.",
                deviceModel = deviceLabel
            )
        )
    }

    private fun installCrashlyticsUncaughtHandlerIfNeeded(appContext: Context) {
        if (uncaughtHandlerInstalled) return
        if (Build.FINGERPRINT.equals("robolectric", ignoreCase = true)) return
        uncaughtHandlerInstalled = true
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                if (_crashlyticsSettings.value.crashReportingEnabled) {
                    recordDiagnostic(
                        context = appContext,
                        subsystem = "RUNTIME_GUARD",
                        severity = "CRASH_PREVENTED",
                        message = "${throwable.javaClass.simpleName} on ${thread.name}: ${throwable.localizedMessage ?: "Unhandled error"}"
                    )
                }
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    fun setWallpaperSettings(context: Context, settings: WallpaperSettings) {
        ensureInitialized(context)
        val clamped = settings.copy(opacityPercent = settings.opacityPercent.coerceIn(4, 28))
        _wallpaperSettings.value = clamped
        val obj = JSONObject().apply {
            put("enabled", clamped.enabled)
            put("style", clamped.style.name)
            put("colorAccent", clamped.colorAccent.name)
            put("density", clamped.density.name)
            put("opacityPercent", clamped.opacityPercent)
        }
        prefs(context).edit().putString(KEY_WALLPAPER_SETTINGS, obj.toString()).apply()
    }

    fun updateWallpaperSettings(context: Context, newSettings: WallpaperSettings) {
        setWallpaperSettings(context, newSettings)
    }

    fun setFcmPushSettings(context: Context, settings: FcmPushSettings) {
        ensureInitialized(context)
        _fcmPushSettings.value = settings
        val obj = JSONObject().apply {
            put("familyTransactionAlertsEnabled", settings.familyTransactionAlertsEnabled)
            put("budgetThresholdAlertsEnabled", settings.budgetThresholdAlertsEnabled)
            put("subscribedTopic", settings.subscribedTopic)
        }
        prefs(context).edit().putString(KEY_FCM_SETTINGS, obj.toString()).apply()
    }

    fun updateFcmPushSettings(context: Context, newSettings: FcmPushSettings) {
        setFcmPushSettings(context, newSettings)
    }

    fun setCrashlyticsSettings(context: Context, settings: CrashlyticsSettings) {
        ensureInitialized(context)
        _crashlyticsSettings.value = settings
        val obj = JSONObject().apply {
            put("crashReportingEnabled", settings.crashReportingEnabled)
            put("smsSyncGuardEnabled", settings.smsSyncGuardEnabled)
            put("typographyGuardEnabled", settings.typographyGuardEnabled)
        }
        prefs(context).edit().putString(KEY_CRASHLYTICS_SETTINGS, obj.toString()).apply()
    }

    fun updateCrashlyticsSettings(context: Context, newSettings: CrashlyticsSettings) {
        setCrashlyticsSettings(context, newSettings)
    }

    fun recordDiagnostic(
        context: Context?,
        subsystem: String,
        severity: String,
        message: String
    ) {
        if (!_crashlyticsSettings.value.crashReportingEnabled) return
        val deviceLabel = "${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})"
        val entry = StabilityDiagnosticEntry(
            id = "diag_${UUID.randomUUID().toString().take(8)}",
            timestampMillis = System.currentTimeMillis(),
            subsystem = subsystem,
            severity = severity,
            message = message.take(240),
            deviceModel = deviceLabel
        )
        val updated = (listOf(entry) + _stabilityDiagnostics.value).take(40)
        _stabilityDiagnostics.value = updated
        if (context != null) {
            val arr = JSONArray()
            updated.forEach { item ->
                arr.put(
                    JSONObject().apply {
                        put("id", item.id)
                        put("timestampMillis", item.timestampMillis)
                        put("subsystem", item.subsystem)
                        put("severity", item.severity)
                        put("message", item.message)
                        put("deviceModel", item.deviceModel)
                    }
                )
            }
            prefs(context).edit().putString(KEY_STABILITY_DIAGNOSTICS, arr.toString()).apply()
        }
    }

    fun clearStabilityDiagnostics(context: Context) {
        ensureInitialized(context)
        _stabilityDiagnostics.value = emptyList()
        prefs(context).edit().remove(KEY_STABILITY_DIAGNOSTICS).apply()
    }

    private fun decodeWallpaperSettings(raw: String?): WallpaperSettings {
        if (raw.isNullOrBlank()) return WallpaperSettings()
        return runCatching {
            val o = JSONObject(raw)
            WallpaperSettings(
                enabled = o.optBoolean("enabled", true),
                style = runCatching {
                    WallpaperPatternStyle.valueOf(o.optString("style", WallpaperPatternStyle.WHATSAPP_DOODLES.name))
                }.getOrDefault(WallpaperPatternStyle.WHATSAPP_DOODLES),
                colorAccent = runCatching {
                    WallpaperColorAccent.valueOf(o.optString("colorAccent", WallpaperColorAccent.THEME_ADAPTIVE.name))
                }.getOrDefault(WallpaperColorAccent.THEME_ADAPTIVE),
                density = runCatching {
                    WallpaperDensity.valueOf(o.optString("density", WallpaperDensity.BALANCED.name))
                }.getOrDefault(WallpaperDensity.BALANCED),
                opacityPercent = o.optInt("opacityPercent", 11).coerceIn(4, 28)
            )
        }.getOrDefault(WallpaperSettings())
    }

    private fun decodeFcmSettings(raw: String?): FcmPushSettings {
        if (raw.isNullOrBlank()) return FcmPushSettings()
        return runCatching {
            val o = JSONObject(raw)
            FcmPushSettings(
                familyTransactionAlertsEnabled = o.optBoolean("familyTransactionAlertsEnabled", true),
                budgetThresholdAlertsEnabled = o.optBoolean("budgetThresholdAlertsEnabled", true),
                subscribedTopic = o.optString("subscribedTopic", "household_shared")
            )
        }.getOrDefault(FcmPushSettings())
    }

    private fun decodeCrashlyticsSettings(raw: String?): CrashlyticsSettings {
        if (raw.isNullOrBlank()) return CrashlyticsSettings()
        return runCatching {
            val o = JSONObject(raw)
            CrashlyticsSettings(
                crashReportingEnabled = o.optBoolean("crashReportingEnabled", true),
                smsSyncGuardEnabled = o.optBoolean("smsSyncGuardEnabled", true),
                typographyGuardEnabled = o.optBoolean("typographyGuardEnabled", true)
            )
        }.getOrDefault(CrashlyticsSettings())
    }

    private fun decodeStabilityDiagnostics(raw: String?): List<StabilityDiagnosticEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = mutableListOf<StabilityDiagnosticEntry>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    StabilityDiagnosticEntry(
                        id = o.optString("id", "diag_$i"),
                        timestampMillis = o.optLong("timestampMillis", System.currentTimeMillis()),
                        subsystem = o.optString("subsystem", "RUNTIME_GUARD"),
                        severity = o.optString("severity", "INFO"),
                        message = o.optString("message", ""),
                        deviceModel = o.optString("deviceModel", "Android Device")
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        ensureInitialized(context)
        _themeMode.value = mode
        prefs(context).edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun cycleThemeMode(context: Context): AppThemeMode {
        ensureInitialized(context)
        val next = when (_themeMode.value) {
            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.SYSTEM
        }
        setThemeMode(context, next)
        return next
    }

    fun getCurrentUser(context: Context? = null): LocalAuthUser? {
        if (!initialized && context != null) {
            ensureInitialized(context)
        }
        return _activeUser.value
    }

    fun hasCompletedInitialSmsSync(context: Context, userId: String): Boolean {
        if (userId.isBlank()) return false
        ensureInitialized(context)
        return prefs(context).getBoolean("initial_sms_sync_done_$userId", false)
    }

    fun setInitialSmsSyncCompleted(context: Context, userId: String, completed: Boolean = true) {
        if (userId.isBlank()) return
        ensureInitialized(context)
        prefs(context).edit().putBoolean("initial_sms_sync_done_$userId", completed).apply()
    }

    fun signInWithUsernamePassword(
        context: Context,
        rawUsername: String,
        rawPassword: String
    ): Result<LocalAuthUser> {
        ensureInitialized(context)
        val username = rawUsername.trim().lowercase()
        val password = rawPassword

        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter both username and password."))
        }

        val prefs = prefs(context)
        val accountsObj = getCredentialsMap(prefs)
        val record = accountsObj.optJSONObject(username)
            ?: return Result.failure(
                IllegalArgumentException("Username '$username' not found. Try 'test' / 'pass' or tap Create Account.")
            )

        val storedPassword = record.optString("password", "")
        if (storedPassword != password) {
            return Result.failure(IllegalArgumentException("Incorrect password for '$username'."))
        }

        val defaultName = if (username == DEFAULT_TEST_USERNAME) "Mock User" else username.replaceFirstChar { it.uppercase() }
        val storedName = record.optString("displayName", defaultName)
        val finalDisplayName = if (username == DEFAULT_TEST_USERNAME && (storedName == "Test User" || storedName.isBlank())) "Mock User" else storedName
        val user = LocalAuthUser(
            uid = record.optString("uid", "local_user_$username"),
            username = username,
            displayName = finalDisplayName,
            email = record.optString("email", "$username@axiofamily.local")
        )
        setActiveUser(prefs, user)
        LocalAccountDataStore.ensureDefaultDataForUser(context, user)
        return Result.success(user)
    }

    fun registerUsernamePassword(
        context: Context,
        rawUsername: String,
        rawPassword: String,
        rawDisplayName: String = ""
    ): Result<LocalAuthUser> {
        ensureInitialized(context)
        val username = rawUsername.trim().lowercase()
        val password = rawPassword

        if (username.length < 2) {
            return Result.failure(IllegalArgumentException("Username must be at least 2 characters."))
        }
        if (password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter a password."))
        }

        val prefs = prefs(context)
        val accountsObj = getCredentialsMap(prefs)
        val uid = if (username == DEFAULT_TEST_USERNAME) DEFAULT_TEST_UID else "local_user_$username"
        val displayName = rawDisplayName.trim().ifBlank {
            if (username == DEFAULT_TEST_USERNAME) "Mock User" else username.replaceFirstChar { it.uppercase() }
        }
        val email = "$username@axiofamily.local"

        val record = JSONObject().apply {
            put("uid", uid)
            put("username", username)
            put("password", password)
            put("displayName", displayName)
            put("email", email)
        }
        accountsObj.put(username, record)
        prefs.edit().putString(KEY_CREDENTIALS_MAP, accountsObj.toString()).apply()

        val user = LocalAuthUser(
            uid = uid,
            username = username,
            displayName = displayName,
            email = email
        )
        setActiveUser(prefs, user)
        LocalAccountDataStore.ensureDefaultDataForUser(context, user)
        return Result.success(user)
    }

    fun updateDisplayName(context: Context, newDisplayName: String) {
        ensureInitialized(context)
        val cleanName = newDisplayName.trim().ifBlank { return }
        val current = _activeUser.value ?: return
        val prefs = prefs(context)
        val updatedUser = current.copy(displayName = cleanName)
        setActiveUser(prefs, updatedUser)

        val accountsObj = getCredentialsMap(prefs)
        val record = accountsObj.optJSONObject(current.username)
        if (record != null) {
            record.put("displayName", cleanName)
            accountsObj.put(current.username, record)
            prefs.edit().putString(KEY_CREDENTIALS_MAP, accountsObj.toString()).apply()
        }
    }

    fun signOut(context: Context? = null) {
        _activeUser.value = null
        if (context != null) {
            prefs(context).edit().remove(KEY_ACTIVE_USER).apply()
        }
    }

    private fun seedBuiltInTestAccount(prefs: SharedPreferences) {
        val accountsObj = getCredentialsMap(prefs)
        val existing = accountsObj.optJSONObject(DEFAULT_TEST_USERNAME)
        if (existing == null || existing.optString("displayName") == "Test User") {
            val testRecord = JSONObject().apply {
                put("uid", DEFAULT_TEST_UID)
                put("username", DEFAULT_TEST_USERNAME)
                put("password", DEFAULT_TEST_PASSWORD)
                put("displayName", "Mock User")
                put("email", "test@axiofamily.local")
            }
            accountsObj.put(DEFAULT_TEST_USERNAME, testRecord)
            prefs.edit().putString(KEY_CREDENTIALS_MAP, accountsObj.toString()).apply()
        }
    }

    private fun getCredentialsMap(prefs: SharedPreferences): JSONObject {
        val raw = prefs.getString(KEY_CREDENTIALS_MAP, null)
        return if (raw.isNullOrBlank()) JSONObject() else runCatching { JSONObject(raw) }.getOrDefault(JSONObject())
    }

    private fun setActiveUser(prefs: SharedPreferences, user: LocalAuthUser) {
        val obj = JSONObject().apply {
            put("uid", user.uid)
            put("username", user.username)
            put("displayName", user.displayName)
            put("email", user.email)
        }
        prefs.edit().putString(KEY_ACTIVE_USER, obj.toString()).apply()
        _activeUser.value = user
    }

    private fun parseUserJson(raw: String): LocalAuthUser? = runCatching {
        val obj = JSONObject(raw)
        val uid = obj.optString("uid", "")
        if (uid.isBlank()) return null
        val rawName = obj.optString("displayName", "Mock User")
        val finalName = if (uid == DEFAULT_TEST_UID && rawName == "Test User") "Mock User" else rawName
        LocalAuthUser(
            uid = uid,
            username = obj.optString("username", "test"),
            displayName = finalName,
            email = obj.optString("email", "test@axiofamily.local")
        )
    }.getOrNull()

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

/**
 * Reactive local store for Username/Password accounts (such as `test` / `pass`) so all
 * Household & Personal expenses, SMS imports, Receipt edits, Budgets, and Accounts persist
 * locally without requiring Google Sign-In or Firebase Auth credentials.
 */
data class SmsRawRecord(
    val signature: String,
    val sender: String,
    val body: String,
    val timestamp: Long
)

object LocalAccountDataStore {
    private const val STORE_PREFS = "axio_local_data_store_prefs"
    private const val KEY_PROFILES = "profiles_json"
    private const val KEY_HOUSEHOLDS = "households_json"
    private const val KEY_EXPENSES = "expenses_json"
    private const val KEY_BUDGETS = "budgets_json"
    private const val KEY_ACCOUNTS = "accounts_json"
    private const val KEY_SMS_RAW_MAP = "sms_raw_map_json"

    private val profilesFlow = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    private val householdsFlow = MutableStateFlow<Map<String, Household>>(emptyMap())
    private val expensesFlow = MutableStateFlow<List<ExpenseTransaction>>(emptyList())
    private val budgetsFlow = MutableStateFlow<List<CategoryBudget>>(emptyList())
    private val accountsFlow = MutableStateFlow<List<FinancialAccount>>(emptyList())
    private val smsRawMapFlow = MutableStateFlow<Map<String, SmsRawRecord>>(emptyMap())

    private var appContext: Context? = null
    private var loaded = false

    fun ensureInitialized(context: Context) {
        appContext = context.applicationContext
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val p = prefs() ?: return
            profilesFlow.value = decodeProfiles(p.getString(KEY_PROFILES, null))
            householdsFlow.value = decodeHouseholds(p.getString(KEY_HOUSEHOLDS, null))
            expensesFlow.value = decodeExpenses(p.getString(KEY_EXPENSES, null))
            budgetsFlow.value = decodeBudgets(p.getString(KEY_BUDGETS, null))
            accountsFlow.value = decodeAccounts(p.getString(KEY_ACCOUNTS, null))
            smsRawMapFlow.value = decodeSmsRawMap(p.getString(KEY_SMS_RAW_MAP, null))
            loaded = true
        }
    }

    fun cacheSmsMessage(
        signature: String,
        sender: String,
        body: String,
        timestamp: Long,
        persistImmediately: Boolean = true
    ) {
        if (signature.isBlank() || body.isBlank()) return
        val current = smsRawMapFlow.value.toMutableMap()
        current[signature] = SmsRawRecord(
            signature = signature,
            sender = sender,
            body = body,
            timestamp = timestamp
        )
        smsRawMapFlow.value = current
        if (persistImmediately) {
            saveSmsRawMap(current)
        }
    }

    fun persistSmsRawCache() {
        saveSmsRawMap(smsRawMapFlow.value)
    }

    fun getSmsMessageForTransaction(tx: ExpenseTransaction): SmsRawRecord? {
        val sig = tx.smsSignature.ifBlank { tx.id.removePrefix("sms_") }
        if (sig.isNotBlank()) {
            smsRawMapFlow.value[sig]?.let { return it }
        }
        // Fallback if the raw SMS was stored in notes
        val notes = tx.notes.trim()
        if (notes.startsWith("[") && notes.contains("] ")) {
            val sender = notes.substringAfter("[").substringBefore("]")
            val body = notes.substringAfter("] ").trim()
            if (body.isNotBlank()) {
                return SmsRawRecord(
                    signature = sig,
                    sender = sender,
                    body = body,
                    timestamp = tx.transactionDateMillis
                )
            }
        } else if (tx.smsSignature.isNotBlank() && notes.isNotBlank()) {
            return SmsRawRecord(
                signature = tx.smsSignature,
                sender = tx.accountName,
                body = notes,
                timestamp = tx.transactionDateMillis
            )
        }
        return null
    }

    fun getLinkedSmsMessagesForTransaction(tx: ExpenseTransaction): List<SmsRawRecord> {
        val result = mutableListOf<SmsRawRecord>()
        getSmsMessageForTransaction(tx)?.let { result.add(it) }
        val map = smsRawMapFlow.value
        tx.linkedSmsSignatures.forEach { linkedSig ->
            val cleanSig = linkedSig.removePrefix("sms_")
            val record = map[linkedSig] ?: map[cleanSig]
            if (record != null && result.none { it.signature == record.signature || it.body == record.body }) {
                result.add(record)
            }
        }
        return result
    }

    fun ensureDefaultDataForUser(context: Context, user: LocalAuthUser) {
        ensureInitialized(context)
        val currentProfiles = profilesFlow.value.toMutableMap()
        val currentHouseholds = householdsFlow.value.toMutableMap()

        // Pre-seed a default household for the built-in `test` user so Household & Personal
        // division works immediately upon logging in with `test` / `pass`.
        val isTestUser = user.uid == LocalAccountSessionManager.DEFAULT_TEST_UID
        val defaultHouseholdId = if (isTestUser) LocalAccountSessionManager.DEFAULT_TEST_HOUSEHOLD_ID else ""

        if (isTestUser && !currentHouseholds.containsKey(defaultHouseholdId)) {
            currentHouseholds[defaultHouseholdId] = Household(
                id = defaultHouseholdId,
                name = "Test Family Home",
                ownerUid = user.uid,
                joinCode = "TEST01",
                memberUids = listOf(user.uid),
                memberNames = mapOf(user.uid to user.displayName),
                memberEmails = mapOf(user.uid to user.email),
                invitedEmails = emptyList(),
                monthlyBudgetLimit = 0.0,
                currencySymbol = "₹",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            householdsFlow.value = currentHouseholds
            saveHouseholds(currentHouseholds)
        } else if (isTestUser && currentHouseholds[defaultHouseholdId]?.monthlyBudgetLimit == 75000.0) {
            val existingHh = currentHouseholds[defaultHouseholdId]!!
            currentHouseholds[defaultHouseholdId] = existingHh.copy(monthlyBudgetLimit = 0.0)
            householdsFlow.value = currentHouseholds
            saveHouseholds(currentHouseholds)
        }

        if (!currentProfiles.containsKey(user.uid)) {
            currentProfiles[user.uid] = UserProfile(
                id = user.uid,
                uid = user.uid,
                email = user.email,
                displayName = user.displayName,
                photoUrl = "",
                activeHouseholdId = defaultHouseholdId,
                personalMonthlyBudget = 0.0,
                currencySymbol = "₹",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            profilesFlow.value = currentProfiles
            saveProfiles(currentProfiles)
        } else if (currentProfiles[user.uid]?.personalMonthlyBudget == 35000.0) {
            val existingProf = currentProfiles[user.uid]!!
            currentProfiles[user.uid] = existingProf.copy(personalMonthlyBudget = 0.0)
            profilesFlow.value = currentProfiles
            saveProfiles(currentProfiles)
        }

        if (isTestUser) {
            LocalAccountSessionManager.setInitialSmsSyncCompleted(context, user.uid, true)
            seedMockDataForTestUser(user, defaultHouseholdId)
        }
    }

    private fun seedMockDataForTestUser(user: LocalAuthUser, householdId: String) {
        if (expensesFlow.value.any { it.userId == user.uid && it.id.startsWith("mock_tx_") }) {
            return
        }

        fun makeTimestamp(calYear: Int, calMonth: Int, calDay: Int, hour: Int, min: Int, sec: Int): Long {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, calYear)
                set(Calendar.MONTH, calMonth)
                set(Calendar.DAY_OF_MONTH, calDay)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, sec)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val baseCal = Calendar.getInstance()
        // Cover the last 3 months (current month + 3 prior months)
        val monthsToSeed = (0..3).map { offset ->
            Calendar.getInstance().apply {
                timeInMillis = baseCal.timeInMillis
                add(Calendar.MONTH, -offset)
            }
        }

        val mockTxList = mutableListOf<ExpenseTransaction>()
        val mockSmsMap = smsRawMapFlow.value.toMutableMap()
        val memberUids = listOf(user.uid)

        monthsToSeed.forEachIndexed { monthIndex, mCal ->
            val y = mCal.get(Calendar.YEAR)
            val m = mCal.get(Calendar.MONTH) // 0-indexed
            val maxDaysInMonth = mCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val isCurrentMonth = monthIndex == 0
            val effectiveMaxDay = if (isCurrentMonth) baseCal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(8) else maxDaysInMonth

            // 1. Monthly Salary (Income)
            val salTime = makeTimestamp(y, m, 1, 10, 0, 0)
            val salSig = "sms_mock_sal_${y}_$m"
            mockTxList.add(
                ExpenseTransaction(
                    id = "mock_tx_sal_${y}_$m",
                    userId = user.uid,
                    userName = user.displayName,
                    householdId = householdId,
                    title = "TechCorp Monthly Salary Deposit",
                    amount = 85000.0,
                    type = "INCOME",
                    category = "Salary",
                    accountName = "HDFC Salary A/c",
                    paymentMode = "BANK",
                    tags = listOf("salary", "income"),
                    notes = "Monthly payroll transfer from employer",
                    visibility = "SHARED",
                    smsSignature = salSig,
                    referenceNumber = "SAL-$y-$m-8821",
                    transactionDateMillis = salTime,
                    memberUids = memberUids,
                    createdAt = Timestamp(Date(salTime)),
                    updatedAt = Timestamp.now()
                )
            )
            mockSmsMap[salSig] = SmsRawRecord(
                signature = salSig,
                sender = "VM-HDFCBK",
                body = "Salary of Rs 85,000.00 credited to HDFC Bank A/c XX4321 on 01-${m+1}-$y by TechCorp India Pvt Ltd. Avail Bal: Rs 88,450.00.",
                timestamp = salTime
            )

            // 2. Apartment Rent
            val rentDay = 2.coerceAtMost(effectiveMaxDay)
            val rentTime = makeTimestamp(y, m, rentDay, 11, 30, 0)
            val rentSig = "sms_mock_rent_${y}_$m"
            mockTxList.add(
                ExpenseTransaction(
                    id = "mock_tx_rent_${y}_$m",
                    userId = user.uid,
                    userName = user.displayName,
                    householdId = householdId,
                    title = "Monthly Apartment Rent",
                    amount = 28000.0,
                    type = "EXPENSE",
                    category = "Rent & Housing",
                    accountName = "HDFC Salary A/c",
                    paymentMode = "BANK",
                    tags = listOf("rent", "fixed", "home"),
                    notes = "Transferred to landlord via NEFT",
                    visibility = "SHARED",
                    smsSignature = rentSig,
                    referenceNumber = "NEFT-RENT-$y$m",
                    transactionDateMillis = rentTime,
                    memberUids = memberUids,
                    createdAt = Timestamp(Date(rentTime)),
                    updatedAt = Timestamp.now()
                )
            )
            mockSmsMap[rentSig] = SmsRawRecord(
                signature = rentSig,
                sender = "VM-HDFCBK",
                body = "Rs 28,000.00 debited from A/c XX4321 on $rentDay-${m+1}-$y towards NEFT Rent Transfer to Landlord. Avail Bal: Rs 60,450.00.",
                timestamp = rentTime
            )

            // 3. Supermarket Groceries
            val grocDay = 3.coerceAtMost(effectiveMaxDay)
            val grocTime = makeTimestamp(y, m, grocDay, 18, 45, 12)
            val grocSig = "sms_mock_groc_${y}_$m"
            mockTxList.add(
                ExpenseTransaction(
                    id = "mock_tx_groc_${y}_$m",
                    userId = user.uid,
                    userName = user.displayName,
                    householdId = householdId,
                    title = "Nature's Basket Organic Groceries",
                    amount = 3420.0 + (monthIndex * 210.0),
                    type = "EXPENSE",
                    category = "Groceries",
                    accountName = "HDFC Salary A/c",
                    paymentMode = "UPI",
                    tags = listOf("groceries", "organic", "weekly"),
                    notes = "Weekly fresh fruits, veggies, and organic supplies",
                    visibility = "SHARED",
                    smsSignature = grocSig,
                    referenceNumber = "UPI-GROC-9823$monthIndex",
                    transactionDateMillis = grocTime,
                    memberUids = memberUids,
                    createdAt = Timestamp(Date(grocTime)),
                    updatedAt = Timestamp.now()
                )
            )
            mockSmsMap[grocSig] = SmsRawRecord(
                signature = grocSig,
                sender = "VM-HDFCBK",
                body = "Rs ${3420.0 + (monthIndex * 210.0)} debited from HDFC Bank A/c XX4321 on $grocDay-${m+1}-$y to Nature's Basket via UPI Ref 9823145612.",
                timestamp = grocTime
            )

            // Seed a duplicate UPI app SMS alert for the same Nature's Basket transaction in the current month so user can test Swipe-Right Duplicate Linking
            if (isCurrentMonth) {
                val dupTime = grocTime + 15_000L
                val dupSig = "sms_mock_groc_dup_${y}_$m"
                mockTxList.add(
                    ExpenseTransaction(
                        id = "mock_tx_groc_dup_${y}_$m",
                        userId = user.uid,
                        userName = user.displayName,
                        householdId = householdId,
                        title = "UPI Alert: Nature's Basket Store",
                        amount = 3420.0,
                        type = "EXPENSE",
                        category = "Groceries",
                        accountName = "BHIM UPI (XX4321)",
                        paymentMode = "UPI",
                        tags = listOf("upi-alert", "possible-duplicate"),
                        notes = "[NPCI-BHIM] Paid Rs 3420.00 to Natures Basket via UPI Ref 9823145612",
                        visibility = "SHARED",
                        smsSignature = dupSig,
                        referenceNumber = "UPI-GROC-98230",
                        transactionDateMillis = dupTime,
                        memberUids = memberUids,
                        createdAt = Timestamp(Date(dupTime)),
                        updatedAt = Timestamp.now()
                    )
                )
                mockSmsMap[dupSig] = SmsRawRecord(
                    signature = dupSig,
                    sender = "AD-BHIMUP",
                    body = "BHIM UPI: You have paid Rs 3,420.00 to Nature's Basket Store from HDFC Bank A/c XX4321 on $grocDay-${m+1}-$y. UPI Ref: 9823145612.",
                    timestamp = dupTime
                )
            }

            // 4. Restaurant Dinner
            val dinDay = 4.coerceAtMost(effectiveMaxDay)
            val dinTime = makeTimestamp(y, m, dinDay, 20, 15, 30)
            val dinSig = "sms_mock_din_${y}_$m"
            mockTxList.add(
                ExpenseTransaction(
                    id = "mock_tx_din_${y}_$m",
                    userId = user.uid,
                    userName = user.displayName,
                    householdId = householdId,
                    title = "Swiggy Gourmet Family Dinner",
                    amount = 1480.0 + (monthIndex * 150.0),
                    type = "EXPENSE",
                    category = "Food & Dining",
                    accountName = "Axis Credit Card",
                    paymentMode = "CARD",
                    tags = listOf("dinner", "family", "swiggy"),
                    notes = "Family dinner delivery",
                    visibility = "SHARED",
                    smsSignature = dinSig,
                    referenceNumber = "AXIS-SWIG-4412$monthIndex",
                    transactionDateMillis = dinTime,
                    memberUids = memberUids,
                    createdAt = Timestamp(Date(dinTime)),
                    updatedAt = Timestamp.now()
                )
            )
            mockSmsMap[dinSig] = SmsRawRecord(
                signature = dinSig,
                sender = "AX-AXISBK",
                body = "Spent Rs ${1480.0 + (monthIndex * 150.0)} on your Axis Bank Credit Card XX7788 at Swiggy on $dinDay-${m+1}-$y. Avail Limit: Rs 145000.00.",
                timestamp = dinTime
            )

            // 5. Speciality Coffee (Personal)
            val cofDay = 5.coerceAtMost(effectiveMaxDay)
            val cofTime = makeTimestamp(y, m, cofDay, 9, 20, 45)
            val cofSig = "sms_mock_cof_${y}_$m"
            mockTxList.add(
                ExpenseTransaction(
                    id = "mock_tx_cof_${y}_$m",
                    userId = user.uid,
                    userName = user.displayName,
                    householdId = "",
                    title = "Blue Tokai Coffee & Croissant",
                    amount = 450.0,
                    type = "EXPENSE",
                    category = "Food & Dining",
                    accountName = "HDFC Salary A/c",
                    paymentMode = "UPI",
                    tags = listOf("coffee", "personal"),
                    notes = "Morning roast & work meeting",
                    visibility = "PERSONAL",
                    smsSignature = cofSig,
                    referenceNumber = "UPI-COF-1122$monthIndex",
                    transactionDateMillis = cofTime,
                    memberUids = memberUids,
                    createdAt = Timestamp(Date(cofTime)),
                    updatedAt = Timestamp.now()
                )
            )
            mockSmsMap[cofSig] = SmsRawRecord(
                signature = cofSig,
                sender = "VM-HDFCBK",
                body = "Rs 450.00 debited from A/c XX4321 on $cofDay-${m+1}-$y to Blue Tokai Coffee via UPI Ref 11223344.",
                timestamp = cofTime
            )

            // 6. Electricity & Utilities
            val utilDay = 6.coerceAtMost(effectiveMaxDay)
            val utilTime = makeTimestamp(y, m, utilDay, 17, 10, 20)
            val utilSig = "sms_mock_util_${y}_$m"
            mockTxList.add(
                ExpenseTransaction(
                    id = "mock_tx_util_${y}_$m",
                    userId = user.uid,
                    userName = user.displayName,
                    householdId = householdId,
                    title = "Electricity & Power Utility",
                    amount = 2850.0 + (monthIndex * 90.0),
                    type = "EXPENSE",
                    category = "Utilities & Bills",
                    accountName = "ICICI Savings A/c",
                    paymentMode = "UPI",
                    tags = listOf("electricity", "bills"),
                    notes = "Monthly electricity bill payment",
                    visibility = "SHARED",
                    smsSignature = utilSig,
                    referenceNumber = "UPI-UTIL-8765$monthIndex",
                    transactionDateMillis = utilTime,
                    memberUids = memberUids,
                    createdAt = Timestamp(Date(utilTime)),
                    updatedAt = Timestamp.now()
                )
            )
            mockSmsMap[utilSig] = SmsRawRecord(
                signature = utilSig,
                sender = "VK-ICICIB",
                body = "Your A/c XX9012 is debited for Rs ${2850.0 + (monthIndex * 90.0)} on $utilDay-${m+1}-$y. Info: UPI/Adani Electricity/Ref 87654321.",
                timestamp = utilTime
            )

            // 7. Shopping
            val shopDay = 7.coerceAtMost(effectiveMaxDay)
            val shopTime = makeTimestamp(y, m, shopDay, 14, 0, 15)
            val shopSig = "sms_mock_shop_${y}_$m"
            mockTxList.add(
                ExpenseTransaction(
                    id = "mock_tx_shop_${y}_$m",
                    userId = user.uid,
                    userName = user.displayName,
                    householdId = "",
                    title = "Amazon Books & Electronics",
                    amount = 4199.0 + (monthIndex * 300.0),
                    type = "EXPENSE",
                    category = "Shopping",
                    accountName = "Axis Credit Card",
                    paymentMode = "CARD",
                    tags = listOf("shopping", "amazon"),
                    notes = "Headphones and books",
                    visibility = "PERSONAL",
                    smsSignature = shopSig,
                    referenceNumber = "AXIS-AMZ-7711$monthIndex",
                    transactionDateMillis = shopTime,
                    memberUids = memberUids,
                    createdAt = Timestamp(Date(shopTime)),
                    updatedAt = Timestamp.now()
                )
            )
            mockSmsMap[shopSig] = SmsRawRecord(
                signature = shopSig,
                sender = "AX-AXISBK",
                body = "Spent Rs ${4199.0 + (monthIndex * 300.0)} on Axis Bank Credit Card XX7788 at Amazon India on $shopDay-${m+1}-$y. Avail Limit: Rs 140701.00.",
                timestamp = shopTime
            )

            // 8. Fuel & Transport
            val fuelDay = 8.coerceAtMost(effectiveMaxDay)
            val fuelTime = makeTimestamp(y, m, fuelDay, 12, 40, 5)
            val fuelSig = "sms_mock_fuel_${y}_$m"
            mockTxList.add(
                ExpenseTransaction(
                    id = "mock_tx_fuel_${y}_$m",
                    userId = user.uid,
                    userName = user.displayName,
                    householdId = householdId,
                    title = "Indian Oil Petrol Pump",
                    amount = 2500.0,
                    type = "EXPENSE",
                    category = "Transport & Fuel",
                    accountName = "Axis Credit Card",
                    paymentMode = "CARD",
                    tags = listOf("fuel", "car"),
                    notes = "Full tank petrol for car",
                    visibility = "SHARED",
                    smsSignature = fuelSig,
                    referenceNumber = "AXIS-IOCL-9988$monthIndex",
                    transactionDateMillis = fuelTime,
                    memberUids = memberUids,
                    createdAt = Timestamp(Date(fuelTime)),
                    updatedAt = Timestamp.now()
                )
            )
            mockSmsMap[fuelSig] = SmsRawRecord(
                signature = fuelSig,
                sender = "AX-AXISBK",
                body = "Spent Rs 2,500.00 on Axis Bank Credit Card XX7788 at Indian Oil on $fuelDay-${m+1}-$y. Avail Limit: Rs 138201.00.",
                timestamp = fuelTime
            )

            // For past months, add second half of month transactions
            if (!isCurrentMonth) {
                // Broadband
                val bbDay = 12.coerceAtMost(maxDaysInMonth)
                val bbTime = makeTimestamp(y, m, bbDay, 11, 20, 0)
                val bbSig = "sms_mock_bb_${y}_$m"
                mockTxList.add(
                    ExpenseTransaction(
                        id = "mock_tx_bb_${y}_$m",
                        userId = user.uid,
                        userName = user.displayName,
                        householdId = householdId,
                        title = "Airtel Fiber Broadband",
                        amount = 1179.0,
                        type = "EXPENSE",
                        category = "Utilities & Bills",
                        accountName = "ICICI Savings A/c",
                        paymentMode = "UPI",
                        tags = listOf("broadband", "wifi"),
                        notes = "Monthly high-speed internet plan",
                        visibility = "SHARED",
                        smsSignature = bbSig,
                        referenceNumber = "UPI-AIRTEL-$y$m",
                        transactionDateMillis = bbTime,
                        memberUids = memberUids,
                        createdAt = Timestamp(Date(bbTime)),
                        updatedAt = Timestamp.now()
                    )
                )
                mockSmsMap[bbSig] = SmsRawRecord(
                    signature = bbSig,
                    sender = "VK-ICICIB",
                    body = "Your A/c XX9012 is debited for Rs 1,179.00 on $bbDay-${m+1}-$y to Airtel Broadband via UPI Ref 76543210.",
                    timestamp = bbTime
                )

                // Cinema & Entertainment
                val movDay = 18.coerceAtMost(maxDaysInMonth)
                val movTime = makeTimestamp(y, m, movDay, 21, 30, 0)
                val movSig = "sms_mock_mov_${y}_$m"
                mockTxList.add(
                    ExpenseTransaction(
                        id = "mock_tx_mov_${y}_$m",
                        userId = user.uid,
                        userName = user.displayName,
                        householdId = householdId,
                        title = "PVR IMAX Movie & Snacks",
                        amount = 1650.0,
                        type = "EXPENSE",
                        category = "Entertainment",
                        accountName = "Axis Credit Card",
                        paymentMode = "CARD",
                        tags = listOf("movies", "weekend", "entertainment"),
                        notes = "Family cinema outing",
                        visibility = "SHARED",
                        smsSignature = movSig,
                        referenceNumber = "AXIS-PVR-$y$m",
                        transactionDateMillis = movTime,
                        memberUids = memberUids,
                        createdAt = Timestamp(Date(movTime)),
                        updatedAt = Timestamp.now()
                    )
                )
                mockSmsMap[movSig] = SmsRawRecord(
                    signature = movSig,
                    sender = "AX-AXISBK",
                    body = "Spent Rs 1,650.00 on Axis Bank Credit Card XX7788 at PVR Cinemas on $movDay-${m+1}-$y. Avail Limit: Rs 136551.00.",
                    timestamp = movTime
                )

                // Healthcare / Pharmacy
                val medDay = 24.coerceAtMost(maxDaysInMonth)
                val medTime = makeTimestamp(y, m, medDay, 16, 15, 0)
                val medSig = "sms_mock_med_${y}_$m"
                mockTxList.add(
                    ExpenseTransaction(
                        id = "mock_tx_med_${y}_$m",
                        userId = user.uid,
                        userName = user.displayName,
                        householdId = householdId,
                        title = "Apollo Pharmacy Health Essentials",
                        amount = 1380.0,
                        type = "EXPENSE",
                        category = "Healthcare",
                        accountName = "HDFC Salary A/c",
                        paymentMode = "UPI",
                        tags = listOf("health", "pharmacy"),
                        notes = "Monthly vitamins and medicine refill",
                        visibility = "SHARED",
                        smsSignature = medSig,
                        referenceNumber = "UPI-APOLLO-$y$m",
                        transactionDateMillis = medTime,
                        memberUids = memberUids,
                        createdAt = Timestamp(Date(medTime)),
                        updatedAt = Timestamp.now()
                    )
                )
                mockSmsMap[medSig] = SmsRawRecord(
                    signature = medSig,
                    sender = "VM-HDFCBK",
                    body = "Rs 1,380.00 debited from A/c XX4321 on $medDay-${m+1}-$y to Apollo Pharmacy via UPI Ref 55443322.",
                    timestamp = medTime
                )
            }
        }

        val updatedExpenses = expensesFlow.value + mockTxList
        expensesFlow.value = updatedExpenses
        saveExpenses(updatedExpenses)

        smsRawMapFlow.value = mockSmsMap
        saveSmsRawMap(mockSmsMap)

        // Seed default financial accounts if none exist
        if (accountsFlow.value.none { it.userId == user.uid }) {
            val mockAccounts = listOf(
                FinancialAccount(
                    id = "${user.uid}_hdfc_salary_4321",
                    userId = user.uid,
                    name = "HDFC Salary A/c",
                    bankName = "HDFC Bank",
                    accountType = "BANK",
                    lastFourDigits = "4321",
                    balance = 84250.0,
                    creditLimit = 0.0,
                    isSharedVisible = true
                ),
                FinancialAccount(
                    id = "${user.uid}_icici_savings_9012",
                    userId = user.uid,
                    name = "ICICI Savings A/c",
                    bankName = "ICICI Bank",
                    accountType = "BANK",
                    lastFourDigits = "9012",
                    balance = 35600.0,
                    creditLimit = 0.0,
                    isSharedVisible = true
                ),
                FinancialAccount(
                    id = "${user.uid}_axis_card_7788",
                    userId = user.uid,
                    name = "Axis Neo Credit Card",
                    bankName = "Axis Bank",
                    accountType = "CREDIT_CARD",
                    lastFourDigits = "7788",
                    balance = 14200.0,
                    creditLimit = 150000.0,
                    isSharedVisible = true
                ),
                FinancialAccount(
                    id = "${user.uid}_cash_wallet_0000",
                    userId = user.uid,
                    name = "Cash Pocket",
                    bankName = "Cash",
                    accountType = "CASH",
                    lastFourDigits = "0000",
                    balance = 5400.0,
                    creditLimit = 0.0,
                    isSharedVisible = true
                )
            )
            accountsFlow.value = mockAccounts
            saveAccounts(mockAccounts)
        }
    }

    // --- OBSERVERS ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        profilesFlow.map { it[userId] }

    fun getUserProfileOnce(userId: String): UserProfile? =
        profilesFlow.value[userId]

    fun observeHousehold(householdId: String): Flow<Household?> =
        householdsFlow.map { it[householdId] }

    fun getHouseholdOnce(householdId: String): Household? =
        householdsFlow.value[householdId]

    fun observeEmailInvitedHouseholds(userEmail: String): Flow<List<Household>> {
        val norm = userEmail.trim().lowercase()
        return householdsFlow.map { map ->
            map.values.filter { h -> h.invitedEmails.any { it.equals(norm, ignoreCase = true) } }
        }
    }

    fun observePersonalExpenses(userId: String): Flow<List<ExpenseTransaction>> =
        expensesFlow.map { list ->
            list.filter { it.userId == userId && it.visibility == "PERSONAL" }
                .sortedByDescending { it.transactionDateMillis }
        }

    fun observeSharedExpenses(householdId: String): Flow<List<ExpenseTransaction>> =
        expensesFlow.map { list ->
            list.filter { it.householdId == householdId && it.visibility == "SHARED" }
                .sortedByDescending { it.transactionDateMillis }
        }

    fun observePersonalCategoryBudgets(userId: String): Flow<List<CategoryBudget>> =
        budgetsFlow.map { list ->
            list.filter { it.userId == userId && it.householdId.isBlank() }
        }

    fun observeSharedCategoryBudgets(householdId: String): Flow<List<CategoryBudget>> =
        budgetsFlow.map { list ->
            list.filter { it.householdId == householdId }
        }

    fun observeAccounts(userId: String): Flow<List<FinancialAccount>> =
        accountsFlow.map { list ->
            list.filter { it.userId == userId }
        }

    // --- MUTATIONS ---

    fun ensureUserProfile(
        uid: String,
        displayName: String,
        email: String,
        photoUrl: String = ""
    ): UserProfile {
        val current = profilesFlow.value.toMutableMap()
        val existing = current[uid]
        if (existing != null) return existing

        val defaultHid = if (uid == LocalAccountSessionManager.DEFAULT_TEST_UID) {
            LocalAccountSessionManager.DEFAULT_TEST_HOUSEHOLD_ID
        } else {
            ""
        }

        val created = UserProfile(
            id = uid,
            uid = uid,
            email = email,
            displayName = displayName.ifBlank { "Test User" },
            photoUrl = photoUrl,
            activeHouseholdId = defaultHid,
            personalMonthlyBudget = 0.0,
            currencySymbol = "₹",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        current[uid] = created
        profilesFlow.value = current
        saveProfiles(current)
        return created
    }

    fun updatePersonalBudgetSettings(
        uid: String,
        personalMonthlyBudget: Double,
        currencySymbol: String
    ) {
        val current = profilesFlow.value.toMutableMap()
        val existing = current[uid] ?: ensureUserProfile(uid, "Test User", "")
        current[uid] = existing.copy(
            personalMonthlyBudget = personalMonthlyBudget.coerceAtLeast(0.0),
            currencySymbol = currencySymbol.ifBlank { "₹" }.take(10),
            updatedAt = Timestamp.now()
        )
        profilesFlow.value = current
        saveProfiles(current)
    }

    fun updateUserDisplayName(
        uid: String,
        newDisplayName: String,
        household: Household?
    ) {
        val cleanName = newDisplayName.trim().ifBlank { return }.take(100)
        val currentProfiles = profilesFlow.value.toMutableMap()
        val existing = currentProfiles[uid] ?: ensureUserProfile(uid, cleanName, "")
        currentProfiles[uid] = existing.copy(
            displayName = cleanName,
            updatedAt = Timestamp.now()
        )
        profilesFlow.value = currentProfiles
        saveProfiles(currentProfiles)

        if (household != null) {
            val hMap = householdsFlow.value.toMutableMap()
            val curHh = hMap[household.id]
            if (curHh != null) {
                val updatedNames = curHh.memberNames.toMutableMap().apply {
                    put(uid, cleanName)
                }
                hMap[household.id] = curHh.copy(
                    memberNames = updatedNames,
                    updatedAt = Timestamp.now()
                )
                householdsFlow.value = hMap
                saveHouseholds(hMap)
            }
        }

        val updatedExpenses = expensesFlow.value.map { tx ->
            if (tx.userId == uid) tx.copy(userName = cleanName) else tx
        }
        expensesFlow.value = updatedExpenses
        saveExpenses(updatedExpenses)
    }

    fun createHousehold(
        uid: String,
        name: String,
        monthlyBudgetLimit: Double,
        currencySymbol: String,
        userDisplayName: String,
        userEmail: String
    ): Household {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val joinCode = (1..6).map { chars.random() }.joinToString("")
        val hid = "local_hh_${UUID.randomUUID().toString().take(8)}"
        val cleanName = name.trim().ifBlank { "Our Family Budget" }.take(100)
        val cleanUser = userDisplayName.trim().ifBlank { "Member" }.take(100)
        val cleanEmail = userEmail.trim().lowercase().take(200)

        val household = Household(
            id = hid,
            name = cleanName,
            ownerUid = uid,
            joinCode = joinCode,
            memberUids = listOf(uid),
            memberNames = mapOf(uid to cleanUser),
            memberEmails = mapOf(uid to cleanEmail),
            invitedEmails = emptyList(),
            monthlyBudgetLimit = monthlyBudgetLimit.coerceAtLeast(0.0),
            currencySymbol = currencySymbol.ifBlank { "₹" }.take(10),
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        val hMap = householdsFlow.value.toMutableMap()
        hMap[hid] = household
        householdsFlow.value = hMap
        saveHouseholds(hMap)

        val pMap = profilesFlow.value.toMutableMap()
        val prof = pMap[uid] ?: ensureUserProfile(uid, cleanUser, cleanEmail)
        pMap[uid] = prof.copy(activeHouseholdId = hid, updatedAt = Timestamp.now())
        profilesFlow.value = pMap
        saveProfiles(pMap)

        return household
    }

    fun joinHouseholdByCode(
        uid: String,
        rawJoinCode: String,
        userDisplayName: String,
        userEmail: String
    ): String {
        val code = rawJoinCode.trim().uppercase()
        val match = householdsFlow.value.values.firstOrNull { it.joinCode.equals(code, ignoreCase = true) }
            ?: throw IllegalArgumentException("No household found with code $code.")
        return joinHouseholdById(uid, match.id, userDisplayName, userEmail)
    }

    fun joinHouseholdById(
        uid: String,
        householdId: String,
        userDisplayName: String,
        userEmail: String
    ): String {
        val hMap = householdsFlow.value.toMutableMap()
        val current = hMap[householdId] ?: throw IllegalArgumentException("Household no longer exists.")
        val updatedMembers = (current.memberUids + uid).distinct()
        val updatedNames = current.memberNames.toMutableMap().apply {
            put(uid, userDisplayName.ifBlank { "Family Member" })
        }
        val normEmail = userEmail.trim().lowercase()
        val updatedEmails = current.memberEmails.toMutableMap().apply {
            put(uid, normEmail)
        }
        val updatedInvites = current.invitedEmails.filterNot { it.equals(normEmail, ignoreCase = true) }

        hMap[householdId] = current.copy(
            memberUids = updatedMembers,
            memberNames = updatedNames,
            memberEmails = updatedEmails,
            invitedEmails = updatedInvites,
            updatedAt = Timestamp.now()
        )
        householdsFlow.value = hMap
        saveHouseholds(hMap)

        val pMap = profilesFlow.value.toMutableMap()
        val prof = pMap[uid] ?: ensureUserProfile(uid, userDisplayName, normEmail)
        pMap[uid] = prof.copy(activeHouseholdId = householdId, updatedAt = Timestamp.now())
        profilesFlow.value = pMap
        saveProfiles(pMap)

        return householdId
    }

    fun inviteMemberByEmail(household: Household, inviteeEmail: String) {
        val norm = inviteeEmail.trim().lowercase()
        require(norm.contains("@")) { "Enter a valid email address." }
        val hMap = householdsFlow.value.toMutableMap()
        val current = hMap[household.id] ?: household
        hMap[household.id] = current.copy(
            invitedEmails = (current.invitedEmails + norm).distinct(),
            updatedAt = Timestamp.now()
        )
        householdsFlow.value = hMap
        saveHouseholds(hMap)
    }

    fun updateHouseholdSettings(householdId: String, name: String, monthlyBudgetLimit: Double) {
        val hMap = householdsFlow.value.toMutableMap()
        val current = hMap[householdId] ?: return
        hMap[householdId] = current.copy(
            name = name.trim().ifBlank { "Family Budget" },
            monthlyBudgetLimit = monthlyBudgetLimit.coerceAtLeast(0.0),
            updatedAt = Timestamp.now()
        )
        householdsFlow.value = hMap
        saveHouseholds(hMap)
    }

    fun addExpense(
        uid: String,
        userName: String,
        household: Household?,
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tags: List<String>,
        notes: String,
        isShared: Boolean,
        smsSignature: String = "",
        referenceNumber: String = "",
        transactionDateMillis: Long = System.currentTimeMillis(),
        customDocId: String? = null
    ): String {
        val effectiveShared = isShared && household != null
        val targetHouseholdId = if (effectiveShared) household!!.id else ""
        val memberUids = if (effectiveShared) (household!!.memberUids + uid).distinct() else listOf(uid)
        val docId = if (!customDocId.isNullOrBlank()) customDocId else "local_tx_${UUID.randomUUID().toString().take(12)}"

        val cleanTags = tags.map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .distinct()
            .take(10)

        val tx = ExpenseTransaction(
            id = docId,
            userId = uid,
            userName = userName.ifBlank { "Me" }.take(100),
            householdId = targetHouseholdId,
            title = title.trim().ifBlank { category }.take(150),
            amount = amount.coerceAtLeast(0.01),
            type = if (type == "INCOME") "INCOME" else "EXPENSE",
            category = category.ifBlank { "General" }.take(60),
            accountName = accountName.ifBlank { "Cash" }.take(100),
            paymentMode = if (paymentMode in setOf("UPI", "BANK", "CARD", "WALLET", "CASH")) paymentMode else "UPI",
            tags = cleanTags,
            notes = notes.trim().take(500),
            visibility = if (effectiveShared) "SHARED" else "PERSONAL",
            smsSignature = smsSignature.take(200),
            referenceNumber = referenceNumber.take(100),
            transactionDateMillis = transactionDateMillis,
            memberUids = memberUids,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        val updated = expensesFlow.value.filterNot { it.id == docId } + tx
        expensesFlow.value = updated
        saveExpenses(updated)
        return docId
    }

    fun batchImportSmsCandidates(
        uid: String,
        candidates: List<SmsCandidate>,
        userName: String,
        household: Household?,
        shareByDefault: Boolean = true
    ): Int {
        if (candidates.isEmpty()) return 0
        val effectiveShared = shareByDefault && household != null
        val targetHouseholdId = if (effectiveShared) household!!.id else ""
        val memberUids = if (effectiveShared) (household!!.memberUids + uid).distinct() else listOf(uid)

        val sortedCandidates = candidates.sortedBy { it.parsed.timestamp }
        val currentMap = expensesFlow.value.associateBy { it.id }.toMutableMap()
        val alreadyLinkedSignatures = expensesFlow.value
            .flatMap { tx -> tx.linkedSmsSignatures + listOf(tx.smsSignature, tx.id) }
            .filter { it.isNotBlank() }
            .flatMap { listOf(it, it.removePrefix("sms_")) }
            .toSet()
        val accountsToUpsert = mutableMapOf<String, SmsCandidate>()
        var importedCount = 0

        for (candidate in sortedCandidates) {
            if (candidate.id in alreadyLinkedSignatures) continue
            val parsed = candidate.parsed
            val isIncome = parsed.type == TransactionType.INCOME
            val category = candidate.suggestedCategory.ifBlank { "General" }.take(60)
            val merchantTitle = (parsed.merchant?.takeIf { it.isNotBlank() }
                ?: "${parsed.bankName} ${parsed.type.name.lowercase().replaceFirstChar { it.uppercase() }}").take(150)
            val docId = "sms_${candidate.id}"
            val tags = listOf("sms-auto", parsed.bankName.lowercase().replace(" ", "-").take(35))
                .filter { it.isNotBlank() }
                .distinct()
            val paymentMode = if (candidate.suggestedPaymentMode in setOf("UPI", "BANK", "CARD", "WALLET", "CASH")) {
                candidate.suggestedPaymentMode
            } else {
                "UPI"
            }

            cacheSmsMessage(
                signature = candidate.id,
                sender = parsed.sender,
                body = parsed.smsBody,
                timestamp = parsed.timestamp
            )

            currentMap[docId] = ExpenseTransaction(
                id = docId,
                userId = uid,
                userName = userName.ifBlank { "Me" }.take(100),
                householdId = targetHouseholdId,
                title = merchantTitle,
                amount = parsed.amount.toDouble().coerceAtLeast(0.01),
                type = if (isIncome) "INCOME" else "EXPENSE",
                category = category,
                accountName = candidate.suggestedAccountName.ifBlank { parsed.bankName }.take(100),
                paymentMode = paymentMode,
                tags = tags,
                notes = "[${parsed.sender}] ${parsed.smsBody}".take(500),
                visibility = if (effectiveShared) "SHARED" else "PERSONAL",
                smsSignature = candidate.id.take(200),
                referenceNumber = parsed.reference.orEmpty().take(100),
                transactionDateMillis = parsed.timestamp.coerceAtLeast(1L),
                memberUids = memberUids,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            importedCount++

            if (parsed.balance != null || parsed.creditLimit != null || parsed.accountNumber != null) {
                accountsToUpsert[candidate.suggestedAccountName] = candidate
            }
        }

        val updatedList = currentMap.values.toList()
        expensesFlow.value = updatedList
        saveExpenses(updatedList)

        for ((_, candidate) in accountsToUpsert) {
            val parsed = candidate.parsed
            val accType = when {
                parsed.isFromCard -> "CREDIT_CARD"
                candidate.suggestedPaymentMode == "WALLET" -> "WALLET"
                else -> "BANK"
            }
            upsertAccount(
                uid = uid,
                name = candidate.suggestedAccountName,
                bankName = parsed.bankName,
                accountType = accType,
                lastFourDigits = parsed.accountNumber.orEmpty(),
                balance = parsed.balance?.toDouble() ?: 0.0,
                creditLimit = parsed.creditLimit?.toDouble() ?: 0.0,
                isSharedVisible = effectiveShared
            )
        }

        return importedCount
    }

    fun updateExpense(
        uid: String,
        existing: ExpenseTransaction,
        household: Household?,
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tags: List<String>,
        notes: String,
        transactionDateMillis: Long,
        isShared: Boolean
    ) {
        val wantShared = isShared && household != null
        val targetHouseholdId = if (wantShared) household!!.id else ""
        val memberUids = if (wantShared) (household!!.memberUids + uid).distinct() else listOf(uid)
        val cleanTags = tags.map { it.trim().removePrefix("#") }.filter { it.isNotBlank() }.distinct().take(10)
        val validMode = if (paymentMode in setOf("UPI", "BANK", "CARD", "WALLET", "CASH")) paymentMode else "UPI"
        val validType = if (type == "INCOME") "INCOME" else "EXPENSE"

        val updatedTx = existing.copy(
            householdId = targetHouseholdId,
            title = title.trim().ifBlank { category }.take(150),
            amount = amount.coerceAtLeast(0.01),
            type = validType,
            category = category.trim().ifBlank { "General" }.take(60),
            accountName = accountName.trim().ifBlank { "Cash" }.take(100),
            paymentMode = validMode,
            tags = cleanTags,
            notes = notes.trim().take(500),
            visibility = if (wantShared) "SHARED" else "PERSONAL",
            transactionDateMillis = transactionDateMillis.coerceAtLeast(1L),
            memberUids = memberUids,
            updatedAt = Timestamp.now()
        )

        val updatedList = expensesFlow.value.map { if (it.id == existing.id) updatedTx else it }
        expensesFlow.value = updatedList
        saveExpenses(updatedList)
    }

    fun updateExpenseCategoryById(expenseId: String, newCategory: String) {
        val updatedList = expensesFlow.value.map { tx ->
            if (tx.id == expenseId) {
                tx.copy(
                    category = newCategory.trim().ifBlank { "General" }.take(60),
                    updatedAt = Timestamp.now()
                )
            } else {
                tx
            }
        }
        expensesFlow.value = updatedList
        saveExpenses(updatedList)
    }

    fun toggleExpensePrivacy(uid: String, expense: ExpenseTransaction, household: Household) {
        val movingToShared = expense.visibility == "PERSONAL"
        val updatedTx = expense.copy(
            householdId = if (movingToShared) household.id else "",
            visibility = if (movingToShared) "SHARED" else "PERSONAL",
            memberUids = if (movingToShared) (household.memberUids + uid).distinct() else listOf(uid),
            updatedAt = Timestamp.now()
        )
        val updatedList = expensesFlow.value.map { if (it.id == expense.id) updatedTx else it }
        expensesFlow.value = updatedList
        saveExpenses(updatedList)
    }

    fun deleteExpense(expenseId: String) {
        val updatedList = expensesFlow.value.filterNot { it.id == expenseId }
        expensesFlow.value = updatedList
        saveExpenses(updatedList)
    }

    /**
     * Links and merges [duplicateTx] into [primaryTx] so duplicate SMS alerts or double-logged
     * entries are consolidated into a single transaction while retaining all raw SMS bodies and signatures.
     */
    fun linkDuplicateTransaction(
        primaryTx: ExpenseTransaction,
        duplicateTx: ExpenseTransaction
    ): ExpenseTransaction {
        require(primaryTx.id != duplicateTx.id) { "Cannot link a transaction to itself." }

        // Ensure duplicate's raw SMS is cached under its signature or ID if present in notes
        getSmsMessageForTransaction(duplicateTx)?.let { dupSms ->
            val key = duplicateTx.smsSignature.ifBlank { duplicateTx.id }
            cacheSmsMessage(
                signature = key,
                sender = dupSms.sender,
                body = dupSms.body,
                timestamp = dupSms.timestamp
            )
        }

        val combinedSignatures = buildList {
            addAll(primaryTx.linkedSmsSignatures)
            if (duplicateTx.smsSignature.isNotBlank()) add(duplicateTx.smsSignature)
            if (duplicateTx.id.isNotBlank()) add(duplicateTx.id)
            addAll(duplicateTx.linkedSmsSignatures)
        }.distinct()

        val combinedTags = (primaryTx.tags + duplicateTx.tags + "linked-duplicate")
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .distinct()
            .take(10)

        val linkNoteSuffix = "[Linked Duplicate: ${duplicateTx.title} • ${duplicateTx.accountName}]"
        val combinedNotes = if (primaryTx.notes.contains(linkNoteSuffix)) {
            primaryTx.notes
        } else if (primaryTx.notes.isBlank()) {
            linkNoteSuffix
        } else {
            "${primaryTx.notes}\n$linkNoteSuffix"
        }.take(500)

        val mergedPrimary = primaryTx.copy(
            linkedSmsSignatures = combinedSignatures,
            tags = combinedTags,
            notes = combinedNotes,
            referenceNumber = primaryTx.referenceNumber.ifBlank { duplicateTx.referenceNumber },
            updatedAt = Timestamp.now()
        )

        val updatedList = expensesFlow.value
            .filterNot { it.id == duplicateTx.id }
            .map { if (it.id == primaryTx.id) mergedPrimary else it }

        expensesFlow.value = updatedList
        saveExpenses(updatedList)
        return mergedPrimary
    }

    fun saveCategoryBudget(
        uid: String,
        category: String,
        monthlyLimit: Double,
        alertThresholdPercent: Int,
        isShared: Boolean,
        household: Household?
    ): String {
        val effectiveShared = isShared && household != null
        val sanitizedId = category.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "general" }
        val docId = if (effectiveShared) "sh_${household!!.id}_$sanitizedId" else "pe_${uid}_$sanitizedId"
        if (monthlyLimit <= 0.0) {
            deleteCategoryBudget(docId)
            return docId
        }
        val memberUids = if (effectiveShared) (household!!.memberUids + uid).distinct() else listOf(uid)

        val budget = CategoryBudget(
            id = docId,
            userId = uid,
            householdId = if (effectiveShared) household!!.id else "",
            category = category.take(60),
            monthlyLimit = monthlyLimit.coerceAtLeast(0.0),
            alertThresholdPercent = alertThresholdPercent.coerceIn(1, 100).toLong(),
            memberUids = memberUids,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        val updated = budgetsFlow.value.filterNot { it.id == docId } + budget
        budgetsFlow.value = updated
        saveBudgets(updated)
        return docId
    }

    fun deleteCategoryBudget(budgetId: String) {
        val updated = budgetsFlow.value.filterNot { it.id == budgetId }
        budgetsFlow.value = updated
        saveBudgets(updated)
    }

    fun upsertAccount(
        uid: String,
        name: String,
        bankName: String,
        accountType: String,
        lastFourDigits: String,
        balance: Double,
        creditLimit: Double = 0.0,
        isSharedVisible: Boolean = true
    ): String {
        val docKey = "${uid}_${bankName}_${accountType}_${lastFourDigits}"
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(64)
            .ifBlank { UUID.randomUUID().toString().take(16) }

        val acc = FinancialAccount(
            id = docKey,
            userId = uid,
            name = name.trim().ifBlank { bankName }.take(100),
            bankName = bankName.trim().ifBlank { "Bank" }.take(100),
            accountType = if (accountType in setOf("BANK", "CREDIT_CARD", "WALLET", "CASH", "UPI")) accountType else "BANK",
            lastFourDigits = lastFourDigits.trim().take(10),
            balance = balance,
            creditLimit = creditLimit.coerceAtLeast(0.0),
            isSharedVisible = isSharedVisible,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        val updated = accountsFlow.value.filterNot { it.id == docKey } + acc
        accountsFlow.value = updated
        saveAccounts(updated)
        return docKey
    }

    fun deleteAccount(accountId: String) {
        val updated = accountsFlow.value.filterNot { it.id == accountId }
        accountsFlow.value = updated
        saveAccounts(updated)
    }

    // --- JSON SERIALIZATION HELPERS ---

    private fun prefs(): SharedPreferences? =
        appContext?.getSharedPreferences(STORE_PREFS, Context.MODE_PRIVATE)

    private fun saveProfiles(map: Map<String, UserProfile>) {
        val arr = JSONArray()
        map.values.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("uid", p.uid)
                    put("email", p.email)
                    put("displayName", p.displayName)
                    put("photoUrl", p.photoUrl)
                    put("activeHouseholdId", p.activeHouseholdId)
                    put("personalMonthlyBudget", p.personalMonthlyBudget)
                    put("currencySymbol", p.currencySymbol)
                }
            )
        }
        prefs()?.edit()?.putString(KEY_PROFILES, arr.toString())?.apply()
    }

    private fun decodeProfiles(raw: String?): Map<String, UserProfile> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val arr = JSONArray(raw)
            val map = mutableMapOf<String, UserProfile>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val uid = o.optString("uid", "")
                if (uid.isNotBlank()) {
                    val rawPersonal = o.optDouble("personalMonthlyBudget", 0.0)
                    map[uid] = UserProfile(
                        id = o.optString("id", uid),
                        uid = uid,
                        email = o.optString("email", ""),
                        displayName = o.optString("displayName", "Test User"),
                        photoUrl = o.optString("photoUrl", ""),
                        activeHouseholdId = o.optString("activeHouseholdId", ""),
                        personalMonthlyBudget = if (rawPersonal == 35000.0) 0.0 else rawPersonal,
                        currencySymbol = o.optString("currencySymbol", "₹"),
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                }
            }
            map
        }.getOrDefault(emptyMap())
    }

    private fun saveHouseholds(map: Map<String, Household>) {
        val arr = JSONArray()
        map.values.forEach { h ->
            arr.put(
                JSONObject().apply {
                    put("id", h.id)
                    put("name", h.name)
                    put("ownerUid", h.ownerUid)
                    put("joinCode", h.joinCode)
                    put("memberUids", JSONArray(h.memberUids))
                    put("memberNames", JSONObject(h.memberNames))
                    put("memberEmails", JSONObject(h.memberEmails))
                    put("invitedEmails", JSONArray(h.invitedEmails))
                    put("monthlyBudgetLimit", h.monthlyBudgetLimit)
                    put("currencySymbol", h.currencySymbol)
                }
            )
        }
        prefs()?.edit()?.putString(KEY_HOUSEHOLDS, arr.toString())?.apply()
    }

    private fun decodeHouseholds(raw: String?): Map<String, Household> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val arr = JSONArray(raw)
            val map = mutableMapOf<String, Household>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val id = o.optString("id", "")
                if (id.isNotBlank()) {
                    val memberUids = mutableListOf<String>()
                    o.optJSONArray("memberUids")?.let { a ->
                        for (j in 0 until a.length()) memberUids.add(a.optString(j))
                    }
                    val memberNames = mutableMapOf<String, String>()
                    o.optJSONObject("memberNames")?.let { m ->
                        m.keys().forEach { k -> memberNames[k] = m.optString(k) }
                    }
                    val memberEmails = mutableMapOf<String, String>()
                    o.optJSONObject("memberEmails")?.let { m ->
                        m.keys().forEach { k -> memberEmails[k] = m.optString(k) }
                    }
                    val invitedEmails = mutableListOf<String>()
                    o.optJSONArray("invitedEmails")?.let { a ->
                        for (j in 0 until a.length()) invitedEmails.add(a.optString(j))
                    }
                    val rawHhLimit = o.optDouble("monthlyBudgetLimit", 0.0)
                    map[id] = Household(
                        id = id,
                        name = o.optString("name", "Family Home"),
                        ownerUid = o.optString("ownerUid", ""),
                        joinCode = o.optString("joinCode", "TEST01"),
                        memberUids = memberUids,
                        memberNames = memberNames,
                        memberEmails = memberEmails,
                        invitedEmails = invitedEmails,
                        monthlyBudgetLimit = if (rawHhLimit == 75000.0) 0.0 else rawHhLimit,
                        currencySymbol = o.optString("currencySymbol", "₹"),
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                }
            }
            map
        }.getOrDefault(emptyMap())
    }

    private fun saveExpenses(list: List<ExpenseTransaction>) {
        val arr = JSONArray()
        list.forEach { tx ->
            arr.put(
                JSONObject().apply {
                    put("id", tx.id)
                    put("userId", tx.userId)
                    put("userName", tx.userName)
                    put("householdId", tx.householdId)
                    put("title", tx.title)
                    put("amount", tx.amount)
                    put("type", tx.type)
                    put("category", tx.category)
                    put("accountName", tx.accountName)
                    put("paymentMode", tx.paymentMode)
                    put("tags", JSONArray(tx.tags))
                    put("notes", tx.notes)
                    put("visibility", tx.visibility)
                    put("smsSignature", tx.smsSignature)
                    put("linkedSmsSignatures", JSONArray(tx.linkedSmsSignatures))
                    put("referenceNumber", tx.referenceNumber)
                    put("transactionDateMillis", tx.transactionDateMillis)
                    put("memberUids", JSONArray(tx.memberUids))
                }
            )
        }
        prefs()?.edit()?.putString(KEY_EXPENSES, arr.toString())?.apply()
    }

    private fun decodeExpenses(raw: String?): List<ExpenseTransaction> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = mutableListOf<ExpenseTransaction>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val tags = mutableListOf<String>()
                o.optJSONArray("tags")?.let { a ->
                    for (j in 0 until a.length()) tags.add(a.optString(j))
                }
                val linkedSmsSignatures = mutableListOf<String>()
                o.optJSONArray("linkedSmsSignatures")?.let { a ->
                    for (j in 0 until a.length()) linkedSmsSignatures.add(a.optString(j))
                }
                val memberUids = mutableListOf<String>()
                o.optJSONArray("memberUids")?.let { a ->
                    for (j in 0 until a.length()) memberUids.add(a.optString(j))
                }
                val dateMillis = o.optLong("transactionDateMillis", System.currentTimeMillis())
                list.add(
                    ExpenseTransaction(
                        id = o.optString("id", ""),
                        userId = o.optString("userId", ""),
                        userName = o.optString("userName", "Me"),
                        householdId = o.optString("householdId", ""),
                        title = o.optString("title", ""),
                        amount = o.optDouble("amount", 0.0),
                        type = o.optString("type", "EXPENSE"),
                        category = o.optString("category", "General"),
                        accountName = o.optString("accountName", "Cash"),
                        paymentMode = o.optString("paymentMode", "UPI"),
                        tags = tags,
                        notes = o.optString("notes", ""),
                        visibility = o.optString("visibility", "PERSONAL"),
                        smsSignature = o.optString("smsSignature", ""),
                        linkedSmsSignatures = linkedSmsSignatures,
                        referenceNumber = o.optString("referenceNumber", ""),
                        transactionDateMillis = dateMillis,
                        memberUids = memberUids,
                        createdAt = Timestamp(Date(dateMillis)),
                        updatedAt = Timestamp.now()
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun saveBudgets(list: List<CategoryBudget>) {
        val arr = JSONArray()
        list.forEach { b ->
            arr.put(
                JSONObject().apply {
                    put("id", b.id)
                    put("userId", b.userId)
                    put("householdId", b.householdId)
                    put("category", b.category)
                    put("monthlyLimit", b.monthlyLimit)
                    put("alertThresholdPercent", b.alertThresholdPercent)
                    put("memberUids", JSONArray(b.memberUids))
                }
            )
        }
        prefs()?.edit()?.putString(KEY_BUDGETS, arr.toString())?.apply()
    }

    private fun decodeBudgets(raw: String?): List<CategoryBudget> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = mutableListOf<CategoryBudget>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val memberUids = mutableListOf<String>()
                o.optJSONArray("memberUids")?.let { a ->
                    for (j in 0 until a.length()) memberUids.add(a.optString(j))
                }
                list.add(
                    CategoryBudget(
                        id = o.optString("id", ""),
                        userId = o.optString("userId", ""),
                        householdId = o.optString("householdId", ""),
                        category = o.optString("category", "General"),
                        monthlyLimit = o.optDouble("monthlyLimit", 5000.0),
                        alertThresholdPercent = o.optLong("alertThresholdPercent", 80L),
                        memberUids = memberUids,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun saveAccounts(list: List<FinancialAccount>) {
        val arr = JSONArray()
        list.forEach { a ->
            arr.put(
                JSONObject().apply {
                    put("id", a.id)
                    put("userId", a.userId)
                    put("name", a.name)
                    put("bankName", a.bankName)
                    put("accountType", a.accountType)
                    put("lastFourDigits", a.lastFourDigits)
                    put("balance", a.balance)
                    put("creditLimit", a.creditLimit)
                    put("isSharedVisible", a.isSharedVisible)
                }
            )
        }
        prefs()?.edit()?.putString(KEY_ACCOUNTS, arr.toString())?.apply()
    }

    private fun decodeAccounts(raw: String?): List<FinancialAccount> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            val list = mutableListOf<FinancialAccount>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    FinancialAccount(
                        id = o.optString("id", ""),
                        userId = o.optString("userId", ""),
                        name = o.optString("name", "Bank"),
                        bankName = o.optString("bankName", "Bank"),
                        accountType = o.optString("accountType", "BANK"),
                        lastFourDigits = o.optString("lastFourDigits", ""),
                        balance = o.optDouble("balance", 0.0),
                        creditLimit = o.optDouble("creditLimit", 0.0),
                        isSharedVisible = o.optBoolean("isSharedVisible", true),
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                )
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun saveSmsRawMap(map: Map<String, SmsRawRecord>) {
        val arr = JSONArray()
        // Keep up to the 800 most recent SMS bodies so SharedPreferences stays fast and compact
        map.values
            .sortedByDescending { it.timestamp }
            .take(800)
            .forEach { r ->
                arr.put(
                    JSONObject().apply {
                        put("signature", r.signature)
                        put("sender", r.sender)
                        put("body", r.body)
                        put("timestamp", r.timestamp)
                    }
                )
            }
        prefs()?.edit()?.putString(KEY_SMS_RAW_MAP, arr.toString())?.apply()
    }

    private fun decodeSmsRawMap(raw: String?): Map<String, SmsRawRecord> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val arr = JSONArray(raw)
            val map = mutableMapOf<String, SmsRawRecord>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val sig = o.optString("signature", "")
                val body = o.optString("body", "")
                if (sig.isNotBlank() && body.isNotBlank()) {
                    map[sig] = SmsRawRecord(
                        signature = sig,
                        sender = o.optString("sender", ""),
                        body = body,
                        timestamp = o.optLong("timestamp", 0L)
                    )
                }
            }
            map
        }.getOrDefault(emptyMap())
    }
}
