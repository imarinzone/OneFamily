package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.CrashlyticsSettings
import com.example.data.model.ExpertFeatureFlagsSettings
import com.example.data.model.FamilyCurrencyOption
import com.example.data.model.FcmPushSettings
import com.example.data.model.Household
import com.example.data.model.NotificationSubCategory
import com.example.data.model.WallpaperColorAccent
import com.example.data.model.WallpaperDensity
import com.example.data.model.WallpaperPatternStyle
import com.example.data.model.WallpaperSettings
import com.example.ui.components.AppTimePickerDialog
import com.example.ui.components.ExpressiveCutoutAppearanceHeroCard
import com.example.ui.components.FinanceWallpaperPreviewBanner
import com.example.ui.components.M3CheckmarkSwitch
import com.example.ui.components.MasterFeatureToggleBanner
import com.example.ui.components.PrimaryColorSelectorBar
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.SmsSyncPeriodExpressiveSlider
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AppPrimaryColorOption
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.groupedCardShape
import com.example.ui.theme.rememberAppHapticPerformer
import com.example.ui.viewmodel.OneFamilyDashboardUiState
import com.example.ui.viewmodel.SmsSyncPeriodOption
import kotlin.math.roundToInt

enum class SettingsCategory(
    val title: String,
    val icon: ImageVector
) {
    ACCOUNT("Account", Icons.Outlined.AccountCircle),
    APPEARANCE("Appearance", Icons.Outlined.Palette),
    FAMILY("Family Household", Icons.Outlined.Groups),
    NOTIFICATIONS("Notifications", Icons.Outlined.Notifications),
    SMS_SYNC("SMS Sync", Icons.Default.Sync),
    CURRENCY_DATA("Currency & Export", Icons.Default.CurrencyExchange),
    ADVANCED("Advanced", Icons.Outlined.Tune)
}

@Composable
internal fun SettingsCategoryListPage(
    displayName: String,
    currentUserEmail: String,
    isDarkActive: Boolean,
    householdFeatureEnabled: Boolean,
    activeHouseholdName: String?,
    smsSyncPeriodOption: SmsSyncPeriodOption,
    smsSyncCustomRange: Pair<Long, Long>?,
    currency: String,
    expertModeEnabled: Boolean,
    onSelectCategory: (SettingsCategory) -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val categories = SettingsCategory.entries

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_category_list"),
        contentPadding = PaddingValues(top = 10.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = dimens.horizontalPadding)
                    .padding(top = 6.dp, bottom = 18.dp)
                    .testTag("settings_hub_title")
            )
        }

        itemsIndexed(categories, key = { _, category -> category.name }) { index, category ->
            val subtitle = when (category) {
                SettingsCategory.ACCOUNT -> if (currentUserEmail.isNotBlank()) {
                    "$displayName · $currentUserEmail"
                } else {
                    "Profile, sign out, and account data"
                }
                SettingsCategory.APPEARANCE -> if (isDarkActive) {
                    "Dark theme, colors, and wallpaper"
                } else {
                    "Light theme, colors, and wallpaper"
                }
                SettingsCategory.FAMILY -> if (householdFeatureEnabled && !activeHouseholdName.isNullOrBlank()) {
                    "$activeHouseholdName · Shared ledger & members"
                } else {
                    "Shared family ledger, invites, and budgets"
                }
                SettingsCategory.NOTIFICATIONS -> "Alerts, summary timings, and limit threshold"
                SettingsCategory.SMS_SYNC -> "${smsSyncPeriodOption.formatRangeSummary(smsSyncCustomRange)} · Bank & UPI receipts"
                SettingsCategory.CURRENCY_DATA -> "Preferred currency ($currency) and CSV export"
                SettingsCategory.ADVANCED -> if (expertModeEnabled) {
                    "Crash analytics and diagnostics enabled"
                } else {
                    "Crash analytics and diagnostics"
                }
            }

            Card(
                onClick = {
                    performHaptic()
                    onSelectCategory(category)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                    .testTag("settings_category_row_${category.name.lowercase()}"),
                shape = groupedCardShape(index = index, count = categories.size),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.title,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun SettingsCategoryDetailHeader(
    title: String,
    onBackClick: () -> Unit
) {
    val performHaptic = rememberAppHapticPerformer()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                performHaptic()
                onBackClick()
            },
            modifier = Modifier
                .size(48.dp)
                .testTag("settings_category_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("settings_category_page_title")
        )
    }
}

@Composable
internal fun AccountSettingsPage(
    displayName: String,
    currentUserEmail: String,
    onUpdateUserName: (String) -> Unit,
    onSignOutClick: () -> Unit,
    isDeletingAccount: Boolean,
    onRequestDeleteAccount: () -> Unit,
    onBackClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    var editableUserName by remember(displayName) { mutableStateOf(displayName) }

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCategoryDetailHeader(title = SettingsCategory.ACCOUNT.title, onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("household_tab_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile & User Info
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .testTag("account_sharing_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Outlined.Person,
                                tint = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentDescription = "Profile"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Profile & User Info",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentUserEmail.isNotBlank()) "$displayName • $currentUserEmail" else displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        val userNameError = validateDisplayName(editableUserName)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editableUserName,
                                onValueChange = { editableUserName = it.take(60) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                isError = userNameError != null,
                                supportingText = userNameError?.let { err -> { Text(err) } },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_user_name_input")
                            )
                            Button(
                                onClick = {
                                    if (validateDisplayName(editableUserName) == null) {
                                        performHaptic()
                                        onUpdateUserName(editableUserName.trim())
                                    }
                                },
                                enabled = userNameError == null && editableUserName.trim() != displayName,
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("save_user_name_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save Name",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Logout Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                tint = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Logout",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sign out of this account on device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                performHaptic()
                                onSignOutClick()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Logout")
                        }
                    }
                }
            }

            // Delete Account & All Data Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.32f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.24f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.DeleteForever,
                                tint = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Text(
                                    text = "Delete Account & Data",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Permanently erase all Firebase & local data and remove account",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                performHaptic()
                                onRequestDeleteAccount()
                            },
                            enabled = !isDeletingAccount,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("delete_account_and_data_button")
                        ) {
                            if (isDeletingAccount) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onError
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Deleting...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "Delete Account and All Data",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AppearanceSettingsPage(
    isDarkActive: Boolean,
    currentThemeMode: AppThemeMode,
    currentPrimaryOption: AppPrimaryColorOption,
    customPrimaryColorArgb: Long,
    amoledDarkEnabled: Boolean,
    hapticFeedbackEnabled: Boolean,
    wallpaperSettings: WallpaperSettings,
    onBackClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val context = LocalContext.current
    val performHaptic = rememberAppHapticPerformer()

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCategoryDetailHeader(title = SettingsCategory.APPEARANCE.title, onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("household_tab_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                ExpressiveCutoutAppearanceHeroCard(
                    isDarkActive = isDarkActive,
                    amoledEnabled = amoledDarkEnabled,
                    onToggleQuickTheme = {
                        val next = if (isDarkActive) AppThemeMode.LIGHT else AppThemeMode.DARK
                        LocalAccountSessionManager.setThemeMode(context, next)
                    },
                    modifier = Modifier
                        .padding(horizontal = dimens.horizontalPadding)
                        .padding(bottom = 12.dp)
                )
            }

            // Visual Style Card 1 of 3: App Theme, Primary Palette & Finance Wallpaper
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                        .testTag("theme_and_wallpaper_card"),
                    shape = groupedCardShape(index = 0, count = 3),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                SquircleIconBadge(
                                    icon = Icons.Outlined.Palette,
                                    tint = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                                Column {
                                    Text(
                                        text = "App Theme & Wallpaper",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Light, dark, and accent style",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            M3CheckmarkSwitch(
                                checked = wallpaperSettings.enabled,
                                onCheckedChange = { enabled ->
                                    LocalAccountSessionManager.updateWallpaperSettings(
                                        context = context,
                                        newSettings = wallpaperSettings.copy(enabled = enabled)
                                    )
                                },
                                modifier = Modifier.testTag("wallpaper_enable_switch")
                            )
                        }

                        ThemeModeSelectorBar(
                            currentMode = currentThemeMode,
                            onSelectMode = {
                                performHaptic()
                                LocalAccountSessionManager.setThemeMode(context, it)
                            }
                        )

                        PrimaryColorSelectorBar(
                            selectedOption = currentPrimaryOption,
                            customPrimaryColorArgb = customPrimaryColorArgb,
                            onSelectOption = {
                                performHaptic()
                                LocalAccountSessionManager.setPrimaryColorOption(context, it)
                            },
                            onSelectCustomColorArgb = {
                                performHaptic()
                                LocalAccountSessionManager.setCustomPrimaryColor(context, it)
                            }
                        )

                        ReceiptDashedDividerLine()

                        FinanceWallpaperPreviewBanner(settings = wallpaperSettings)

                        AnimatedVisibility(visible = wallpaperSettings.enabled) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    WallpaperPatternStyle.entries.forEach { style ->
                                        val symbol = when (style) {
                                            WallpaperPatternStyle.WHATSAPP_DOODLES -> "💳 🧾"
                                            WallpaperPatternStyle.FINANCE_ICONS_AND_EMOJIS -> "💰 📊"
                                            WallpaperPatternStyle.MINIMAL_GEOMETRIC -> "❖ ◇"
                                        }
                                        FilterChip(
                                            selected = wallpaperSettings.style == style,
                                            onClick = {
                                                performHaptic()
                                                LocalAccountSessionManager.updateWallpaperSettings(
                                                    context = context,
                                                    newSettings = wallpaperSettings.copy(style = style)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = symbol,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            modifier = Modifier.testTag("wallpaper_style_chip_${style.name.lowercase()}")
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    WallpaperColorAccent.entries.forEach { accent ->
                                        val isSelected = wallpaperSettings.colorAccent == accent
                                        val swatchColor = when (accent) {
                                            WallpaperColorAccent.THEME_ADAPTIVE -> MaterialTheme.colorScheme.primary
                                            WallpaperColorAccent.EMERALD_MINT -> Color(0xFF10B981)
                                            WallpaperColorAccent.ROYAL_GOLD -> Color(0xFFF59E0B)
                                            WallpaperColorAccent.OCEAN_SLATE -> Color(0xFF0EA5E9)
                                            WallpaperColorAccent.WARM_CORAL -> Color(0xFFFF5A5F)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor.copy(alpha = if (isSelected) 1f else 0.72f))
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    performHaptic()
                                                    LocalAccountSessionManager.updateWallpaperSettings(
                                                        context = context,
                                                        newSettings = wallpaperSettings.copy(colorAccent = accent)
                                                    )
                                                }
                                                .testTag("wallpaper_accent_chip_${accent.name.lowercase()}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = accent.label,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    WallpaperDensity.entries.forEach { density ->
                                        val densityIcon = when (density) {
                                            WallpaperDensity.COMPACT -> Icons.Default.Apps
                                            WallpaperDensity.BALANCED -> Icons.Default.GridOn
                                            WallpaperDensity.AIRY -> Icons.Default.Window
                                        }
                                        FilterChip(
                                            selected = wallpaperSettings.density == density,
                                            onClick = {
                                                performHaptic()
                                                LocalAccountSessionManager.updateWallpaperSettings(
                                                    context = context,
                                                    newSettings = wallpaperSettings.copy(density = density)
                                                )
                                            },
                                            label = {
                                                Icon(
                                                    imageVector = densityIcon,
                                                    contentDescription = density.label,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            modifier = Modifier.testTag("wallpaper_density_chip_${density.name.lowercase()}")
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Slider(
                                        value = wallpaperSettings.opacityPercent.toFloat(),
                                        onValueChange = { newVal ->
                                            LocalAccountSessionManager.updateWallpaperSettings(
                                                context = context,
                                                newSettings = wallpaperSettings.copy(opacityPercent = newVal.roundToInt().coerceIn(4, 28))
                                            )
                                        },
                                        valueRange = 4f..28f,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("wallpaper_opacity_slider")
                                    )
                                    Text(
                                        text = "${wallpaperSettings.opacityPercent}%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Visual Style Card 2 of 3: AMOLED Mode
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                        .testTag("amoled_mode_card"),
                    shape = groupedCardShape(index = 1, count = 3),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Outlined.DarkMode,
                                tint = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                            Column {
                                Text(
                                    text = "AMOLED Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pure black background for OLED screens",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        M3CheckmarkSwitch(
                            checked = amoledDarkEnabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.setAmoledDarkEnabled(context, enabled)
                            },
                            modifier = Modifier.testTag("amoled_mode_switch")
                        )
                    }
                }
            }

            // Visual Style Card 3 of 3: Haptic Feedback
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                        .testTag("haptic_feedback_card"),
                    shape = groupedCardShape(index = 2, count = 3),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Outlined.Vibration,
                                tint = MaterialTheme.colorScheme.tertiary,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                            Column {
                                Text(
                                    text = "Haptic Feedback",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Subtle vibrations on gestures & switches",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        M3CheckmarkSwitch(
                            checked = hapticFeedbackEnabled,
                            onCheckedChange = { enabled ->
                                LocalAccountSessionManager.setHapticFeedbackEnabled(context, enabled)
                            },
                            modifier = Modifier.testTag("haptic_feedback_switch")
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FamilyHouseholdSettingsPage(
    uiState: OneFamilyDashboardUiState,
    currency: String,
    householdFeatureEnabled: Boolean,
    clipboardManager: ClipboardManager,
    onCreateHousehold: (String, Double) -> Unit,
    onJoinByCode: (String) -> Unit,
    onInviteByEmail: (String) -> Unit,
    onAcceptEmailInvite: (Household) -> Unit,
    onBackClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val context = LocalContext.current
    val performHaptic = rememberAppHapticPerformer()

    var newHouseholdName by remember { mutableStateOf("Our Family Home") }
    var newHouseholdLimit by remember { mutableStateOf("") }
    var joinCodeInput by remember { mutableStateOf("") }
    var inviteEmailInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCategoryDetailHeader(title = SettingsCategory.FAMILY.title, onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("household_tab_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                MasterFeatureToggleBanner(
                    title = "Family Household",
                    subtitle = "Enable shared family ledger, invites & budgets",
                    checked = householdFeatureEnabled,
                    onCheckedChange = { enabled ->
                        LocalAccountSessionManager.setHouseholdFeatureEnabled(context, enabled)
                    },
                    leadingIcon = Icons.Outlined.Groups,
                    switchTestTag = "household_feature_enable_switch",
                    modifier = Modifier
                        .padding(horizontal = dimens.horizontalPadding)
                        .testTag("household_feature_settings_card")
                )
            }

            if (householdFeatureEnabled) {
                val household = uiState.household

                if (household == null) {
                    if (uiState.pendingEmailInvites.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = dimens.horizontalPadding),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(dimens.cardPadding)) {
                                    uiState.pendingEmailInvites.forEach { inv ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = inv.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Button(
                                                onClick = { onAcceptEmailInvite(inv) },
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text("Join")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Box(modifier = Modifier.padding(horizontal = dimens.horizontalPadding)) {
                            ReceiptTicketCard(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(dimens.cardPadding)
                                        .testTag("household_create_card")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SquircleIconBadge(
                                            icon = Icons.Default.Groups,
                                            tint = MaterialTheme.colorScheme.primary,
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            size = 40.dp,
                                            iconSize = 20.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Create Household",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    ReceiptDashedDividerLine()
                                    val hhNameError = validateDisplayName(newHouseholdName)
                                    val hhLimitError = validateNonNegativeAmount(newHouseholdLimit)
                                    OutlinedTextField(
                                        value = newHouseholdName,
                                        onValueChange = { newHouseholdName = it.take(60) },
                                        label = { Text("Household Name") },
                                        isError = hhNameError != null,
                                        supportingText = hhNameError?.let { err -> { Text(err) } },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("create_household_name_input")
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = newHouseholdLimit,
                                        onValueChange = { newHouseholdLimit = sanitizeDecimalInput(it) },
                                        label = { Text("Monthly Budget ($currency)") },
                                        placeholder = { Text("Optional") },
                                        isError = hhLimitError != null,
                                        supportingText = hhLimitError?.let { err -> { Text(err) } },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("create_household_limit_input")
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            if (hhNameError == null && hhLimitError == null) {
                                                performHaptic()
                                                val limit = newHouseholdLimit.trim().toDoubleOrNull() ?: 0.0
                                                onCreateHousehold(newHouseholdName.trim(), limit)
                                            }
                                        },
                                        enabled = hhNameError == null && hhLimitError == null,
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("create_household_submit_button")
                                    ) {
                                        Text("Create")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimens.horizontalPadding)
                                .testTag("household_join_code_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(dimens.cardPadding)) {
                                Text(
                                    text = "Join with Code",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                val joinCodeError = if (joinCodeInput.isNotEmpty()) {
                                    validateHouseholdJoinCode(joinCodeInput)
                                } else {
                                    null
                                }
                                OutlinedTextField(
                                    value = joinCodeInput,
                                    onValueChange = {
                                        joinCodeInput = it.filter { ch -> ch.isLetterOrDigit() }.uppercase().take(6)
                                    },
                                    label = { Text("6-Character Invite Code") },
                                    isError = joinCodeError != null,
                                    supportingText = joinCodeError?.let { err -> { Text(err) } },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("join_household_code_input")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (validateHouseholdJoinCode(joinCodeInput) == null) {
                                            performHaptic()
                                            onJoinByCode(joinCodeInput.trim())
                                        }
                                    },
                                    enabled = validateHouseholdJoinCode(joinCodeInput) == null,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("join_household_submit_button")
                                ) {
                                    Text("Join")
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Box(modifier = Modifier.padding(horizontal = dimens.horizontalPadding)) {
                            ReceiptTicketCard(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(dimens.cardPadding)
                                        .testTag("household_active_details_card")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = household.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (household.monthlyBudgetLimit > 0.0) {
                                            Text(
                                                text = formatCurrency(household.monthlyBudgetLimit, currency),
                                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    ReceiptDashedDividerLine()

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = household.joinCode,
                                                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                            OutlinedButton(
                                                onClick = {
                                                    performHaptic()
                                                    clipboardManager.setText(AnnotatedString(household.joinCode))
                                                },
                                                shape = RoundedCornerShape(50),
                                                modifier = Modifier.testTag("copy_join_code_button")
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Copy")
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val inviteEmailError = if (inviteEmailInput.isNotEmpty()) {
                                        validateEmailAddress(inviteEmailInput)
                                    } else {
                                        null
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = inviteEmailInput,
                                            onValueChange = { inviteEmailInput = it.trim().take(120) },
                                            label = { Text("Email Address") },
                                            isError = inviteEmailError != null,
                                            supportingText = inviteEmailError?.let { err -> { Text(err) } },
                                            leadingIcon = {
                                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("invite_member_email_input")
                                        )
                                        Button(
                                            onClick = {
                                                if (validateEmailAddress(inviteEmailInput) == null) {
                                                    performHaptic()
                                                    onInviteByEmail(inviteEmailInput.trim())
                                                    inviteEmailInput = ""
                                                }
                                            },
                                            enabled = validateEmailAddress(inviteEmailInput) == null,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .height(54.dp)
                                                .testTag("send_email_invite_button")
                                        ) {
                                            Text("Invite")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimens.horizontalPadding)
                                .testTag("household_members_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(dimens.cardPadding)) {
                                Text(
                                    text = "Members",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                household.memberUids.forEach { uid ->
                                    val name = household.memberNames[uid] ?: "Member"
                                    val memberSharedSpend = uiState.sharedExpenses
                                        .filter { it.userId == uid && !it.type.equals("INCOME", ignoreCase = true) }
                                        .sumOf { it.amount }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(HouseholdBadgeIndigo.copy(alpha = 0.16f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = name.take(1).uppercase(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = HouseholdBadgeIndigo,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = formatCurrency(memberSharedSpend, currency),
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NotificationsSettingsPage(
    fcmPushSettings: FcmPushSettings,
    onBackClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val context = LocalContext.current
    val performHaptic = rememberAppHapticPerformer()

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCategoryDetailHeader(title = SettingsCategory.NOTIFICATIONS.title, onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("household_tab_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Card 1 of 6: Instant Transaction & Limit Alerts
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                        .testTag("notification_preferences_card"),
                    shape = groupedCardShape(index = 0, count = 6),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.NotificationsActive,
                                tint = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentDescription = "Notifications"
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Alerts, spend report schedules & push sync",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        val instantAlertCategories = listOf(
                            NotificationSubCategory.EXPENSES,
                            NotificationSubCategory.INCOME,
                            NotificationSubCategory.CREDIT_CARD_LIMIT
                        )
                        instantAlertCategories.forEach { subCategory ->
                            val isEnabled = fcmPushSettings.isSubCategoryEnabled(subCategory)
                            val subTag = subCategory.name.lowercase()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        performHaptic()
                                        LocalAccountSessionManager.updateFcmPushSettings(
                                            context = context,
                                            newSettings = fcmPushSettings.withSubCategoryEnabled(subCategory, !isEnabled)
                                        )
                                    }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                    .testTag("notif_subcategory_row_$subTag"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = subCategory.shortName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subCategory.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                M3CheckmarkSwitch(
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        performHaptic()
                                        LocalAccountSessionManager.updateFcmPushSettings(
                                            context = context,
                                            newSettings = fcmPushSettings.withSubCategoryEnabled(subCategory, checked)
                                        )
                                    },
                                    modifier = Modifier.testTag("notif_subcategory_switch_$subTag")
                                )
                            }
                        }
                    }
                }
            }

            // Cards 2–4 of 6: Daily, Weekly (with End-of-Week Day), and Monthly (End-of-Month) Spend Reports
            val summaryCategories = listOf(
                NotificationSubCategory.DAILY_SUMMARY,
                NotificationSubCategory.WEEKLY_SUMMARY,
                NotificationSubCategory.MONTHLY_SUMMARY
            )
            summaryCategories.forEachIndexed { summaryIdx, subCategory ->
                item(key = "notif_summary_${subCategory.name}") {
                    val isEnabled = fcmPushSettings.isSubCategoryEnabled(subCategory)
                    val subTag = subCategory.name.lowercase()
                    val (curHour, curMinute) = fcmPushSettings.getSummaryTime(subCategory)
                    var showSummaryTimePicker by remember(subCategory) { mutableStateOf(false) }
                    val scheduleSubtitle = when (subCategory) {
                        NotificationSubCategory.WEEKLY_SUMMARY ->
                            "Every ${fcmPushSettings.formatWeeklySummaryDayOfWeek()} (end of week)"
                        NotificationSubCategory.MONTHLY_SUMMARY ->
                            "End of every month (last calendar day)"
                        else -> "Daily • Tap to open time picker"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp),
                        shape = groupedCardShape(index = summaryIdx + 1, count = 6),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimens.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        performHaptic()
                                        LocalAccountSessionManager.updateFcmPushSettings(
                                            context = context,
                                            newSettings = fcmPushSettings.withSubCategoryEnabled(subCategory, !isEnabled)
                                        )
                                    }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                    .testTag("notif_subcategory_row_$subTag"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = subCategory.shortName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subCategory.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                M3CheckmarkSwitch(
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        performHaptic()
                                        LocalAccountSessionManager.updateFcmPushSettings(
                                            context = context,
                                            newSettings = fcmPushSettings.withSubCategoryEnabled(subCategory, checked)
                                        )
                                    },
                                    modifier = Modifier.testTag("notif_subcategory_switch_$subTag")
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .testTag("notif_category_timing_panel_$subTag")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                performHaptic()
                                                showSummaryTimePicker = true
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                            .testTag("notif_category_pick_time_button_$subTag"),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "Scheduled notification time",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Text(
                                                    text = "Scheduled Time",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = scheduleSubtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = fcmPushSettings.formatSummaryTime(subCategory),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = ReceiptMonoFontFamily
                                                    ),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.testTag("notif_category_time_value_$subTag")
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Change scheduled time",
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (subCategory == NotificationSubCategory.WEEKLY_SUMMARY) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
                                                .testTag("notif_weekly_day_selector"),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "End-of-Week Day",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = fcmPushSettings.formatWeeklySummaryDayOfWeek(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.testTag("notif_weekly_selected_day_label")
                                                )
                                            }
                                            @OptIn(ExperimentalLayoutApi::class)
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                FcmPushSettings.WEEKLY_DAY_OPTIONS.forEach { (calDay, shortLabel, _) ->
                                                    val selectedDay = fcmPushSettings.weeklySummaryDayOfWeek == calDay
                                                    Surface(
                                                        onClick = {
                                                            performHaptic()
                                                            LocalAccountSessionManager.updateFcmPushSettings(
                                                                context = context,
                                                                newSettings = fcmPushSettings.withWeeklySummaryDayOfWeek(calDay)
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(50),
                                                        color = if (selectedDay) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                                        },
                                                        contentColor = if (selectedDay) {
                                                            MaterialTheme.colorScheme.onPrimary
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        },
                                                        modifier = Modifier
                                                            .heightIn(min = 38.dp)
                                                            .testTag("notif_weekly_day_chip_${shortLabel.lowercase()}")
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = shortLabel,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = if (selectedDay) FontWeight.Bold else FontWeight.Medium,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (subCategory == NotificationSubCategory.MONTHLY_SUMMARY) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
                                                .testTag("notif_monthly_end_of_month_badge"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Auto-adjusts for 28, 29, 30, or 31-day months",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            if (showSummaryTimePicker) {
                                AppTimePickerDialog(
                                    initialHour = curHour,
                                    initialMinute = curMinute,
                                    title = "${subCategory.shortName} Time",
                                    is24Hour = false,
                                    onDismiss = { showSummaryTimePicker = false },
                                    onConfirmTime = { pickedHour, pickedMinute ->
                                        performHaptic()
                                        LocalAccountSessionManager.updateFcmPushSettings(
                                            context = context,
                                            newSettings = fcmPushSettings.withSummaryTime(
                                                subCategory = subCategory,
                                                hour = pickedHour,
                                                minute = pickedMinute
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Card 5 of 6: FCM Push Notifications
            item {
                val fcmPushNotifCategories = listOf(
                    NotificationSubCategory.FAMILY_SYNC,
                    NotificationSubCategory.BUDGET_LIMITS
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                        .testTag("fcm_push_settings_card"),
                    shape = groupedCardShape(index = 4, count = 6),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "FCM Push Notifications",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        fcmPushNotifCategories.forEach { subCategory ->
                            val isEnabled = fcmPushSettings.isSubCategoryEnabled(subCategory)
                            val subTag = subCategory.name.lowercase()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        performHaptic()
                                        LocalAccountSessionManager.updateFcmPushSettings(
                                            context = context,
                                            newSettings = fcmPushSettings.withSubCategoryEnabled(subCategory, !isEnabled)
                                        )
                                    }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                    .testTag("notif_subcategory_row_$subTag"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = subCategory.shortName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = subCategory.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                M3CheckmarkSwitch(
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        performHaptic()
                                        LocalAccountSessionManager.updateFcmPushSettings(
                                            context = context,
                                            newSettings = fcmPushSettings.withSubCategoryEnabled(subCategory, checked)
                                        )
                                    },
                                    modifier = Modifier.testTag("notif_subcategory_switch_$subTag")
                                )
                            }
                        }
                    }
                }
            }

            // Card 6 of 6: Card Limit Alert Threshold
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                        .testTag("notif_credit_threshold_card"),
                    shape = groupedCardShape(index = 5, count = 6),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding)
                            .testTag("notif_credit_threshold_section"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Card Limit Alert Threshold",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Alert when a card reaches this % of its spend limit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${fcmPushSettings.creditCardAlertThresholdPercent}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("notif_credit_threshold_value")
                                )
                            }
                        }

                        Slider(
                            value = fcmPushSettings.creditCardAlertThresholdPercent.toFloat().coerceIn(50f, 100f),
                            onValueChange = { raw ->
                                val rounded = (raw / 5f).roundToInt() * 5
                                LocalAccountSessionManager.updateFcmPushSettings(
                                    context = context,
                                    newSettings = fcmPushSettings.copy(creditCardAlertThresholdPercent = rounded.coerceIn(50, 100))
                                )
                            },
                            valueRange = 50f..100f,
                            steps = 9,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notif_credit_threshold_slider")
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SmsSyncSettingsPage(
    smsSyncPeriodOption: SmsSyncPeriodOption,
    smsSyncCustomRange: Pair<Long, Long>?,
    onSelectSmsSyncPeriod: (SmsSyncPeriodOption) -> Unit,
    onSelectSmsSyncCustomRange: ((Long, Long) -> Unit)? = null,
    onOpenCustomRangePicker: () -> Unit,
    onScanSmsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCategoryDetailHeader(title = SettingsCategory.SMS_SYNC.title, onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("household_tab_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .testTag("settings_sms_sync_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.Sync,
                                tint = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentDescription = "SMS Sync"
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SMS Inbox Sync",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Auto-detect Bank & UPI SMS receipts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        SmsSyncPeriodExpressiveSlider(
                            selectedPeriod = smsSyncPeriodOption,
                            customRange = smsSyncCustomRange,
                            onSelectPeriod = onSelectSmsSyncPeriod,
                            onOpenCustomRangePicker = onOpenCustomRangePicker,
                            onUpdateCustomRange = onSelectSmsSyncCustomRange,
                            containerTag = "settings_sms_sync_period_selector",
                            selectedLabelTag = "settings_sms_sync_selected_period_label",
                            sliderTag = "settings_sms_period_slider",
                            chipTagPrefix = "settings_sms_period_chip"
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                performHaptic()
                                onScanSmsClick()
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("scan_sms_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sync SMS Now",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CurrencyAndExportSettingsPage(
    currency: String,
    onUpdatePreferredCurrency: (String) -> Unit,
    onExportCsvClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    var customCurrencyInput by remember(currency) { mutableStateOf(currency) }

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCategoryDetailHeader(title = SettingsCategory.CURRENCY_DATA.title, onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("household_tab_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Card 1 of 2: Preferred Currency
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                        .testTag("family_currency_settings_card"),
                    shape = groupedCardShape(index = 0, count = 2),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SquircleIconBadge(
                                    icon = Icons.Default.CurrencyExchange,
                                    tint = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentDescription = "Currency"
                                )
                                Text(
                                    text = "Preferred Currency",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.testTag("active_currency_badge")
                            ) {
                                Text(
                                    text = currency,
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FamilyCurrencyOption.entries.forEach { option ->
                                val isSelected = currency == option.symbol
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        performHaptic()
                                        customCurrencyInput = option.symbol
                                        onUpdatePreferredCurrency(option.symbol)
                                    },
                                    label = {
                                        Text(
                                            text = option.symbol,
                                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = ReceiptMonoFontFamily),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    },
                                    modifier = Modifier.testTag("currency_chip_${option.code.lowercase()}")
                                )
                            }
                        }

                        val currencyError = validateCurrencySymbol(customCurrencyInput)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customCurrencyInput,
                                onValueChange = { customCurrencyInput = it.take(6) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                placeholder = { Text("₹ $ € £") },
                                isError = currencyError != null,
                                supportingText = currencyError?.let { err -> { Text(err) } },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_currency_input")
                            )
                            Button(
                                onClick = {
                                    if (validateCurrencySymbol(customCurrencyInput) == null) {
                                        performHaptic()
                                        onUpdatePreferredCurrency(customCurrencyInput.trim())
                                    }
                                },
                                enabled = currencyError == null && customCurrencyInput.trim() != currency,
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("save_custom_currency_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Apply Currency",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card 2 of 2: Export Transactions (CSV)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding, vertical = 1.5.dp)
                        .testTag("export_csv_settings_card"),
                    shape = groupedCardShape(index = 1, count = 2),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Outlined.Download,
                                tint = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentDescription = "Export CSV"
                            )
                            Column {
                                Text(
                                    text = "Export Transactions (CSV)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Download by FY, year, month, or date range",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = {
                                performHaptic()
                                onExportCsvClick()
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("settings_export_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export CSV",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CSV")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AdvancedSettingsPage(
    expertFeatureFlags: ExpertFeatureFlagsSettings,
    crashlyticsSettings: CrashlyticsSettings,
    onBackClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        SettingsCategoryDetailHeader(title = SettingsCategory.ADVANCED.title, onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("household_tab_list"),
            contentPadding = PaddingValues(top = 6.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.horizontalPadding)
                        .animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        .testTag("expert_feature_flag_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                SquircleIconBadge(
                                    icon = Icons.Outlined.Tune,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentDescription = "Advanced Settings"
                                )
                                Column {
                                    Text(
                                        text = "Advanced Settings",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Crash Analytics & Diagnostics",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            M3CheckmarkSwitch(
                                checked = expertFeatureFlags.expertModeEnabled,
                                onCheckedChange = { enabled ->
                                    LocalAccountSessionManager.updateExpertFeatureFlags(
                                        context = context,
                                        newSettings = expertFeatureFlags.copy(expertModeEnabled = enabled)
                                    )
                                },
                                modifier = Modifier.testTag("expert_feature_flag_switch")
                            )
                        }

                        AnimatedVisibility(visible = expertFeatureFlags.expertModeEnabled) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ReceiptDashedDividerLine()

                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("crashlytics_settings_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = "Crashlytics Reporting",
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Crash Analytics",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.BugReport,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Crashlytics Crash Reporting",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            M3CheckmarkSwitch(
                                                checked = crashlyticsSettings.crashReportingEnabled,
                                                onCheckedChange = { enabled ->
                                                    LocalAccountSessionManager.updateCrashlyticsSettings(
                                                        context = context,
                                                        newSettings = crashlyticsSettings.copy(crashReportingEnabled = enabled)
                                                    )
                                                },
                                                modifier = Modifier.testTag("crashlytics_enabled_switch")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
