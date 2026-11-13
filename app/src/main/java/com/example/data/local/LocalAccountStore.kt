package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.data.model.CategoryBudget
import com.example.data.model.CrashlyticsSettings
import com.example.data.model.ExpertFeatureFlagsSettings
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
import com.example.ui.theme.AppPrimaryColorOption
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
    private const val PREFS_NAME = "onefamily_local_auth_prefs"
    private const val KEY_ACTIVE_USER = "active_local_user_json"
    private const val KEY_CREDENTIALS_MAP = "registered_credentials_json"
    private const val KEY_THEME_MODE = "app_theme_mode"
    private const val KEY_PRIMARY_COLOR_OPTION = "app_primary_color_option"
    private const val KEY_CUSTOM_PRIMARY_COLOR_ARGB = "app_custom_primary_color_argb"
    private const val KEY_WALLPAPER_SETTINGS = "wallpaper_settings_json"
    private const val KEY_FCM_SETTINGS = "fcm_push_settings_json"
    private const val KEY_CRASHLYTICS_SETTINGS = "crashlytics_settings_json"
    private const val KEY_EXPERT_FEATURE_FLAGS = "expert_feature_flags_json"
    private const val KEY_STABILITY_DIAGNOSTICS = "stability_diagnostics_json"
    private const val KEY_HOUSEHOLD_FEATURE_ENABLED = "household_feature_enabled"
    private const val KEY_CUSTOM_CATEGORIES = "custom_categories_json"
    private const val KEY_AMOLED_DARK_ENABLED = "amoled_dark_enabled"
    private const val KEY_HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled"

    const val DEFAULT_TEST_USERNAME = "test"
    const val DEFAULT_TEST_PASSWORD = "pass"
    const val DEFAULT_TEST_UID = "local_user_test"
    const val DEFAULT_TEST_HOUSEHOLD_ID = "local_household_test"

    private val _activeUser = MutableStateFlow<LocalAuthUser?>(null)
    val activeUser: StateFlow<LocalAuthUser?> = _activeUser.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _primaryColorOption = MutableStateFlow(AppPrimaryColorOption.KIWI_GREEN)
    val primaryColorOption: StateFlow<AppPrimaryColorOption> = _primaryColorOption.asStateFlow()

    private val _customPrimaryColorArgb = MutableStateFlow(0xFF65A30DL)
    val customPrimaryColorArgb: StateFlow<Long> = _customPrimaryColorArgb.asStateFlow()

    private val _wallpaperSettings = MutableStateFlow(WallpaperSettings())
    val wallpaperSettings: StateFlow<WallpaperSettings> = _wallpaperSettings.asStateFlow()

    private val _fcmPushSettings = MutableStateFlow(FcmPushSettings())
    val fcmPushSettings: StateFlow<FcmPushSettings> = _fcmPushSettings.asStateFlow()

    private val _crashlyticsSettings = MutableStateFlow(CrashlyticsSettings())
    val crashlyticsSettings: StateFlow<CrashlyticsSettings> = _crashlyticsSettings.asStateFlow()

    private val _expertFeatureFlags = MutableStateFlow(ExpertFeatureFlagsSettings())
    val expertFeatureFlags: StateFlow<ExpertFeatureFlagsSettings> = _expertFeatureFlags.asStateFlow()

    private val _stabilityDiagnostics = MutableStateFlow<List<StabilityDiagnosticEntry>>(emptyList())
    val stabilityDiagnostics: StateFlow<List<StabilityDiagnosticEntry>> = _stabilityDiagnostics.asStateFlow()

    private val _householdFeatureEnabled = MutableStateFlow(false)
    val householdFeatureEnabled: StateFlow<Boolean> = _householdFeatureEnabled.asStateFlow()

    private val _customCategories = MutableStateFlow<List<String>>(emptyList())
    val customCategories: StateFlow<List<String>> = _customCategories.asStateFlow()

    private val _amoledDarkEnabled = MutableStateFlow(false)
    val amoledDarkEnabled: StateFlow<Boolean> = _amoledDarkEnabled.asStateFlow()

    private val _hapticFeedbackEnabled = MutableStateFlow(true)
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

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
            val savedPrimaryOption = prefs.getString(KEY_PRIMARY_COLOR_OPTION, AppPrimaryColorOption.KIWI_GREEN.name)
            _primaryColorOption.value = runCatching {
                AppPrimaryColorOption.valueOf(savedPrimaryOption ?: AppPrimaryColorOption.KIWI_GREEN.name)
            }.getOrDefault(AppPrimaryColorOption.KIWI_GREEN)
            _customPrimaryColorArgb.value = prefs.getLong(KEY_CUSTOM_PRIMARY_COLOR_ARGB, 0xFF65A30DL)
            _wallpaperSettings.value = decodeWallpaperSettings(prefs.getString(KEY_WALLPAPER_SETTINGS, null))
            _fcmPushSettings.value = decodeFcmSettings(prefs.getString(KEY_FCM_SETTINGS, null))
            _crashlyticsSettings.value = decodeCrashlyticsSettings(prefs.getString(KEY_CRASHLYTICS_SETTINGS, null))
            _expertFeatureFlags.value = decodeExpertFeatureFlags(prefs.getString(KEY_EXPERT_FEATURE_FLAGS, null))
            _stabilityDiagnostics.value = decodeStabilityDiagnostics(prefs.getString(KEY_STABILITY_DIAGNOSTICS, null))
                .ifEmpty { seedInitialStabilityBreadcrumbs() }
            _householdFeatureEnabled.value = prefs.getBoolean(KEY_HOUSEHOLD_FEATURE_ENABLED, false)
            _customCategories.value = decodeCustomCategories(prefs.getString(KEY_CUSTOM_CATEGORIES, null))
            _amoledDarkEnabled.value = prefs.getBoolean(KEY_AMOLED_DARK_ENABLED, false)
            _hapticFeedbackEnabled.value = prefs.getBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, true)
            initialized = true
        }
    }

    fun setHouseholdFeatureEnabled(context: Context, enabled: Boolean) {
        ensureInitialized(context)
        _householdFeatureEnabled.value = enabled
        prefs(context).edit().putBoolean(KEY_HOUSEHOLD_FEATURE_ENABLED, enabled).apply()
    }

    fun setAmoledDarkEnabled(context: Context, enabled: Boolean) {
        ensureInitialized(context)
        _amoledDarkEnabled.value = enabled
        prefs(context).edit().putBoolean(KEY_AMOLED_DARK_ENABLED, enabled).apply()
    }

    fun setHapticFeedbackEnabled(context: Context, enabled: Boolean) {
        ensureInitialized(context)
        _hapticFeedbackEnabled.value = enabled
        prefs(context).edit().putBoolean(KEY_HAPTIC_FEEDBACK_ENABLED, enabled).apply()
    }

    fun addCustomCategory(context: Context?, rawCategory: String) {
        val cleaned = rawCategory.trim()
        if (cleaned.length < 2) return
        if (context != null) ensureInitialized(context)
        val updated = (_customCategories.value + cleaned)
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        _customCategories.value = updated
        if (context != null) {
            val arr = JSONArray()
            updated.forEach { arr.put(it) }
            prefs(context).edit().putString(KEY_CUSTOM_CATEGORIES, arr.toString()).apply()
        }
    }

    private fun decodeCustomCategories(raw: String?): List<String> = runCatching {
        if (raw.isNullOrBlank()) return emptyList()
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val item = arr.optString(i, "").trim()
                if (item.isNotBlank()) add(item)
            }
        }.distinctBy { it.lowercase() }
    }.getOrDefault(emptyList())

    private fun seedInitialStabilityBreadcrumbs(): List<StabilityDiagnosticEntry> {
        val deviceLabel = "${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})"
        return listOf(
            StabilityDiagnosticEntry(
                id = "diag_init_runtime",
                timestampMillis = System.currentTimeMillis() - 60_000L,
                subsystem = "RUNTIME_HEALTH",
                severity = "INFO",
                message = "Crashlytics runtime diagnostics initialized.",
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
        }
        prefs(context).edit().putString(KEY_CRASHLYTICS_SETTINGS, obj.toString()).apply()
    }

    fun updateCrashlyticsSettings(context: Context, newSettings: CrashlyticsSettings) {
        setCrashlyticsSettings(context, newSettings)
    }

    fun setExpertFeatureFlags(context: Context, settings: ExpertFeatureFlagsSettings) {
        ensureInitialized(context)
        _expertFeatureFlags.value = settings
        val obj = JSONObject().apply {
            put("expertModeEnabled", settings.expertModeEnabled)
            put("crashAnalyticsFeatureEnabled", settings.crashAnalyticsFeatureEnabled)
            put("fcmPushDiagnosticsEnabled", settings.fcmPushDiagnosticsEnabled)
        }
        prefs(context).edit().putString(KEY_EXPERT_FEATURE_FLAGS, obj.toString()).apply()
    }

    fun updateExpertFeatureFlags(context: Context, newSettings: ExpertFeatureFlagsSettings) {
        setExpertFeatureFlags(context, newSettings)
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
                crashReportingEnabled = o.optBoolean("crashReportingEnabled", true)
            )
        }.getOrDefault(CrashlyticsSettings())
    }

    private fun decodeExpertFeatureFlags(raw: String?): ExpertFeatureFlagsSettings {
        if (raw.isNullOrBlank()) return ExpertFeatureFlagsSettings()
        return runCatching {
            val o = JSONObject(raw)
            ExpertFeatureFlagsSettings(
                expertModeEnabled = o.optBoolean("expertModeEnabled", false),
                crashAnalyticsFeatureEnabled = o.optBoolean("crashAnalyticsFeatureEnabled", true),
                fcmPushDiagnosticsEnabled = o.optBoolean("fcmPushDiagnosticsEnabled", true)
            )
        }.getOrDefault(ExpertFeatureFlagsSettings())
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

    fun setPrimaryColorOption(context: Context, option: AppPrimaryColorOption) {
        ensureInitialized(context)
        _primaryColorOption.value = option
        prefs(context).edit().putString(KEY_PRIMARY_COLOR_OPTION, option.name).apply()
    }

    fun setCustomPrimaryColor(context: Context, colorArgb: Long) {
        ensureInitialized(context)
        val normalizedArgb = colorArgb or 0xFF000000L
        _customPrimaryColorArgb.value = normalizedArgb
        _primaryColorOption.value = AppPrimaryColorOption.CUSTOM_COLOR
        prefs(context).edit()
            .putLong(KEY_CUSTOM_PRIMARY_COLOR_ARGB, normalizedArgb)
            .putString(KEY_PRIMARY_COLOR_OPTION, AppPrimaryColorOption.CUSTOM_COLOR.name)
            .apply()
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
            email = record.optString("email", "$username@onefamily.local")
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
        val email = "$username@onefamily.local"

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
                put("email", "test@onefamily.local")
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
            email = obj.optString("email", "test@onefamily.local")
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
    private const val STORE_PREFS = "onefamily_local_data_store_prefs"
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

    private val storeLock = Any()
    private val accountTimestampMap = mutableMapOf<String, Long>()

    private var appContext: Context? = null
    private var loaded = false

    fun ensureInitialized(context: Context) {
        appContext = context.applicationContext
        if (loaded) return
        synchronized(storeLock) {
            if (loaded) return
            val p = prefs() ?: return
            profilesFlow.value = decodeProfiles(p.getString(KEY_PROFILES, null))
            householdsFlow.value = decodeHouseholds(p.getString(KEY_HOUSEHOLDS, null))
            val rawExpenses = decodeExpenses(p.getString(KEY_EXPENSES, null))
            val validExpenses = rawExpenses.filter {
                it.userId == LocalAccountSessionManager.DEFAULT_TEST_UID ||
                    (!it.id.startsWith("mock_tx_") && !it.id.startsWith("demo_"))
            }
            val dedupedExpenses = deduplicateSmsExpenses(validExpenses)
            expensesFlow.value = dedupedExpenses
            if (dedupedExpenses.size != rawExpenses.size) {
                saveExpenses(dedupedExpenses)
            }
            budgetsFlow.value = decodeBudgets(p.getString(KEY_BUDGETS, null))
            accountsFlow.value = decodeAccounts(p.getString(KEY_ACCOUNTS, null))
            smsRawMapFlow.value = decodeSmsRawMap(p.getString(KEY_SMS_RAW_MAP, null))
            loaded = true
        }
    }

    fun getSmsRecordBySignature(signature: String): SmsRawRecord? {
        val clean = signature.trim().removePrefix("sms_")
        if (clean.isBlank()) return null
        val map = smsRawMapFlow.value
        return map[signature.trim()] ?: map[clean]
    }

    fun getAllExistingSmsSignatures(uid: String): Set<String> = synchronized(storeLock) {
        if (uid.isBlank()) return emptySet()
        val map = smsRawMapFlow.value
        expensesFlow.value
            .filter { it.userId == uid || (it.memberUids.contains(uid) && (uid == LocalAccountSessionManager.DEFAULT_TEST_UID || it.userId != LocalAccountSessionManager.DEFAULT_TEST_UID)) }
            .flatMap { tx ->
                val rawBody = com.example.data.sms.SmsInboxReader.extractRawSmsBodyFromNotes(tx.notes)
                val notesKeys = if (rawBody.isNotBlank() && tx.smsSignature.isNotBlank()) {
                    com.example.data.sms.SmsInboxReader.buildSmsAdjacentContentKeys(rawBody, tx.transactionDateMillis)
                } else {
                    emptyList()
                }
                val cachedKeys = (listOf(tx.smsSignature) + tx.linkedSmsSignatures)
                    .filter { it.isNotBlank() }
                    .mapNotNull { sig -> map[sig] ?: map[sig.removePrefix("sms_")] }
                    .flatMap { rec ->
                        com.example.data.sms.SmsInboxReader.buildSmsAdjacentContentKeys(rec.body, rec.timestamp)
                    }
                tx.linkedSmsSignatures + listOf(tx.smsSignature, tx.id) + notesKeys + cachedKeys
            }
            .filter { it.isNotBlank() }
            .flatMap { listOf(it, it.removePrefix("sms_")) }
            .toSet()
    }

    /**
     * Ensures the exact same SMS message (same smsSignature, linked duplicate signature, or
     * identical normalized SMS body + amount + type within a true 10-minute sliding window,
     * with zero fixed-bucket boundary splits) never produces duplicate transactions in the ledger.
     */
    fun deduplicateSmsExpenses(list: List<ExpenseTransaction>): List<ExpenseTransaction> {
        if (list.size <= 1) return list
        val result = mutableListOf<ExpenseTransaction>()
        val seenSignatures = mutableSetOf<String>()
        val seenSlidingBodyTimestamps = mutableMapOf<String, MutableList<Long>>()

        // Synthetic mock entries are preserved only for the demo Test User; real users never get synthetic entries
        val cleaned = list.filter {
            it.userId == LocalAccountSessionManager.DEFAULT_TEST_UID ||
                (!it.id.startsWith("mock_tx_") && !it.id.startsWith("demo_"))
        }

        // Pre-seed all linkedSmsSignatures from kept primary transactions so linked duplicates never resurface
        cleaned.forEach { tx ->
            tx.linkedSmsSignatures.forEach { linked ->
                val cleanLinked = linked.trim().removePrefix("sms_")
                if (cleanLinked.isNotBlank()) {
                    seenSignatures.add(cleanLinked)
                }
            }
        }

        for (tx in cleaned) {
            val sig = tx.smsSignature.trim().removePrefix("sms_")
            val cleanDocId = tx.id.trim().removePrefix("sms_")
            if (sig.isNotBlank()) {
                if (sig in seenSignatures || cleanDocId in seenSignatures) continue
                val rawSmsText = com.example.data.sms.SmsInboxReader.normalizeSmsBody(
                    com.example.data.sms.SmsInboxReader.extractRawSmsBodyFromNotes(tx.notes)
                )
                if (rawSmsText.length >= 15) {
                    val fpKey = "${tx.userId}|${tx.type}|${String.format(java.util.Locale.US, "%.2f", tx.amount)}|$rawSmsText"
                    val timestamps = seenSlidingBodyTimestamps[fpKey]
                    if (timestamps != null && timestamps.any { prevTs ->
                            kotlin.math.abs(tx.transactionDateMillis - prevTs) <= com.example.data.sms.SmsInboxReader.SLIDING_DEDUP_WINDOW_MS
                        }
                    ) {
                        continue
                    }
                    seenSlidingBodyTimestamps.getOrPut(fpKey) { mutableListOf() }.add(tx.transactionDateMillis)
                }
                seenSignatures.add(sig)
                if (cleanDocId.isNotBlank()) seenSignatures.add(cleanDocId)
            }
            result.add(tx)
        }
        return result
    }

    fun cacheSmsMessage(
        signature: String,
        sender: String,
        body: String,
        timestamp: Long,
        persistImmediately: Boolean = true
    ) {
        if (signature.isBlank() || body.isBlank()) return
        synchronized(storeLock) {
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
    }

    fun persistSmsRawCache() {
        synchronized(storeLock) {
            saveSmsRawMap(smsRawMapFlow.value)
        }
    }

    fun getSmsMessageForTransaction(tx: ExpenseTransaction): SmsRawRecord? {
        val sig = tx.smsSignature.ifBlank { tx.id.removePrefix("sms_") }
        if (sig.isNotBlank()) {
            smsRawMapFlow.value[sig]?.let { return it }
        }
        // Fallback if the raw SMS was stored in notes
        val notes = tx.notes.substringBefore("\n[Linked Duplicate:").substringBefore("[Linked Duplicate:").trim()
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
        } else if (currentProfiles[user.uid]?.personalMonthlyBudget == 35000.0) {
            val existingProf = currentProfiles[user.uid]!!
            currentProfiles[user.uid] = existingProf.copy(personalMonthlyBudget = 0.0)
            profilesFlow.value = currentProfiles
            saveProfiles(currentProfiles)
        }

        if (isTestUser) {
            seedMockDataForTestUser(user, defaultHouseholdId)
        }
    }

    private fun seedMockDataForTestUser(user: LocalAuthUser, householdId: String) {
        seedDefaultAccountsIfEmpty(user)

        // Seed realistic mock transactions across past 3 months for the Test/Demo user
        if (expensesFlow.value.none { it.userId == user.uid }) {
            val memberUids = listOf(user.uid)
            val mockList = mutableListOf<ExpenseTransaction>()
            val rawMapUpdates = smsRawMapFlow.value.toMutableMap()

            data class MockItemDef(
                val monthOffset: Int,
                val dayOfMonth: Int,
                val hour: Int,
                val title: String,
                val amount: Double,
                val type: String,
                val category: String,
                val accountName: String,
                val paymentMode: String,
                val isShared: Boolean,
                val sender: String,
                val smsBody: String,
                val ref: String
            )

            val mockDefs = listOf(
                // Month 0 (Current Month)
                MockItemDef(0, 1, 10, "Infosys Tech Ltd Salary", 85000.0, "INCOME", "Salary & Income", "HDFC Salary A/c", "BANK", true, "VM-HDFCBK", "Rs. 85,000.00 credited to your A/c XX4321 on 01-11-2026 by INFOSYS TECH LTD SALARY NEFT Ref 9911223344. Avl Bal: Rs. 1,02,450.00.", "9911223344"),
                MockItemDef(0, 3, 11, "Apartment Rent", 24000.0, "EXPENSE", "Rent & Housing", "HDFC Salary A/c", "BANK", true, "VM-HDFCBK", "Rs. 24,000.00 debited from A/c XX4321 on 03-11-2026 to LANDLORD RENT via NEFT Ref 7766554433. Avl Bal: Rs. 78,450.00.", "7766554433"),
                MockItemDef(0, 5, 16, "Nature's Basket Groceries", 3450.0, "EXPENSE", "Groceries", "HDFC Salary A/c", "UPI", true, "VM-HDFCBK", "Rs. 3,450.00 debited from A/c XX4321 on 05-11-2026 at Nature's Basket via UPI Ref 8877665544. Avl Bal: Rs. 75,000.00.", "8877665544"),
                MockItemDef(0, 7, 9, "Blue Tokai Coffee Roasters", 380.0, "EXPENSE", "Food & Dining", "ICICI Savings A/c", "UPI", false, "VM-ICICIB", "Rs. 380.00 debited from A/c XX9012 on 07-11-2026 at Blue Tokai Coffee via UPI Ref 3344556677. Avl Bal: Rs. 35,220.00.", "3344556677"),
                MockItemDef(0, 9, 14, "Shell Petrol Pump", 2800.0, "EXPENSE", "Fuel & Travel", "Axis Neo Credit Card", "CARD", true, "AX-AXISBK", "Alert: Rs. 2,800.00 spent on Axis Card 7788 at Shell Petrol Pump on 09-11-2026. Avail Limit: Rs. 1,47,200.00.", "TX778809"),
                MockItemDef(0, 11, 18, "Apollo Pharmacy", 850.0, "EXPENSE", "Health & Fitness", "HDFC Salary A/c", "UPI", true, "VM-HDFCBK", "Rs. 850.00 debited from A/c XX4321 on 11-11-2026 at Apollo Pharmacy via UPI Ref 1122334455. Avl Bal: Rs. 74,150.00.", "1122334455"),
                MockItemDef(0, 13, 20, "Swiggy Gourmet Dinner", 1240.0, "EXPENSE", "Food & Dining", "HDFC Salary A/c", "UPI", true, "VM-HDFCBK", "Rs. 1,240.00 debited from A/c XX4321 on 13-11-2026 at Swiggy via UPI Ref 4433221100. Avl Bal: Rs. 72,910.00.", "4433221100"),
                MockItemDef(0, 15, 12, "Tata Power Electricity Bill", 2100.0, "EXPENSE", "Bills & Utilities", "ICICI Savings A/c", "UPI", true, "VM-ICICIB", "Rs. 2,100.00 debited from A/c XX9012 on 15-11-2026 to Tata Power Electricity Bill via UPI Ref 5566778899. Avl Bal: Rs. 33,120.00.", "5566778899"),
                MockItemDef(0, 17, 17, "Zara Clothing & Apparel", 4500.0, "EXPENSE", "Shopping", "Axis Neo Credit Card", "CARD", false, "AX-AXISBK", "Alert: Rs. 4,500.00 spent on Axis Card 7788 at Zara Clothing on 17-11-2026. Avail Limit: Rs. 1,42,700.00.", "TX778817"),
                MockItemDef(0, 19, 15, "Local Bakery Treat", 450.0, "EXPENSE", "Food & Dining", "Cash Pocket", "CASH", false, "Cash", "Cash payment of Rs. 450.00 at Local Bakery.", "CSH019"),

                // Month -1 (Previous Month)
                MockItemDef(-1, 1, 10, "Infosys Tech Ltd Salary", 85000.0, "INCOME", "Salary & Income", "HDFC Salary A/c", "BANK", true, "VM-HDFCBK", "Rs. 85,000.00 credited to your A/c XX4321 on 01-10-2026 by INFOSYS TECH LTD SALARY NEFT Ref 9900112233. Avl Bal: Rs. 98,200.00.", "9900112233"),
                MockItemDef(-1, 3, 11, "Apartment Rent", 24000.0, "EXPENSE", "Rent & Housing", "HDFC Salary A/c", "BANK", true, "VM-HDFCBK", "Rs. 24,000.00 debited from A/c XX4321 on 03-10-2026 to LANDLORD RENT via NEFT Ref 7755443322. Avl Bal: Rs. 74,200.00.", "7755443322"),
                MockItemDef(-1, 6, 17, "BigBasket Monthly Grocery", 4200.0, "EXPENSE", "Groceries", "HDFC Salary A/c", "UPI", true, "VM-HDFCBK", "Rs. 4,200.00 debited from A/c XX4321 on 06-10-2026 at BigBasket via UPI Ref 8844332211. Avl Bal: Rs. 70,000.00.", "8844332211"),
                MockItemDef(-1, 8, 9, "Starbucks Coffee", 450.0, "EXPENSE", "Food & Dining", "ICICI Savings A/c", "UPI", false, "VM-ICICIB", "Rs. 450.00 debited from A/c XX9012 on 08-10-2026 at Starbucks via UPI Ref 3322110099. Avl Bal: Rs. 36,000.00.", "3322110099"),
                MockItemDef(-1, 12, 14, "HPCL Fuel Station", 2500.0, "EXPENSE", "Fuel & Travel", "Axis Neo Credit Card", "CARD", true, "AX-AXISBK", "Alert: Rs. 2,500.00 spent on Axis Card 7788 at HPCL Fuel Station on 12-10-2026. Avail Limit: Rs. 1,47,500.00.", "TX778812"),
                MockItemDef(-1, 15, 20, "Swiggy Weekend Family Meal", 1650.0, "EXPENSE", "Food & Dining", "HDFC Salary A/c", "UPI", true, "VM-HDFCBK", "Rs. 1,650.00 debited from A/c XX4321 on 15-10-2026 at Swiggy via UPI Ref 4411009988. Avl Bal: Rs. 68,350.00.", "4411009988"),
                MockItemDef(-1, 18, 12, "Airtel Fiber Broadband Bill", 1180.0, "EXPENSE", "Bills & Utilities", "ICICI Savings A/c", "UPI", true, "VM-ICICIB", "Rs. 1,180.00 debited from A/c XX9012 on 18-10-2026 to Airtel Fiber Bill via UPI Ref 5544332211. Avl Bal: Rs. 34,820.00.", "5544332211"),
                MockItemDef(-1, 22, 19, "PVR Cinemas Movie Tickets", 1400.0, "EXPENSE", "Entertainment", "Axis Neo Credit Card", "CARD", false, "AX-AXISBK", "Alert: Rs. 1,400.00 spent on Axis Card 7788 at PVR Cinemas on 22-10-2026. Avail Limit: Rs. 1,46,100.00.", "TX778822"),

                // Month -2 (Two Months Ago)
                MockItemDef(-2, 1, 10, "Infosys Tech Ltd Salary", 85000.0, "INCOME", "Salary & Income", "HDFC Salary A/c", "BANK", true, "VM-HDFCBK", "Rs. 85,000.00 credited to your A/c XX4321 on 01-09-2026 by INFOSYS TECH LTD SALARY NEFT Ref 9988776655. Avl Bal: Rs. 92,000.00.", "9988776655"),
                MockItemDef(-2, 3, 11, "Apartment Rent", 24000.0, "EXPENSE", "Rent & Housing", "HDFC Salary A/c", "BANK", true, "VM-HDFCBK", "Rs. 24,000.00 debited from A/c XX4321 on 03-09-2026 to LANDLORD RENT via NEFT Ref 7744332211. Avl Bal: Rs. 68,000.00.", "7744332211"),
                MockItemDef(-2, 7, 16, "Spencer's Retail Groceries", 3900.0, "EXPENSE", "Groceries", "HDFC Salary A/c", "UPI", true, "VM-HDFCBK", "Rs. 3,900.00 debited from A/c XX4321 on 07-09-2026 at Spencers Retail via UPI Ref 8833221100. Avl Bal: Rs. 64,100.00.", "8833221100"),
                MockItemDef(-2, 11, 13, "City Fuel Station", 2400.0, "EXPENSE", "Fuel & Travel", "Axis Neo Credit Card", "CARD", true, "AX-AXISBK", "Alert: Rs. 2,400.00 spent on Axis Card 7788 at City Fuel Station on 11-09-2026. Avail Limit: Rs. 1,47,600.00.", "TX778811"),
                MockItemDef(-2, 16, 11, "Family Dental Clinic", 1500.0, "EXPENSE", "Health & Fitness", "HDFC Salary A/c", "UPI", true, "VM-HDFCBK", "Rs. 1,500.00 debited from A/c XX4321 on 16-09-2026 at Dental Care via UPI Ref 1100998877. Avl Bal: Rs. 62,600.00.", "1100998877"),
                MockItemDef(-2, 20, 20, "Barbeque Nation Family Dinner", 2800.0, "EXPENSE", "Food & Dining", "HDFC Salary A/c", "CARD", true, "VM-HDFCBK", "Rs. 2,800.00 spent on HDFC Debit Card XX4321 at Barbeque Nation on 20-09-2026. Avl Bal: Rs. 59,800.00.", "TX432120")
            )

            mockDefs.forEachIndexed { idx, def ->
                val cal = Calendar.getInstance().apply {
                    if (def.monthOffset != 0) {
                        add(Calendar.MONTH, def.monthOffset)
                    }
                    val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                    set(Calendar.DAY_OF_MONTH, def.dayOfMonth.coerceAtMost(maxDay))
                    set(Calendar.HOUR_OF_DAY, def.hour)
                    set(Calendar.MINUTE, 25)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val txMillis = cal.timeInMillis
                val docId = "mock_tx_${idx + 1}"
                val sig = "mock_sms_sig_${idx + 1}"
                val targetHouseholdId = if (def.isShared) householdId else ""

                // Cache realistic raw bank SMS for each mock transaction so EditTransactionDialog shows the raw SMS
                rawMapUpdates[sig] = SmsRawRecord(
                    signature = sig,
                    sender = def.sender,
                    body = def.smsBody,
                    timestamp = txMillis
                )

                mockList.add(
                    ExpenseTransaction(
                        id = docId,
                        userId = user.uid,
                        userName = user.displayName,
                        householdId = targetHouseholdId,
                        title = def.title,
                        amount = def.amount,
                        type = def.type,
                        category = def.category,
                        accountName = def.accountName,
                        paymentMode = def.paymentMode,
                        tags = listOf("mock-sample", def.accountName.lowercase().replace(" ", "-")),
                        notes = "[${def.sender}] ${def.smsBody}",
                        visibility = if (def.isShared) "SHARED" else "PERSONAL",
                        smsSignature = sig,
                        referenceNumber = def.ref,
                        transactionDateMillis = txMillis,
                        memberUids = memberUids,
                        createdAt = Timestamp(Date(txMillis)),
                        updatedAt = Timestamp.now()
                    )
                )
            }

            smsRawMapFlow.value = rawMapUpdates
            saveSmsRawMap(rawMapUpdates)

            val updatedExpenses = (expensesFlow.value + mockList).sortedByDescending { it.transactionDateMillis }
            expensesFlow.value = updatedExpenses
            saveExpenses(updatedExpenses)
        }
    }

    private fun seedDefaultAccountsIfEmpty(user: LocalAuthUser) {
        if (accountsFlow.value.none { it.userId == user.uid }) {
            val defaultAccounts = listOf(
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
            accountsFlow.value = defaultAccounts
            saveAccounts(defaultAccounts)
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
            if (userId.isBlank()) {
                emptyList()
            } else {
                list.filter { it.userId == userId && it.visibility == "PERSONAL" }
                    .sortedByDescending { it.transactionDateMillis }
            }
        }

    fun observeSharedExpenses(householdId: String): Flow<List<ExpenseTransaction>> =
        expensesFlow.map { list ->
            if (householdId.isBlank()) {
                emptyList()
            } else {
                list.filter { it.householdId == householdId && it.visibility == "SHARED" }
                    .sortedByDescending { it.transactionDateMillis }
            }
        }

    fun observePersonalCategoryBudgets(userId: String): Flow<List<CategoryBudget>> =
        budgetsFlow.map { list ->
            if (userId.isBlank()) emptyList() else list.filter { it.userId == userId && it.householdId.isBlank() }
        }

    fun observeSharedCategoryBudgets(householdId: String): Flow<List<CategoryBudget>> =
        budgetsFlow.map { list ->
            if (householdId.isBlank()) emptyList() else list.filter { it.householdId == householdId }
        }

    fun observeAccounts(userId: String): Flow<List<FinancialAccount>> =
        accountsFlow.map { list ->
            if (userId.isBlank()) emptyList() else list.filter { it.userId == userId }
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
        val cleanCurrency = currencySymbol.trim().ifBlank { "₹" }.take(10)
        val current = profilesFlow.value.toMutableMap()
        val existing = current[uid] ?: ensureUserProfile(uid, "Test User", "")
        val updatedProfile = existing.copy(
            personalMonthlyBudget = personalMonthlyBudget.coerceAtLeast(0.0),
            currencySymbol = cleanCurrency,
            updatedAt = Timestamp.now()
        )
        current[uid] = updatedProfile
        profilesFlow.value = current
        saveProfiles(current)

        val hid = updatedProfile.activeHouseholdId
        if (hid.isNotBlank()) {
            val hMap = householdsFlow.value.toMutableMap()
            val curHh = hMap[hid]
            if (curHh != null && curHh.currencySymbol != cleanCurrency) {
                hMap[hid] = curHh.copy(currencySymbol = cleanCurrency, updatedAt = Timestamp.now())
                householdsFlow.value = hMap
                saveHouseholds(hMap)
            }
        }
    }

    fun updatePreferredCurrency(
        uid: String,
        currencySymbol: String,
        household: Household?
    ) {
        val cleanCurrency = currencySymbol.trim().ifBlank { "₹" }.take(10)
        val current = profilesFlow.value.toMutableMap()
        val existing = current[uid] ?: ensureUserProfile(uid, "Test User", "")
        current[uid] = existing.copy(
            currencySymbol = cleanCurrency,
            updatedAt = Timestamp.now()
        )
        profilesFlow.value = current
        saveProfiles(current)

        val targetHouseholdId = household?.id?.ifBlank { existing.activeHouseholdId } ?: existing.activeHouseholdId
        if (targetHouseholdId.isNotBlank()) {
            val hMap = householdsFlow.value.toMutableMap()
            val curHh = hMap[targetHouseholdId]
            if (curHh != null) {
                hMap[targetHouseholdId] = curHh.copy(
                    currencySymbol = cleanCurrency,
                    updatedAt = Timestamp.now()
                )
                householdsFlow.value = hMap
                saveHouseholds(hMap)
            }
        }
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

    fun updateHouseholdSettings(
        householdId: String,
        name: String,
        monthlyBudgetLimit: Double,
        currencySymbol: String? = null
    ) {
        val hMap = householdsFlow.value.toMutableMap()
        val current = hMap[householdId] ?: return
        val nextCurrency = currencySymbol?.trim()?.takeIf { it.isNotBlank() }?.take(10)
            ?: current.currencySymbol.ifBlank { "₹" }
        hMap[householdId] = current.copy(
            name = name.trim().ifBlank { "Family Budget" },
            monthlyBudgetLimit = monthlyBudgetLimit.coerceAtLeast(0.0),
            currencySymbol = nextCurrency,
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
            category = com.example.data.model.ExpenseCategories.normalizeCategory(category),
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

        synchronized(storeLock) {
            val withoutSameDocOrSig = expensesFlow.value.filterNot { existing ->
                existing.id == docId ||
                    (smsSignature.isNotBlank() && (
                        existing.smsSignature.removePrefix("sms_") == smsSignature.removePrefix("sms_") ||
                            existing.linkedSmsSignatures.any { it.removePrefix("sms_") == smsSignature.removePrefix("sms_") }
                        ))
            }
            val updated = deduplicateSmsExpenses(withoutSameDocOrSig + tx)
            expensesFlow.value = updated
            saveExpenses(updated)
        }
        return docId
    }

    fun batchImportSmsCandidates(
        uid: String,
        candidates: List<SmsCandidate>,
        userName: String,
        household: Household?,
        shareByDefault: Boolean = true
    ): Int = synchronized(storeLock) {
        if (candidates.isEmpty()) return 0
        val effectiveShared = shareByDefault && household != null
        val targetHouseholdId = if (effectiveShared) household!!.id else ""
        val memberUids = if (effectiveShared) (household!!.memberUids + uid).distinct() else listOf(uid)

        val sortedCandidates = candidates.sortedBy { it.parsed.timestamp }
        val currentMap = expensesFlow.value.associateBy { it.id }.toMutableMap()
        val alreadyLinkedSignatures = getAllExistingSmsSignatures(uid).toMutableSet()
        val existingBodyTimestamps = mutableMapOf<String, MutableList<Long>>()
        expensesFlow.value
            .filter { it.userId == uid && it.smsSignature.isNotBlank() }
            .forEach { existingTx ->
                val normBody = com.example.data.sms.SmsInboxReader.normalizeSmsBody(
                    com.example.data.sms.SmsInboxReader.extractRawSmsBodyFromNotes(existingTx.notes)
                )
                if (normBody.length >= 15) {
                    val key = "${existingTx.type}|${String.format(java.util.Locale.US, "%.2f", existingTx.amount)}|$normBody"
                    existingBodyTimestamps.getOrPut(key) { mutableListOf() }.add(existingTx.transactionDateMillis)
                }
            }
        val accountsToUpsert = mutableMapOf<String, SmsCandidate>()
        val rawMapUpdates = smsRawMapFlow.value.toMutableMap()
        var importedCount = 0

        for (candidate in sortedCandidates) {
            val contentKey = com.example.data.sms.SmsInboxReader.buildSmsContentKey(
                body = candidate.parsed.smsBody,
                timestamp = candidate.parsed.timestamp
            )
            val adjacentKeys = com.example.data.sms.SmsInboxReader.buildSmsAdjacentContentKeys(
                body = candidate.parsed.smsBody,
                timestamp = candidate.parsed.timestamp
            )
            val normBody = com.example.data.sms.SmsInboxReader.normalizeSmsBody(candidate.parsed.smsBody)
            val txTypeStr = if (candidate.parsed.type == TransactionType.INCOME) "INCOME" else "EXPENSE"
            val bodyKey = if (normBody.length >= 15) {
                "$txTypeStr|${String.format(java.util.Locale.US, "%.2f", candidate.parsed.amount.toDouble())}|$normBody"
            } else {
                ""
            }
            if (candidate.id in alreadyLinkedSignatures ||
                contentKey in alreadyLinkedSignatures ||
                adjacentKeys.any { it in alreadyLinkedSignatures }
            ) continue
            if (bodyKey.isNotEmpty() &&
                existingBodyTimestamps[bodyKey]?.any { prevTs ->
                    kotlin.math.abs(candidate.parsed.timestamp - prevTs) <= com.example.data.sms.SmsInboxReader.SLIDING_DEDUP_WINDOW_MS
                } == true
            ) continue

            alreadyLinkedSignatures.add(candidate.id)
            alreadyLinkedSignatures.add(contentKey)
            alreadyLinkedSignatures.addAll(adjacentKeys)
            if (bodyKey.isNotEmpty()) {
                existingBodyTimestamps.getOrPut(bodyKey) { mutableListOf() }.add(candidate.parsed.timestamp)
            }
            val parsed = candidate.parsed
            val isIncome = parsed.type == TransactionType.INCOME
            val category = com.example.data.model.ExpenseCategories.normalizeCategory(candidate.suggestedCategory)
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

            if (candidate.id.isNotBlank() && parsed.smsBody.isNotBlank()) {
                rawMapUpdates[candidate.id] = SmsRawRecord(
                    signature = candidate.id,
                    sender = parsed.sender,
                    body = parsed.smsBody,
                    timestamp = parsed.timestamp
                )
            }

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
                val prevCandidate = accountsToUpsert[candidate.suggestedAccountName]
                if (prevCandidate == null || parsed.timestamp >= prevCandidate.parsed.timestamp) {
                    accountsToUpsert[candidate.suggestedAccountName] = candidate
                }
            }
        }

        if (rawMapUpdates.size != smsRawMapFlow.value.size) {
            smsRawMapFlow.value = rawMapUpdates
            saveSmsRawMap(rawMapUpdates)
        }

        val updatedList = deduplicateSmsExpenses(currentMap.values.toList())
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
                isSharedVisible = effectiveShared,
                asOfTimestampMillis = parsed.timestamp
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
    ) = synchronized(storeLock) {
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
            category = com.example.data.model.ExpenseCategories.normalizeCategory(category),
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

    fun updateExpenseCategoryById(expenseId: String, newCategory: String) = synchronized(storeLock) {
        val updatedList = expensesFlow.value.map { tx ->
            if (tx.id == expenseId) {
                tx.copy(
                    category = com.example.data.model.ExpenseCategories.normalizeCategory(newCategory),
                    updatedAt = Timestamp.now()
                )
            } else {
                tx
            }
        }
        expensesFlow.value = updatedList
        saveExpenses(updatedList)
    }

    fun toggleExpensePrivacy(uid: String, expense: ExpenseTransaction, household: Household) = synchronized(storeLock) {
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

    fun deleteExpense(expenseId: String) = synchronized(storeLock) {
        val updatedList = expensesFlow.value.filterNot { it.id == expenseId }
        expensesFlow.value = updatedList
        saveExpenses(updatedList)
    }

    /**
     * Restores an exact [ExpenseTransaction] (used when undoing an accidental swipe delete,
     * swipe duplicate link, or privacy toggle on the Home Screen).
     */
    fun restoreExpense(expense: ExpenseTransaction) = synchronized(storeLock) {
        val withoutId = expensesFlow.value.filterNot { it.id == expense.id }
        val updatedList = (withoutId + expense.copy(updatedAt = Timestamp.now()))
            .sortedByDescending { it.transactionDateMillis }
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
    ): ExpenseTransaction = synchronized(storeLock) {
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
    ): String = synchronized(storeLock) {
        val effectiveShared = isShared && household != null
        val normalizedCat = com.example.data.model.ExpenseCategories.normalizeCategory(category)
        val sanitizedId = normalizedCat.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "unknown" }
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
            category = normalizedCat,
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

    fun deleteCategoryBudget(budgetId: String) = synchronized(storeLock) {
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
        isSharedVisible: Boolean = true,
        asOfTimestampMillis: Long = Long.MAX_VALUE
    ): String = synchronized(storeLock) {
        val docKey = "${uid}_${bankName}_${accountType}_${lastFourDigits}"
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(64)
            .ifBlank { UUID.randomUUID().toString().take(16) }

        val prevTimestamp = accountTimestampMap[docKey] ?: Long.MIN_VALUE
        if (asOfTimestampMillis != Long.MAX_VALUE && prevTimestamp != Long.MAX_VALUE && asOfTimestampMillis < prevTimestamp) {
            return docKey
        }
        accountTimestampMap[docKey] = asOfTimestampMillis

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

    fun deleteAccount(accountId: String) = synchronized(storeLock) {
        accountTimestampMap.remove(accountId)
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
                        category = com.example.data.model.ExpenseCategories.normalizeCategory(o.optString("category", "Unknown")),
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
                        category = com.example.data.model.ExpenseCategories.normalizeCategory(o.optString("category", "Unknown")),
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
