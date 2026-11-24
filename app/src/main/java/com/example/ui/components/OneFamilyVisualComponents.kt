package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.data.model.Household
import com.example.data.model.WallpaperColorAccent
import com.example.data.model.WallpaperDensity
import com.example.data.model.WallpaperPatternStyle
import com.example.data.model.WallpaperSettings
import com.example.ui.theme.AppPrimaryColorOption
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.PersonalBadgeTeal
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.categoryAccentColor
import com.example.ui.theme.rememberAppHapticPerformer
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.MonthlySpendBar
import com.example.ui.viewmodel.SmsSyncPeriodOption
import com.example.ui.viewmodel.YearMonthPeriod
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

fun formatCurrency(amount: Double, symbol: String = "₹"): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        minimumFractionDigits = 0
    }
    return "$symbol${formatter.format(amount)}"
}

fun formatCompactCurrency(amount: Double, symbol: String = "₹"): String {
    return when {
        amount >= 100000 -> String.format(Locale.US, "%s%.1fL", symbol, amount / 100000.0)
        amount >= 1000 -> String.format(Locale.US, "%s%.1fk", symbol, amount / 1000.0)
        amount > 0 -> String.format(Locale.US, "%s%.0f", symbol, amount)
        else -> "${symbol}0"
    }
}

fun formatShortDate(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))
}

fun categoryIcon(category: String): ImageVector {
    val lower = category.trim().lowercase()
    return when {
        lower.isBlank() || lower == "unknown" || lower == "general" || lower == "uncategorized" -> Icons.AutoMirrored.Filled.HelpOutline
        lower.contains("grocer") -> Icons.Default.LocalGroceryStore
        lower.contains("food") || lower.contains("dining") -> Icons.Default.Fastfood
        lower.contains("rent") || lower.contains("housing") -> Icons.Default.Home
        lower.contains("bill") || lower.contains("util") -> Icons.Default.Bolt
        lower.contains("fuel") || lower.contains("travel") || lower.contains("transport") -> Icons.Default.DirectionsCar
        lower.contains("shop") -> Icons.Default.ShoppingBag
        lower.contains("health") || lower.contains("medical") || lower.contains("fitness") -> Icons.Default.MedicalServices
        lower.contains("entertain") -> Icons.Default.Movie
        lower.contains("invest") -> Icons.AutoMirrored.Filled.TrendingUp
        lower.contains("cashback") || lower.contains("cash back") -> Icons.Default.CardGiftcard
        lower == "income" -> Icons.Default.AccountBalanceWallet
        lower.contains("salary") || lower.contains("income") -> Icons.Default.Payments
        else -> Icons.Default.Category
    }
}

// ============================================================================
// M3 EXPRESSIVE + PAPER RECEIPT SHARED BUILDING BLOCKS
// ============================================================================

/**
 * 44dp circular tonal IconButton with 48dp accessible touch target, inspired by
 * M3 Expressive top-bar action pills ("Your Mix" / "Queue" references).
 */
@Composable
fun CircularTonalIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val performHaptic = rememberAppHapticPerformer()
    Surface(
        onClick = {
            performHaptic()
            onClick()
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(48.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun rememberCustomCategoryEmojis(): Map<String, String> =
    LocalAccountSessionManager.customCategoryEmojis.collectAsStateWithLifecycle().value

@Composable
fun CategoryBadgeGlyph(
    category: String,
    tint: Color,
    iconSize: Dp = 20.dp,
    customEmoji: String? = rememberCustomCategoryEmojis()[category.trim().lowercase()],
    contentDescription: String? = null
) {
    val cleanEmoji = customEmoji?.trim().orEmpty()
    if (cleanEmoji.isNotEmpty()) {
        Text(
            text = cleanEmoji,
            fontSize = (iconSize.value * 0.95f).sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    } else {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * 16dp squircle pastel-tonal icon badge used in connected M3 card stacks.
 */
@Composable
fun SquircleIconBadge(
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp,
    emoji: String? = null,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        val cleanEmoji = emoji?.trim().orEmpty()
        if (cleanEmoji.isNotEmpty()) {
            Text(
                text = cleanEmoji,
                fontSize = (iconSize.value * 0.96f).sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Oversized M3 Expressive Editorial Header with optional compact count pill
 * and top-right circular tonal action buttons.
 */
@Composable
fun EditorialScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    countPillText: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!countPillText.isNullOrBlank() || !subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!countPillText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = countPillText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

/**
 * Perforated Thermal Receipt Section Divider:
 * Tracked uppercase title + inline perforated dotted rule + right count badge pill
 * ("RECEIPTS • • • • • • • [28]").
 */
@Composable
fun PerforatedSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    countBadge: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val dimens = LocalAppDimens.current
    val dotColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
            ) {
                if (size.width > 8f) {
                    drawLine(
                        color = dotColor,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 9f), 0f)
                    )
                }
            }
        }
        if (!countBadge.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = countBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

/**
 * Compact Material 3 Expressive Sort Selector Pill + Dropdown Menu for Transaction Lists.
 * Supports sorting by Amount (Low to High / High to Low) and Date (Newest / Oldest).
 */
@Composable
fun TransactionSortSelectorButton(
    selectedSortOrder: String,
    onSelectSortOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "tx_sort"
) {
    val performHaptic = rememberAppHapticPerformer()
    var menuExpanded by remember { mutableStateOf(false) }

    val normalizedSort = selectedSortOrder.uppercase(Locale.US)
    val isAmountSort = normalizedSort == "LOWEST" || normalizedSort == "HIGHEST"
    val isCustomSort = normalizedSort != "NEWEST"

    val pillLabel = when (normalizedSort) {
        "LOWEST" -> "Low → High"
        "HIGHEST" -> "High → Low"
        "OLDEST" -> "Oldest"
        else -> "Sort"
    }
    val pillIcon = when (normalizedSort) {
        "LOWEST" -> Icons.Default.ArrowUpward
        "HIGHEST" -> Icons.Default.ArrowDownward
        "OLDEST" -> Icons.Default.Schedule
        else -> Icons.Default.SwapVert
    }
    val containerColor = when {
        isAmountSort -> MaterialTheme.colorScheme.primaryContainer
        isCustomSort -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = when {
        isAmountSort -> MaterialTheme.colorScheme.onPrimaryContainer
        isCustomSort -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(modifier = modifier) {
        Surface(
            onClick = {
                performHaptic()
                menuExpanded = true
            },
            shape = RoundedCornerShape(50),
            color = containerColor,
            contentColor = contentColor,
            border = if (isAmountSort) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            } else {
                null
            },
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("${testTagPrefix}_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = pillIcon,
                    contentDescription = "Sort transactions",
                    tint = contentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = pillLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.testTag("${testTagPrefix}_menu")
        ) {
            val options = listOf(
                Triple("LOWEST", "Amount: Low to High", Icons.Default.ArrowUpward),
                Triple("HIGHEST", "Amount: High to Low", Icons.Default.ArrowDownward),
                Triple("NEWEST", "Date: Newest First", Icons.Default.Schedule),
                Triple("OLDEST", "Date: Oldest First", Icons.Default.Schedule)
            )
            options.forEach { (key, label, icon) ->
                val isSelected = normalizedSort == key
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        performHaptic()
                        menuExpanded = false
                        onSelectSortOrder(key)
                    },
                    modifier = Modifier.testTag("${testTagPrefix}_option_${key.lowercase(Locale.US)}")
                )
            }
        }
    }
}

/**
 * Material 3 Switch with a crisp Check icon inside the thumb when enabled.
 */
@Composable
fun M3CheckmarkSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val performHaptic = rememberAppHapticPerformer()
    Switch(
        checked = checked,
        onCheckedChange = { next ->
            performHaptic()
            onCheckedChange(next)
        },
        enabled = enabled,
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        } else {
            null
        },
        modifier = modifier
    )
}

/**
 * Full-width M3 stadium master toggle banner (inspired by ViperFX "Enabled" banner).
 */
@Composable
fun MasterFeatureToggleBanner(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    switchTestTag: String? = null
) {
    val performHaptic = rememberAppHapticPerformer()
    val bgColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "master_banner_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "master_banner_fg"
    )

    Surface(
        onClick = {
            performHaptic()
            onCheckedChange(!checked)
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = bgColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (leadingIcon != null) {
                    SquircleIconBadge(
                        icon = leadingIcon,
                        tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = if (checked) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        size = 44.dp,
                        iconSize = 22.dp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.78f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            M3CheckmarkSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = if (switchTestTag != null) Modifier.testTag(switchTestTag) else Modifier
            )
        }
    }
}

/**
 * Minimal appearance summary card with a single-tap Light / Dark circular toggle button.
 */
@Composable
fun ExpressiveCutoutAppearanceHeroCard(
    isDarkActive: Boolean,
    amoledEnabled: Boolean,
    onToggleQuickTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val performHaptic = rememberAppHapticPerformer()
    val statusText = when {
        isDarkActive && amoledEnabled -> "Dark mode · AMOLED"
        isDarkActive -> "Dark mode"
        else -> "Light mode"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("appearance_cutout_hero_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                onClick = {
                    performHaptic()
                    onToggleQuickTheme()
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("appearance_cutout_theme_emblem"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isDarkActive) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = if (isDarkActive) "Switch to light theme" else "Switch to dark theme",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun buildSmoothBezierPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val controlX = (p0.x + p1.x) / 2f
        path.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
    }
    return path
}

private fun buildSmoothAreaPath(points: List<Offset>, baselineY: Float): Path {
    val path = buildSmoothBezierPath(points)
    if (points.size >= 2) {
        path.lineTo(points.last().x, baselineY)
        path.lineTo(points.first().x, baselineY)
        path.close()
    }
    return path
}

// ============================================================================
// CORE LEDGER, CHART & RECEIPT VISUAL COMPONENTS
// ============================================================================

@Composable
fun ScopeSwitcherBar(
    selectedScope: BudgetScope,
    @Suppress("UNUSED_PARAMETER") hasHousehold: Boolean,
    householdName: String?,
    onScopeSelected: (BudgetScope) -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding, vertical = dimens.itemSpacing * 0.35f),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BudgetScope.entries.forEach { scope ->
                val isSelected = selectedScope == scope
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        Color.Transparent
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "scope_bg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "scope_fg"
                )

                val label = if (scope == BudgetScope.HOUSEHOLD_SHARED && !householdName.isNullOrBlank()) {
                    householdName
                } else {
                    scope.label
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .clickable {
                            performHaptic()
                            onScopeSelected(scope)
                        }
                        .padding(vertical = 10.dp)
                        .testTag("scope_chip_${scope.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                isSelected -> Icons.Default.Check
                                scope == BudgetScope.HOUSEHOLD_SHARED -> Icons.Default.Groups
                                else -> Icons.Default.Lock
                            },
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Converts a local timestamp to UTC midnight millis matching the same local calendar date,
 * as expected by Material 3 `rememberDatePickerState` and `rememberDateRangePickerState`.
 */
fun localMillisToUtcPickerDateMillis(localMillis: Long): Long {
    val localCal = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            localCal.get(Calendar.YEAR),
            localCal.get(Calendar.MONTH),
            localCal.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Converts a Material 3 UTC midnight `selectedDateMillis` into local start-of-day (`00:00:00.000`)
 * or end-of-day (`23:59:59.999`) preserving the exact calendar date chosen by the user.
 */
fun utcPickerDateMillisToLocalDayMillis(utcDateMillis: Long, endOfDay: Boolean = false): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcDateMillis }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
        if (endOfDay) {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        } else {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onConfirmDate: (year: Int, monthZeroBased: Int, dayOfMonth: Int, localStartOfDayMillis: Long) -> Unit,
    confirmLabel: String = "Apply"
) {
    val initialUtcMillis = remember(initialDateMillis) {
        localMillisToUtcPickerDateMillis(initialDateMillis)
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialUtcMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.testTag("app_date_picker_dialog"),
        confirmButton = {
            Button(
                onClick = {
                    val selectedUtc = datePickerState.selectedDateMillis ?: initialUtcMillis
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = selectedUtc
                    }
                    val year = utcCal.get(Calendar.YEAR)
                    val month = utcCal.get(Calendar.MONTH)
                    val day = utcCal.get(Calendar.DAY_OF_MONTH)
                    val localStart = utcPickerDateMillisToLocalDayMillis(selectedUtc, endOfDay = false)
                    onConfirmDate(year, month, day, localStart)
                    onDismiss()
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("date_picker_confirm_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("date_picker_dismiss_button")
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    title: String = "Select Time",
    is24Hour: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmTime: (hourOfDay: Int, minute: Int) -> Unit,
    confirmLabel: String = "Apply"
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour.coerceIn(0, 23),
        initialMinute = initialMinute.coerceIn(0, 59),
        is24Hour = is24Hour
    )
    var useKeyboardInput by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.testTag("app_time_picker_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { useKeyboardInput = !useKeyboardInput },
                    modifier = Modifier.testTag("time_picker_mode_toggle_button")
                ) {
                    Icon(
                        imageVector = if (useKeyboardInput) Icons.Default.Schedule else Icons.Default.Keyboard,
                        contentDescription = if (useKeyboardInput) "Switch to clock dial" else "Switch to keyboard input",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmTime(timePickerState.hour, timePickerState.minute)
                    onDismiss()
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("time_picker_confirm_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("time_picker_dismiss_button")
            ) {
                Text("Cancel")
            }
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (useKeyboardInput) {
                    TimeInput(state = timePickerState)
                } else {
                    TimePicker(state = timePickerState)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerModal(
    initialStartMillis: Long? = null,
    initialEndMillis: Long? = null,
    onDismiss: () -> Unit,
    onApplyRange: (Long, Long) -> Unit
) {
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMillis?.let { localMillisToUtcPickerDateMillis(it) },
        initialSelectedEndDateMillis = initialEndMillis?.let { localMillisToUtcPickerDateMillis(it) }
    )
    val shortDateFmt = remember {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val compactMonthDayFmt = remember {
        SimpleDateFormat("MMM d", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val startUtc = pickerState.selectedStartDateMillis
    val endUtc = pickerState.selectedEndDateMillis
    val headlineText = remember(startUtc, endUtc) {
        when {
            startUtc != null && endUtc != null -> {
                val lo = minOf(startUtc, endUtc)
                val hi = maxOf(startUtc, endUtc)
                val startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = lo }
                val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = hi }
                if (startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR)) {
                    "${compactMonthDayFmt.format(Date(lo))} – ${shortDateFmt.format(Date(hi))}"
                } else {
                    "${shortDateFmt.format(Date(lo))} – ${shortDateFmt.format(Date(hi))}"
                }
            }
            startUtc != null -> "${shortDateFmt.format(Date(startUtc))} – End date"
            else -> "Start date – End date"
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .widthIn(max = 420.dp)
            .testTag("custom_date_range_picker_dialog"),
        confirmButton = {
            Button(
                onClick = {
                    val chosenStartUtc = pickerState.selectedStartDateMillis
                    val chosenEndUtc = pickerState.selectedEndDateMillis ?: chosenStartUtc
                    if (chosenStartUtc != null && chosenEndUtc != null) {
                        val loUtc = minOf(chosenStartUtc, chosenEndUtc)
                        val hiUtc = maxOf(chosenStartUtc, chosenEndUtc)
                        val localStart = utcPickerDateMillisToLocalDayMillis(loUtc, endOfDay = false)
                        val localEnd = utcPickerDateMillisToLocalDayMillis(hiUtc, endOfDay = true)
                        onApplyRange(localStart, localEnd)
                    }
                    onDismiss()
                },
                enabled = pickerState.selectedStartDateMillis != null,
                modifier = Modifier.testTag("confirm_custom_date_range_button")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = pickerState,
            title = {
                Text(
                    text = "Select date range",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 2.dp)
                )
            },
            headline = {
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 8.dp, top = 2.dp, bottom = 10.dp)
                        .testTag("custom_date_range_headline")
                )
            },
            showModeToggle = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 380.dp, max = 500.dp)
        )
    }
}

/**
 * Material 3 Expressive Slider for choosing the SMS Sync fetch window
 * (`24h`, `This Month`, `30d`, `3m`, `6m`, `FY`, `1y`, `All`, `Custom`).
 * Features a thick segmented expressive track with inner stop ticks, a bold vertical
 * pill handle, direct stop pills, and native M3 Date Range + Time pickers for Custom Range.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSyncPeriodExpressiveSlider(
    selectedPeriod: SmsSyncPeriodOption,
    customRange: Pair<Long, Long>?,
    onSelectPeriod: (SmsSyncPeriodOption) -> Unit,
    onOpenCustomRangePicker: () -> Unit,
    onUpdateCustomRange: ((Long, Long) -> Unit)? = null,
    containerTag: String = "sms_sync_period_selector",
    selectedLabelTag: String = "sms_sync_selected_period_label",
    sliderTag: String = "sms_period_slider",
    chipTagPrefix: String = "sms_period_chip"
) {
    val performHaptic = rememberAppHapticPerformer()
    val options = SmsSyncPeriodOption.entries
    val selectedIndex = options.indexOf(selectedPeriod).coerceAtLeast(0)

    var showStartTimePicker by rememberSaveable { mutableStateOf(false) }
    var showEndTimePicker by rememberSaveable { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
    val inactiveTickColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.45f)
    val haloColor = MaterialTheme.colorScheme.surfaceContainerLow

    val animatedFraction by animateFloatAsState(
        targetValue = if (options.size > 1) selectedIndex.toFloat() / options.lastIndex.toFloat() else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sms_period_slider_fraction"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(containerTag),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header row + expressive tonal readout pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Fetch SMS From",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Fetch SMS From",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        if (selectedPeriod == SmsSyncPeriodOption.CUSTOM) {
                            performHaptic()
                            onOpenCustomRangePicker()
                        }
                    }
                    .testTag(selectedLabelTag)
            ) {
                Text(
                    text = selectedPeriod.formatRangeSummary(customRange),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Material 3 Expressive Thick Segmented Track + Vertical Pill Handle Slider
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { raw ->
                val snappedIdx = raw.roundToInt().coerceIn(0, options.lastIndex)
                val targetOption = options[snappedIdx]
                if (targetOption != selectedPeriod) {
                    performHaptic()
                    onSelectPeriod(targetOption)
                }
            },
            onValueChangeFinished = {
                if (selectedPeriod == SmsSyncPeriodOption.CUSTOM && customRange == null) {
                    onOpenCustomRangePicker()
                }
            },
            valueRange = 0f..options.lastIndex.toFloat(),
            steps = (options.size - 2).coerceAtLeast(0),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 12.dp, height = 34.dp)
                            .clip(RoundedCornerShape(50))
                            .background(haloColor)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 5.dp, height = 30.dp)
                            .clip(RoundedCornerShape(50))
                            .background(primaryColor)
                    )
                }
            },
            track = { _ ->
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                ) {
                    val trackHeightPx = 16.dp.toPx()
                    val topY = (size.height - trackHeightPx) / 2f
                    val outerRadius = trackHeightPx / 2f
                    val thumbGapPx = 7.dp.toPx()
                    val frac = animatedFraction.coerceIn(0f, 1f)
                    val splitX = (size.width * frac).coerceIn(0f, size.width)

                    val activeRight = (splitX - thumbGapPx).coerceAtLeast(0f)
                    if (activeRight > 2f) {
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(0f, topY),
                            size = Size(activeRight, trackHeightPx),
                            cornerRadius = CornerRadius(outerRadius, outerRadius)
                        )
                    }

                    val inactiveLeft = (splitX + thumbGapPx).coerceAtMost(size.width)
                    val inactiveWidth = (size.width - inactiveLeft).coerceAtLeast(0f)
                    if (inactiveWidth > 2f) {
                        drawRoundRect(
                            color = inactiveTrackColor,
                            topLeft = Offset(inactiveLeft, topY),
                            size = Size(inactiveWidth, trackHeightPx),
                            cornerRadius = CornerRadius(outerRadius, outerRadius)
                        )
                    }

                    // Expressive discrete stop dots inside the thick track
                    val stopCount = options.size
                    val dotRadius = 2.2.dp.toPx()
                    val insetX = outerRadius * 0.85f
                    val usableWidth = (size.width - insetX * 2f).coerceAtLeast(1f)
                    for (i in 0 until stopCount) {
                        val stopFrac = if (stopCount > 1) i.toFloat() / (stopCount - 1).toFloat() else 0f
                        val cx = insetX + usableWidth * stopFrac
                        if (kotlin.math.abs(cx - splitX) > thumbGapPx + dotRadius) {
                            drawCircle(
                                color = if (cx < splitX) onPrimaryColor.copy(alpha = 0.5f) else inactiveTickColor,
                                radius = dotRadius,
                                center = Offset(cx, size.height / 2f)
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 42.dp)
                .testTag(sliderTag)
        )

        // Expressive stop selector pills for instant 1-tap selection & clear stop labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = selectedPeriod == option
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "sms_stop_pill_bg_${option.name}"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "sms_stop_pill_fg_${option.name}"
                )
                Surface(
                    onClick = {
                        performHaptic()
                        if (option == SmsSyncPeriodOption.CUSTOM) {
                            onOpenCustomRangePicker()
                        } else {
                            onSelectPeriod(option)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = containerColor,
                    contentColor = contentColor,
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .testTag("${chipTagPrefix}_${option.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (option == SmsSyncPeriodOption.CUSTOM) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = option.shortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Custom Date & Time Picker Controls when Custom Range is active
        if (selectedPeriod == SmsSyncPeriodOption.CUSTOM) {
            val activeStart = customRange?.first ?: (System.currentTimeMillis() - 24L * 3_600_000L)
            val activeEnd = customRange?.second ?: System.currentTimeMillis()
            val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${chipTagPrefix}_custom_datetime_bar")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            performHaptic()
                            onOpenCustomRangePicker()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("${chipTagPrefix}_pick_custom_dates_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick custom date range",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (customRange != null) {
                                "Change Dates • ${selectedPeriod.formatRangeSummary(customRange)}"
                            } else {
                                "Pick Custom Date Range"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (onUpdateCustomRange != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    performHaptic()
                                    showStartTimePicker = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("${chipTagPrefix}_start_time_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Select start time",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "From ${timeFmt.format(Date(activeStart))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    performHaptic()
                                    showEndTimePicker = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("${chipTagPrefix}_end_time_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Select end time",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "To ${timeFmt.format(Date(activeEnd))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            if (showStartTimePicker && onUpdateCustomRange != null) {
                val cal = remember(activeStart) { Calendar.getInstance().apply { timeInMillis = activeStart } }
                AppTimePickerDialog(
                    initialHour = cal.get(Calendar.HOUR_OF_DAY),
                    initialMinute = cal.get(Calendar.MINUTE),
                    title = "Select Start Time",
                    onDismiss = { showStartTimePicker = false },
                    onConfirmTime = { hour, minute ->
                        val updatedStart = Calendar.getInstance().apply {
                            timeInMillis = activeStart
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        onUpdateCustomRange(updatedStart, activeEnd.coerceAtLeast(updatedStart))
                    }
                )
            }

            if (showEndTimePicker && onUpdateCustomRange != null) {
                val cal = remember(activeEnd) { Calendar.getInstance().apply { timeInMillis = activeEnd } }
                AppTimePickerDialog(
                    initialHour = cal.get(Calendar.HOUR_OF_DAY),
                    initialMinute = cal.get(Calendar.MINUTE),
                    title = "Select End Time",
                    onDismiss = { showEndTimePicker = false },
                    onConfirmTime = { hour, minute ->
                        val updatedEnd = Calendar.getInstance().apply {
                            timeInMillis = activeEnd
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.timeInMillis
                        onUpdateCustomRange(activeStart.coerceAtMost(updatedEnd), updatedEnd)
                    }
                )
            }
        }

        // Clear assurance note that rechecking ignores previous check time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Rechecks all SMS in this period regardless of when last checked.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthPeriodSelectorBar(
    selectedPeriod: YearMonthPeriod?,
    periodLabel: String,
    onStepMonth: (Int) -> Unit,
    onSelectCurrentMonth: () -> Unit,
    onToggleAllTime: () -> Unit = {},
    selectedDateRangePreset: DateRangePreset = if (selectedPeriod == null) DateRangePreset.ALL_TIME else DateRangePreset.MONTH,
    onSelectDateRangePreset: ((DateRangePreset) -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") onSelectMonthPeriod: ((YearMonthPeriod) -> Unit)? = null,
    customStartDateMillis: Long? = null,
    customEndDateMillis: Long? = null,
    onSelectCustomDateRange: ((Long, Long) -> Unit)? = null,
    showDateRangeChips: Boolean = false
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val activePeriod = selectedPeriod ?: YearMonthPeriod.current()

    var showCustomRangePicker by rememberSaveable { mutableStateOf(false) }
    var dragAccumulated by remember { mutableFloatStateOf(0f) }
    var barWidthPx by remember { mutableFloatStateOf(1f) }
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragAccumulated * 0.35f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "gesture_bar_drag_offset"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.6f)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(activePeriod) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccumulated = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulated += dragAmount
                        },
                        onDragCancel = { dragAccumulated = 0f },
                        onDragEnd = {
                            val swipeThreshold = barWidthPx * 0.10f
                            if (dragAccumulated > swipeThreshold) {
                                performHaptic()
                                onStepMonth(-1)
                            } else if (dragAccumulated < -swipeThreshold) {
                                performHaptic()
                                onStepMonth(1)
                            }
                            dragAccumulated = 0f
                        }
                    )
                }
                .testTag("top_month_switcher_bar")
        ) {
            val prevPeriod = activePeriod.previousMonth()
            val nextPeriod = activePeriod.nextMonth()
            val topYearLabel = if (selectedDateRangePreset == DateRangePreset.MONTH) {
                activePeriod.year.toString()
            } else {
                selectedDateRangePreset.label
            }
            val bottomMonthLabel = if (selectedDateRangePreset == DateRangePreset.MONTH) {
                activePeriod.formatFullMonth()
            } else {
                periodLabel
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Stationary Previous Arrow + Text-Only Animated Period
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                performHaptic()
                                onStepMonth(-1)
                            }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                            .testTag("prev_month_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        AnimatedContent(
                            targetState = prevPeriod,
                            transitionSpec = {
                                val movingForward = targetState >= initialState
                                val enterSlide = slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    initialOffsetX = { w -> if (movingForward) w / 2 else -w / 2 }
                                ) + fadeIn()
                                val exitSlide = slideOutHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    targetOffsetX = { w -> if (movingForward) -w / 2 else w / 2 }
                                ) + fadeOut()
                                enterSlide.togetherWith(exitSlide)
                            },
                            modifier = Modifier
                                .clipToBounds()
                                .offset { IntOffset((animatedDragOffset * 0.25f).roundToInt(), 0) },
                            label = "prev_month_text_only_anim"
                        ) { p ->
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = p.year.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = p.formatShortMonth(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Middle: Stationary Pill Container with Text-Only Animation
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                performHaptic()
                                showCustomRangePicker = true
                            }
                            .testTag("center_month_year_pill")
                    ) {
                        AnimatedContent(
                            targetState = Triple(activePeriod, topYearLabel, bottomMonthLabel),
                            transitionSpec = {
                                val movingForward = targetState.first >= initialState.first
                                val enterSlide = slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    initialOffsetX = { w -> if (movingForward) w / 2 else -w / 2 }
                                ) + fadeIn()
                                val exitSlide = slideOutHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    targetOffsetX = { w -> if (movingForward) -w / 2 else w / 2 }
                                ) + fadeOut()
                                enterSlide.togetherWith(exitSlide)
                            },
                            modifier = Modifier
                                .clipToBounds()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .offset { IntOffset((animatedDragOffset * 0.35f).roundToInt(), 0) },
                            label = "center_month_text_only_anim"
                        ) { (_, yearTxt, monthTxt) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = yearTxt,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = monthTxt,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Right: Text-Only Animated Period + Stationary Next Arrow
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                performHaptic()
                                onStepMonth(1)
                            }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                            .testTag("next_month_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        AnimatedContent(
                            targetState = nextPeriod,
                            transitionSpec = {
                                val movingForward = targetState >= initialState
                                val enterSlide = slideInHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    initialOffsetX = { w -> if (movingForward) w / 2 else -w / 2 }
                                ) + fadeIn()
                                val exitSlide = slideOutHorizontally(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    targetOffsetX = { w -> if (movingForward) -w / 2 else w / 2 }
                                ) + fadeOut()
                                enterSlide.togetherWith(exitSlide)
                            },
                            modifier = Modifier
                                .clipToBounds()
                                .offset { IntOffset((animatedDragOffset * 0.25f).roundToInt(), 0) },
                            label = "next_month_text_only_anim"
                        ) { p ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = p.year.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = p.formatShortMonth(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showDateRangeChips) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isCustomSelected = selectedDateRangePreset == DateRangePreset.CUSTOM
                val customChipLabel = if (isCustomSelected && customStartDateMillis != null && customEndDateMillis != null) {
                    periodLabel
                } else {
                    "Custom Range"
                }
                FilterChip(
                    selected = isCustomSelected,
                    onClick = {
                        performHaptic()
                        showCustomRangePicker = true
                    },
                    shape = RoundedCornerShape(50),
                    leadingIcon = {
                        Icon(
                            imageVector = if (isCustomSelected) Icons.Default.Check else Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    label = {
                        Text(
                            text = customChipLabel,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.testTag("date_range_chip_custom")
                )

                val presets = listOf(
                    DateRangePreset.MONTH to "Current Month",
                    DateRangePreset.CURRENT_FY to "Current FY",
                    DateRangePreset.PAST_FY to "Past FY",
                    DateRangePreset.THIS_YEAR to "This Year",
                    DateRangePreset.PAST_YEAR to "Past Year",
                    DateRangePreset.LAST_30_DAYS to "Last 30 Days",
                    DateRangePreset.LAST_90_DAYS to "Last 3 Months",
                    DateRangePreset.LAST_7_DAYS to "Last 7 Days",
                    DateRangePreset.ALL_TIME to "All Time"
                )
                presets.forEach { (preset, chipLabel) ->
                    val isSelected = selectedDateRangePreset == preset &&
                        (preset != DateRangePreset.MONTH || selectedPeriod == YearMonthPeriod.current())
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            performHaptic()
                            if (onSelectDateRangePreset != null) {
                                if (preset == DateRangePreset.MONTH) {
                                    onSelectCurrentMonth()
                                }
                                onSelectDateRangePreset(preset)
                            } else if (preset == DateRangePreset.ALL_TIME) {
                                onToggleAllTime()
                            } else if (preset == DateRangePreset.MONTH) {
                                onSelectCurrentMonth()
                            }
                        },
                        shape = RoundedCornerShape(50),
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        },
                        label = { Text(chipLabel) },
                        modifier = Modifier.testTag(
                            if (preset == DateRangePreset.ALL_TIME) {
                                "all_time_period_chip"
                            } else {
                                "date_range_chip_${preset.name.lowercase()}"
                            }
                        )
                    )
                }
            }
        }
    }

    if (showCustomRangePicker) {
        CustomDateRangePickerModal(
            initialStartMillis = customStartDateMillis,
            initialEndMillis = customEndDateMillis,
            onDismiss = { showCustomRangePicker = false },
            onApplyRange = { start, end ->
                onSelectCustomDateRange?.invoke(start, end)
            }
        )
    }
}

@Composable
fun MonthlyBudgetPulseCard(
    totalSpent: Double,
    totalIncome: Double,
    monthlyBudget: Double,
    dailySafeSpend: Double,
    @Suppress("UNUSED_PARAMETER") remainingDays: Int,
    currencySymbol: String,
    @Suppress("UNUSED_PARAMETER") periodLabel: String,
    @Suppress("UNUSED_PARAMETER") household: Household?,
    @Suppress("UNUSED_PARAMETER") monthlyTrendBars: List<MonthlySpendBar> = emptyList(),
    transactionsCount: Int = 0,
    onOpenTransactionsClick: (() -> Unit)? = null,
    onQuickAddClick: (() -> Unit)? = null,
    onEditBudgetClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val hasAssignedBudget = monthlyBudget > 0.0
    val rawProgress = if (hasAssignedBudget) (totalSpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "budget_bar_progress"
    )
    val isAboveBudget = hasAssignedBudget && totalSpent > monthlyBudget
    val diffAmount = kotlin.math.abs(monthlyBudget - totalSpent)
    val heroBgStart = MaterialTheme.colorScheme.primaryContainer
    val heroBgEnd = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
    val heroInk = MaterialTheme.colorScheme.onPrimaryContainer
    val statusColor = when {
        isAboveBudget -> BudgetDangerCoral
        hasAssignedBudget && rawProgress >= 0.8f -> BudgetWarnAmber
        else -> heroInk
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding)
            .testTag("home_hero_card"),
        shape = RoundedCornerShape(32.dp),
        color = heroBgStart,
        contentColor = heroInk
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(heroBgStart, heroBgEnd)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered Editorial Eyebrow & Hero Amount (Rhythm-inspired calm minimalism)
            Text(
                text = "Monthly spend",
                style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                color = heroInk.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatCurrency(totalSpent, currencySymbol),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = ReceiptMonoFontFamily,
                    letterSpacing = (-1.2).sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = heroInk,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("home_hero_amount")
            )

            if (hasAssignedBudget) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = statusColor,
                    trackColor = heroInk.copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxWidth(0.58f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.height(6.dp))
                val statusLabel = if (isAboveBudget) {
                    "${formatCurrency(diffAmount, currencySymbol)} over ${formatCurrency(monthlyBudget, currencySymbol)}"
                } else {
                    "${formatCurrency(diffAmount, currencySymbol)} left · ${formatCurrency(dailySafeSpend, currencySymbol)}/d"
                }
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAboveBudget) BudgetDangerCoral else heroInk.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("home_budget_status_pill")
                )
            }

            if (totalIncome > 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = heroInk.copy(alpha = 0.08f),
                    modifier = Modifier.testTag("home_monthly_income_pill")
                ) {
                    Text(
                        text = "+${formatCurrency(totalIncome, currencySymbol)} income",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.SemiBold,
                        color = heroInk.copy(alpha = 0.86f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Signature Rhythm Bottom Pill Action Row: [ ○ Budget ] [  Transactions (N)  ] [ ○ Add ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        performHaptic()
                        onEditBudgetClick()
                    },
                    shape = CircleShape,
                    color = heroInk.copy(alpha = 0.11f),
                    contentColor = heroInk,
                    modifier = Modifier
                        .size(50.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("edit_monthly_budget_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Manage Budgets",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        performHaptic()
                        (onOpenTransactionsClick ?: onEditBudgetClick).invoke()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = heroInk,
                        contentColor = heroBgStart
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("view_all_transactions_footer_button")
                ) {
                    Text(
                        text = if (transactionsCount > 0) "Transactions ($transactionsCount)" else "Transactions",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    onClick = {
                        performHaptic()
                        (onQuickAddClick ?: onEditBudgetClick).invoke()
                    },
                    shape = CircleShape,
                    color = heroInk.copy(alpha = 0.11f),
                    contentColor = heroInk,
                    modifier = Modifier
                        .size(50.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("home_quick_add_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniPill(
    modifier: Modifier = Modifier,
    text: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MonthlyBarGraph(
    bars: List<MonthlySpendBar>,
    currencySymbol: String,
    overallBudget: Double = 0.0,
    onSelectMonth: (YearMonthPeriod) -> Unit,
    @Suppress("UNUSED_PARAMETER") onStepMonth: ((Int) -> Unit)? = null
) {
    val dimens = LocalAppDimens.current
    val density = LocalDensity.current
    val performHaptic = rememberAppHapticPerformer()
    val hasOverallBudget = overallBudget > 0.0
    val hasIncome = bars.any { it.incomeTotal > 0.0 }
    val maxExpense = bars.maxOfOrNull { it.expenseTotal }?.coerceAtLeast(1.0) ?: 1.0
    val maxIncome = bars.maxOfOrNull { it.incomeTotal } ?: 0.0
    val maxReference = maxOf(
        maxExpense,
        if (hasOverallBudget) overallBudget * 1.15 else 0.0,
        if (hasIncome) maxIncome * 1.12 else 0.0,
        1.0
    )

    val selectedBar = bars.firstOrNull { it.isSelected } ?: bars.lastOrNull()
    val selectedSpend = selectedBar?.expenseTotal ?: 0.0
    val selectedIncomeDisplay = selectedBar?.incomeTotal?.takeIf { it > 0.0 }
        ?: bars.lastOrNull { it.incomeTotal > 0.0 }?.incomeTotal
        ?: 0.0
    val isSelectedAboveBudget = hasOverallBudget && selectedSpend > overallBudget
    val budgetDiff = kotlin.math.abs(overallBudget - selectedSpend)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow
    val budgetLineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val scrollState = rememberScrollState()
    var initialScrollDone by remember(bars.size) { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hasOverallBudget) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overall_budget_trend_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "- - Budget ${formatCompactCurrency(overallBudget, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                val badgeColor = if (isSelectedAboveBudget) BudgetDangerCoral else BudgetSafeGreen
                val statusLabel = if (isSelectedAboveBudget) {
                    "▲ ${formatCompactCurrency(budgetDiff, currencySymbol)} Above Budget"
                } else {
                    "▼ ${formatCompactCurrency(budgetDiff, currencySymbol)} Below Budget"
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = 0.16f),
                    modifier = Modifier.testTag("overall_budget_status_pill")
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (hasIncome) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_income_trend_legend"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PersonalBadgeTeal.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "● Salary / Income ${formatCompactCurrency(selectedIncomeDisplay, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = PersonalBadgeTeal,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (selectedBar != null && selectedBar.incomeTotal > 0.0) {
                    val netSavings = selectedBar.incomeTotal - selectedSpend
                    val netColor = if (netSavings >= 0.0) BudgetSafeGreen else BudgetDangerCoral
                    val netLabel = if (netSavings >= 0.0) {
                        "+${formatCompactCurrency(netSavings, currencySymbol)} Saved"
                    } else {
                        "-${formatCompactCurrency(kotlin.math.abs(netSavings), currencySymbol)} Deficit"
                    }
                    Text(
                        text = netLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.SemiBold,
                        color = netColor,
                        maxLines = 1
                    )
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val viewportWidth = maxWidth
            val minSlotWidth = if (hasIncome) 74.dp else 64.dp
            val slotWidth = if (bars.size > 6) {
                (viewportWidth / 5.2f).coerceAtLeast(minSlotWidth)
            } else {
                (viewportWidth / bars.size.coerceAtLeast(1)).coerceAtLeast(minSlotWidth)
            }
            val totalContentWidth = (slotWidth * bars.size.coerceAtLeast(1)).coerceAtLeast(viewportWidth)
            val singleBarWidth = if (hasIncome) {
                (slotWidth * 0.26f).coerceIn(12.dp, 20.dp)
            } else {
                (slotWidth * 0.40f).coerceIn(16.dp, 30.dp)
            }
            val maxBarHeight = dimens.barChartHeight * 0.56f
            val chartHeight = if (hasIncome) dimens.barChartHeight * 1.15f else dimens.barChartHeight

            LaunchedEffect(bars.size, totalContentWidth) {
                if (!initialScrollDone && bars.size > 6) {
                    val selectedIndex = bars.indexOfFirst { it.isSelected }.takeIf { it >= 0 } ?: (bars.size - 1)
                    val targetPx = with(density) {
                        ((slotWidth * (selectedIndex + 1)) - viewportWidth).coerceAtLeast(0.dp).roundToPx()
                    }
                    scrollState.scrollTo(targetPx)
                    initialScrollDone = true
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .testTag("monthly_bar_graph_scroll_container")
            ) {
                Box(
                    modifier = Modifier
                        .width(totalContentWidth)
                        .height(chartHeight)
                ) {
                    // Background Horizontal M3 Reference Grid Lines (inspired by FIR Equalizer)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bottomLabelReservePx = (dimens.itemSpacing * 1.05f + 18.dp).toPx()
                        val maxBarAreaPx = maxBarHeight.toPx()
                        val barBottomY = (size.height - bottomLabelReservePx).coerceAtLeast(maxBarAreaPx)
                        for (step in 0..3) {
                            val y = barBottomY - (maxBarAreaPx * (step / 3f))
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        bars.forEach { bar ->
                            val expFraction = (bar.expenseTotal / maxReference).toFloat().coerceIn(0.08f, 1f)
                            val incFraction = (bar.incomeTotal / maxReference).toFloat().coerceIn(0.08f, 1f)
                            val isBarAboveBudget = hasOverallBudget && bar.expenseTotal > overallBudget
                            val expBarColor = when {
                                hasOverallBudget && bar.isSelected -> if (isBarAboveBudget) BudgetDangerCoral else BudgetSafeGreen
                                hasOverallBudget -> if (isBarAboveBudget) {
                                    BudgetDangerCoral.copy(alpha = 0.56f)
                                } else {
                                    BudgetSafeGreen.copy(alpha = 0.52f)
                                }
                                bar.isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
                            }
                            val incBarColor = if (bar.isSelected) {
                                PersonalBadgeTeal
                            } else {
                                PersonalBadgeTeal.copy(alpha = 0.62f)
                            }
                            val amountText = if (bar.expenseTotal > 0) {
                                val arrow = if (hasOverallBudget) {
                                    if (isBarAboveBudget) "▲" else "▼"
                                } else {
                                    ""
                                }
                                "$arrow${formatCompactCurrency(bar.expenseTotal, currencySymbol)}"
                            } else {
                                "-"
                            }

                            Column(
                                modifier = Modifier
                                    .width(slotWidth)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (bar.isSelected) {
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        performHaptic()
                                        onSelectMonth(bar.period)
                                    }
                                    .padding(horizontal = 2.dp, vertical = 4.dp)
                                    .testTag("month_bar_column_${bar.period.year}_${bar.period.month}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                if (bar.incomeTotal > 0.0) {
                                    Text(
                                        text = "+${formatCompactCurrency(bar.incomeTotal, currencySymbol)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                        color = PersonalBadgeTeal,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        modifier = Modifier.testTag("month_income_label_${bar.period.year}_${bar.period.month}")
                                    )
                                }
                                Text(
                                    text = amountText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    color = when {
                                        hasOverallBudget && bar.expenseTotal > 0 -> if (isBarAboveBudget) BudgetDangerCoral else BudgetSafeGreen
                                        bar.isSelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = if (bar.isSelected || hasOverallBudget) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(singleBarWidth)
                                            .height((maxBarHeight * expFraction).coerceAtLeast(8.dp))
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                            .background(expBarColor)
                                    )
                                    if (bar.incomeTotal > 0.0) {
                                        Box(
                                            modifier = Modifier
                                                .width(singleBarWidth)
                                                .height((maxBarHeight * incFraction).coerceAtLeast(8.dp))
                                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                                .background(incBarColor)
                                                .testTag("month_income_bar_${bar.period.year}_${bar.period.month}")
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = bar.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (bar.isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (bar.isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Smooth Bezier Curve + Gradient Fill + Circular Nodes for Salary / Income
                    if (hasIncome && bars.isNotEmpty()) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("salary_income_trend_canvas")
                        ) {
                            val bottomLabelReservePx = (dimens.itemSpacing * 1.05f + 18.dp).toPx()
                            val maxBarAreaPx = maxBarHeight.toPx()
                            val barBottomY = (size.height - bottomLabelReservePx).coerceAtLeast(maxBarAreaPx)
                            val slotPx = size.width / bars.size.coerceAtLeast(1)

                            val incomePoints = bars.mapIndexed { index, bar ->
                                val frac = if (bar.incomeTotal > 0.0) {
                                    (bar.incomeTotal / maxReference).toFloat().coerceIn(0.08f, 1f)
                                } else {
                                    0.04f
                                }
                                val cx = slotPx * (index + 0.5f)
                                val cy = (barBottomY - maxBarAreaPx * frac).coerceIn(6.dp.toPx(), barBottomY - 4.dp.toPx())
                                Offset(cx, cy)
                            }

                            if (incomePoints.size >= 2) {
                                val area = buildSmoothAreaPath(incomePoints, barBottomY)
                                drawPath(
                                    path = area,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(PersonalBadgeTeal.copy(alpha = 0.22f), Color.Transparent),
                                        startY = barBottomY - maxBarAreaPx,
                                        endY = barBottomY
                                    )
                                )
                                drawPath(
                                    path = buildSmoothBezierPath(incomePoints),
                                    color = PersonalBadgeTeal,
                                    style = Stroke(
                                        width = 2.6.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            incomePoints.forEachIndexed { index, point ->
                                val bar = bars[index]
                                if (bar.incomeTotal > 0.0) {
                                    val nodeRadius = if (bar.isSelected) 5.6.dp.toPx() else 4.2.dp.toPx()
                                    drawCircle(
                                        color = surfaceColor,
                                        radius = nodeRadius + 2.dp.toPx(),
                                        center = point
                                    )
                                    drawCircle(
                                        color = PersonalBadgeTeal,
                                        radius = nodeRadius,
                                        center = point
                                    )
                                }
                            }
                        }
                    }

                    // Smooth Bezier Curve + Gradient Fill + Circular Nodes for Overall Budget & Spend
                    if (hasOverallBudget && bars.isNotEmpty()) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("overall_budget_trend_canvas")
                        ) {
                            val bottomLabelReservePx = (dimens.itemSpacing * 1.05f + 18.dp).toPx()
                            val maxBarAreaPx = maxBarHeight.toPx()
                            val barBottomY = (size.height - bottomLabelReservePx).coerceAtLeast(maxBarAreaPx)

                            val budgetFraction = (overallBudget / maxReference).toFloat().coerceIn(0.08f, 1f)
                            val budgetY = (barBottomY - maxBarAreaPx * budgetFraction).coerceIn(8.dp.toPx(), barBottomY - 4.dp.toPx())

                            drawLine(
                                color = budgetLineColor.copy(alpha = 0.72f),
                                start = Offset(4.dp.toPx(), budgetY),
                                end = Offset(size.width - 4.dp.toPx(), budgetY),
                                strokeWidth = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )

                            val slotPx = size.width / bars.size.coerceAtLeast(1)
                            val trendPoints = bars.mapIndexed { index, bar ->
                                val frac = (bar.expenseTotal / maxReference).toFloat().coerceIn(0.08f, 1f)
                                val cx = slotPx * (index + 0.5f)
                                val cy = (barBottomY - maxBarAreaPx * frac).coerceIn(6.dp.toPx(), barBottomY - 4.dp.toPx())
                                Offset(cx, cy)
                            }

                            if (trendPoints.size >= 2) {
                                val area = buildSmoothAreaPath(trendPoints, barBottomY)
                                drawPath(
                                    path = area,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                                        startY = barBottomY - maxBarAreaPx,
                                        endY = barBottomY
                                    )
                                )
                                drawPath(
                                    path = buildSmoothBezierPath(trendPoints),
                                    color = primaryColor,
                                    style = Stroke(
                                        width = 2.6.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            trendPoints.forEachIndexed { index, point ->
                                val bar = bars[index]
                                val isAbove = bar.expenseTotal > overallBudget
                                val nodeColor = if (isAbove) BudgetDangerCoral else BudgetSafeGreen
                                val nodeRadius = if (bar.isSelected) 5.8.dp.toPx() else 4.4.dp.toPx()
                                drawCircle(
                                    color = surfaceColor,
                                    radius = nodeRadius + 2.dp.toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = nodeColor,
                                    radius = nodeRadius,
                                    center = point
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountsCarouselStrip(
    accounts: List<FinancialAccount>,
    currencySymbol: String,
    onAddAccountClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            accounts.forEach { acc ->
                val amountText = if (acc.accountType == "CREDIT_CARD" && acc.creditLimit > 0) {
                    formatCurrency(acc.creditLimit, currencySymbol)
                } else {
                    formatCurrency(acc.balance, currencySymbol)
                }
                AccountSummaryCard(
                    title = acc.name,
                    amountLabel = amountText,
                    accountType = acc.accountType
                )
            }
            FilledTonalButton(
                onClick = onAddAccountClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .height(dimens.navPillHeight)
                    .testTag("add_account_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add")
            }
        }
    }
}

@Composable
private fun AccountSummaryCard(
    title: String,
    amountLabel: String,
    accountType: String
) {
    val icon = when (FinancialAccount.normalizeAccountType(accountType)) {
        "CREDIT_CARD", "DEBIT_CARD" -> Icons.Default.CreditCard
        "CASH", "WALLET" -> Icons.Default.Payments
        else -> Icons.Default.AccountBalance
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquircleIconBadge(
                icon = icon,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                size = 36.dp,
                iconSize = 18.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = amountLabel,
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

/**
 * Connected-Stack M3 Transaction Receipt Row:
 * Supports `shape` (for 24dp outer / 6dp inner connected M3 card stacks) and two-way
 * progressive swipe physics with threshold icon bounce and haptic ticks.
 */
fun deriveTransactionDisplayTags(expense: ExpenseTransaction): List<String> {
    val explicit = expense.tags.map { it.trim().removePrefix("#") }.filter { it.isNotBlank() }
    if (explicit.isNotEmpty()) return explicit
    val mode = expense.paymentMode.trim().uppercase()
    val titleLower = expense.title.lowercase()
    val derived = when {
        mode == "UPI" || titleLower.contains("upi") -> "Online"
        mode == "CARD" -> "Card"
        mode == "CASH" -> "Cash"
        mode == "WALLET" -> "Wallet"
        else -> "Online"
    }
    return listOf(derived)
}

@Composable
fun ExpenseTransactionItemCard(
    expense: ExpenseTransaction,
    currencySymbol: String,
    @Suppress("UNUSED_PARAMETER") canTogglePrivacy: Boolean = false,
    @Suppress("UNUSED_PARAMETER") onTogglePrivacy: () -> Unit = {},
    onToggleExcludeTransaction: () -> Unit = {},
    onLinkDuplicate: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEditTransaction: () -> Unit = {},
    onQuickChangeCategory: () -> Unit = {},
    availableCategories: List<String> = ExpenseCategories.ALL,
    onTagClick: (String) -> Unit = {},
    onCategoryClick: ((String) -> Unit)? = null,
    onMerchantClick: ((String) -> Unit)? = null,
    onTypeClick: ((String) -> Unit)? = null,
    shape: Shape? = null,
    containerColor: Color? = null
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val customEmojis = rememberCustomCategoryEmojis()
    val customEmoji = customEmojis[expense.category.trim().lowercase()]
    val effectiveShape = shape ?: RoundedCornerShape(dimens.cardCornerRadius)
    val effectiveContainerColor = containerColor ?: MaterialTheme.colorScheme.surfaceContainerLow

    val isIncome = expense.type.equals("INCOME", ignoreCase = true) ||
        com.example.data.model.ExpenseCategories.isIncomeCategory(expense.category)
    val catColor = categoryAccentColor(expense.category)
    val displayTags = remember(expense.tags, expense.paymentMode, expense.title) {
        deriveTransactionDisplayTags(expense)
    }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var cardWidthPx by remember { mutableFloatStateOf(1f) }
    var thresholdCrossed by remember { mutableStateOf(false) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tx_swipe_offset"
    )
    val revealIconScale by animateFloatAsState(
        targetValue = if (thresholdCrossed) 1.18f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tx_swipe_icon_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .clip(effectiveShape)
    ) {
        if (kotlin.math.abs(animatedOffsetX) > 6f) {
            val isSwipingRight = animatedOffsetX > 0f
            val bgRevealColor = if (isSwipingRight) {
                MaterialTheme.colorScheme.primary
            } else {
                BudgetDangerCoral
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(bgRevealColor.copy(alpha = 0.22f))
                    .padding(horizontal = dimens.horizontalPadding * 1.1f),
                contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (isSwipingRight) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Link Duplicate SMS / Transaction",
                            tint = bgRevealColor,
                            modifier = Modifier
                                .size(dimens.rowIconSize)
                                .scale(revealIconScale)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Link Duplicate",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = bgRevealColor
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = bgRevealColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Transaction",
                            tint = bgRevealColor,
                            modifier = Modifier
                                .size(dimens.rowIconSize)
                                .scale(revealIconScale)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .clip(effectiveShape)
                .pointerInput(expense.id) {
                    detectTapGestures(
                        onTap = {
                            performHaptic()
                            onEditTransaction()
                        }
                    )
                }
                .pointerInput(expense.id, cardWidthPx) {
                    val maxDrag = cardWidthPx * 0.42f
                    val triggerThreshold = cardWidthPx * 0.20f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val next = (dragOffsetX + dragAmount).coerceIn(-maxDrag, maxDrag)
                            val nowOver = kotlin.math.abs(next) > triggerThreshold
                            if (nowOver && !thresholdCrossed) {
                                performHaptic()
                            }
                            thresholdCrossed = nowOver
                            dragOffsetX = next
                        },
                        onDragEnd = {
                            thresholdCrossed = false
                            when {
                                dragOffsetX > triggerThreshold -> {
                                    dragOffsetX = 0f
                                    onLinkDuplicate()
                                }
                                dragOffsetX < -triggerThreshold -> {
                                    dragOffsetX = 0f
                                    onDelete()
                                }
                                else -> {
                                    dragOffsetX = 0f
                                }
                            }
                        },
                        onDragCancel = {
                            thresholdCrossed = false
                            dragOffsetX = 0f
                        }
                    )
                }
                .testTag("expense_card_${expense.id}"),
            shape = effectiveShape,
            colors = CardDefaults.cardColors(containerColor = effectiveContainerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SquircleIconBadge(
                        icon = categoryIcon(expense.category),
                        tint = catColor,
                        containerColor = catColor.copy(alpha = 0.16f),
                        size = dimens.rowIconBoxSize,
                        iconSize = dimens.rowIconSize,
                        emoji = customEmoji,
                        contentDescription = "${expense.category} (Tap to edit, long press and slide to change category)",
                        modifier = Modifier
                            .categoryWheelLongPressGesture(
                                transaction = expense,
                                availableCategories = availableCategories,
                                onTap = { onEditTransaction() },
                                onFallbackQuickChange = onQuickChangeCategory
                            )
                            .testTag("category_pill_${expense.id}")
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("tx_merchant_chip_${expense.id}")
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // Clickable #Tag pills + Non-filtering Category & Account metadata
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            displayTags.forEach { rawTag ->
                                val cleanTag = rawTag.trim().removePrefix("#")
                                val safeTagId = cleanTag.lowercase().replace(Regex("[^a-z0-9]+"), "_")
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF8AB4F8).copy(alpha = 0.24f),
                                    border = BorderStroke(0.8.dp, Color(0xFF8AB4F8).copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            performHaptic()
                                            onTagClick(cleanTag)
                                        }
                                        .testTag("tx_tag_pill_${expense.id}_$safeTagId")
                                ) {
                                    Text(
                                        text = "#$cleanTag",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8AB4F8),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = catColor.copy(alpha = 0.14f),
                                modifier = Modifier.testTag("tx_category_chip_${expense.id}")
                            ) {
                                Text(
                                    text = expense.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = catColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                                )
                            }

                            if (expense.accountName.isNotBlank()) {
                                Text(
                                    text = "• ${expense.accountName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "${if (isIncome) "+" else "-"}${formatCurrency(expense.amount, currencySymbol)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    expense.isExcludedFromExpense -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                    isIncome -> BudgetSafeGreen
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("tx_direction_badge_${expense.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isIncome) "↙" else "↗",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIncome) BudgetSafeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = formatDayMonthLabel(expense.transactionDateMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Bottom Metadata Badges & Full 48dp Action Icons Row (prevents squeezing merchant name & accidental icon taps)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (expense.isExcludedFromExpense) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.testTag("excluded_tx_badge_${expense.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoneyOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Excluded",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        if (expense.linkedSmsSignatures.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.testTag("linked_sms_badge_${expense.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Linked (${expense.linkedSmsSignatures.size + 1} SMS)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                performHaptic()
                                onToggleExcludeTransaction()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(48.dp)
                                .testTag("exclude_tx_button_${expense.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoneyOff,
                                contentDescription = when {
                                    isIncome && expense.isExcludedFromExpense -> "Include in Income"
                                    isIncome -> "Exclude from Income"
                                    expense.isExcludedFromExpense -> "Include in Expenses"
                                    else -> "Exclude from Expenses"
                                },
                                tint = if (expense.isExcludedFromExpense) {
                                    BudgetWarnAmber
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                performHaptic()
                                onLinkDuplicate()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(48.dp)
                                .testTag("link_duplicate_button_${expense.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Link Duplicate SMS / Transaction",
                                tint = if (expense.linkedSmsSignatures.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    categoryTotals: List<Pair<String, Double>>,
    currencySymbol: String,
    selectedCategory: String? = null,
    onCategoryClick: (String) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val palette = listOf(
        Color(0xFFFF5A5F),
        Color(0xFFFFB703),
        Color(0xFF06D6A0),
        Color(0xFF8338EC),
        Color(0xFFEC4899),
        Color(0xFF118AB2),
        Color(0xFFF97316),
        Color(0xFF14B8A6),
        Color(0xFFA855F7),
        Color(0xFFEAB308)
    )
    val visibleSlices = categoryTotals
    val grandTotal = visibleSlices.sumOf { it.second }.coerceAtLeast(1.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.itemSpacing * 0.65f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimens.donutChartSize)
                .testTag("category_pie_chart_canvas"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(dimens.donutChartSize * 0.88f)
                    .pointerInput(visibleSlices) {
                        detectTapGestures { tapOffset ->
                            if (visibleSlices.isEmpty()) return@detectTapGestures
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val dx = tapOffset.x - cx
                            val dy = tapOffset.y - cy
                            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                            val outerRadius = size.width / 2f
                            if (distance > outerRadius * 1.15f) return@detectTapGestures

                            val degFromEast = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            val clockwiseFromTop = ((degFromEast + 90f) % 360f + 360f) % 360f

                            var accumulated = 0f
                            for ((cat, amt) in visibleSlices) {
                                val sweep = ((amt / grandTotal) * 360.0).toFloat()
                                if (clockwiseFromTop >= accumulated && clockwiseFromTop <= accumulated + sweep + 0.5f) {
                                    performHaptic()
                                    onCategoryClick(cat)
                                    break
                                }
                                accumulated += sweep
                            }
                        }
                    }
            ) {
                val baseStroke = size.minDimension * 0.20f
                val selectedStroke = size.minDimension * 0.25f
                var startAngle = -90f
                visibleSlices.forEachIndexed { idx, (cat, amount) ->
                    val sweep = ((amount / grandTotal) * 360.0).toFloat()
                    val isSelected = selectedCategory != null && selectedCategory.equals(cat, ignoreCase = true)
                    val stroke = if (isSelected) selectedStroke else baseStroke
                    val sliceColor = if (selectedCategory == null || isSelected) {
                        palette[idx % palette.size]
                    } else {
                        palette[idx % palette.size].copy(alpha = 0.42f)
                    }
                    drawArc(
                        color = sliceColor,
                        startAngle = startAngle,
                        sweepAngle = (sweep - 1.5f).coerceAtLeast(1f),
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt),
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke)
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${categoryTotals.size}",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "cats",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(dimens.horizontalPadding))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            visibleSlices.forEachIndexed { idx, (category, amount) ->
                val pct = ((amount / grandTotal) * 100).toInt()
                val isSelected = selectedCategory != null && selectedCategory.equals(category, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) {
                        palette[idx % palette.size].copy(alpha = 0.16f)
                    } else {
                        Color.Transparent
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            performHaptic()
                            onCategoryClick(category)
                        }
                        .testTag("pie_slice_legend_${category.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(palette[idx % palette.size])
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "$pct% (${formatCompactCurrency(amount, currencySymbol)})",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptZigzagEdgeStrip(
    paperColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    isTop: Boolean = true
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val teethCount = 28
        val toothWidth = size.width / teethCount
        val path = Path().apply {
            if (isTop) {
                moveTo(0f, size.height)
                for (i in 0 until teethCount) {
                    val startX = i * toothWidth
                    lineTo(startX + toothWidth / 2f, 0f)
                    lineTo(startX + toothWidth, size.height)
                }
                close()
            } else {
                moveTo(0f, 0f)
                for (i in 0 until teethCount) {
                    val startX = i * toothWidth
                    lineTo(startX + toothWidth / 2f, size.height)
                    lineTo(startX + toothWidth, 0f)
                }
                close()
            }
        }
        drawPath(path = path, color = paperColor)
    }
}

@Composable
fun ReceiptDashedDividerLine(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val dimens = LocalAppDimens.current
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimens.itemSpacing * 0.6f)
            .height(2.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    }
}

@Composable
fun ReceiptTicketCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ReceiptZigzagEdgeStrip(paperColor = containerColor, isTop = true)
        Surface(
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
        ReceiptZigzagEdgeStrip(paperColor = containerColor, isTop = false)
    }
}


@Composable
fun ThemeModeSelectorBar(
    currentMode: com.example.ui.theme.AppThemeMode,
    onSelectMode: (com.example.ui.theme.AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember {
        listOf(
            Triple(com.example.ui.theme.AppThemeMode.LIGHT, "Light", Icons.Default.LightMode),
            Triple(com.example.ui.theme.AppThemeMode.SYSTEM, "Auto", Icons.Default.BrightnessAuto),
            Triple(com.example.ui.theme.AppThemeMode.DARK, "Dark", Icons.Default.DarkMode)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("theme_color_swatch_bar"),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { (mode, label, icon) ->
                val isSelected = currentMode == mode
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "theme_pill_container"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "theme_pill_content"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("theme_mode_${mode.name.lowercase()}")
                ) {
                    Surface(
                        onClick = { onSelectMode(mode) },
                        shape = RoundedCornerShape(50),
                        color = containerColor,
                        contentColor = contentColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("appearance_segment_${label.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "$label theme",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Color-only Primary Theme Color Selector (`PrimaryColorSelectorBar`):
 * - Lets the user choose:
 *   1. Material 3 System Dynamic Color (`SYSTEM_DYNAMIC`)
 *   2. Kiwi Green (`KIWI_GREEN`, the app's default theme color)
 *   3. Curated preset primary colors (`CORAL_POP`, `OCEAN_BLUE`, `ROYAL_BERRY`, `EMERALD_TEAL`, `SUNNY_AMBER`, `ROSE_PINK`)
 *   4. Any custom primary color (`CUSTOM_COLOR`) via an interactive color picker modal (`CustomPrimaryColorPickerDialog`).
 * - Uses pure color swatches and icons without text labels on the selector bar.
 */
@Composable
fun PrimaryColorSelectorBar(
    selectedOption: AppPrimaryColorOption,
    customPrimaryColorArgb: Long,
    onSelectOption: (AppPrimaryColorOption) -> Unit,
    onSelectCustomColorArgb: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    var showCustomColorDialog by rememberSaveable { mutableStateOf(false) }

    val orderedOptions = remember {
        listOf(
            AppPrimaryColorOption.KIWI_GREEN,
            AppPrimaryColorOption.SYSTEM_DYNAMIC,
            AppPrimaryColorOption.CORAL_POP,
            AppPrimaryColorOption.OCEAN_BLUE,
            AppPrimaryColorOption.ROYAL_BERRY,
            AppPrimaryColorOption.EMERALD_TEAL,
            AppPrimaryColorOption.SUNNY_AMBER,
            AppPrimaryColorOption.ROSE_PINK,
            AppPrimaryColorOption.CUSTOM_COLOR
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("primary_color_swatch_bar"),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(
                    horizontal = dimens.horizontalPadding * 0.55f,
                    vertical = dimens.itemSpacing * 0.55f
                ),
            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.45f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            orderedOptions.forEach { option ->
                val isSelected = selectedOption == option
                val swatchTag = when (option) {
                    AppPrimaryColorOption.CUSTOM_COLOR -> "primary_color_custom_picker"
                    else -> "primary_color_${option.name.lowercase()}"
                }
                val swatchColor = when (option) {
                    AppPrimaryColorOption.CUSTOM_COLOR -> Color(customPrimaryColorArgb or 0xFF000000L)
                    else -> option.lightPrimary
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (option == AppPrimaryColorOption.CUSTOM_COLOR) {
                                showCustomColorDialog = true
                            } else {
                                onSelectOption(option)
                            }
                        }
                        .testTag(swatchTag),
                    contentAlignment = Alignment.Center
                ) {
                    if (option == AppPrimaryColorOption.SYSTEM_DYNAMIC) {
                        // Material 3 Dynamic Color 4-quadrant adaptive palette wheel
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2f
                            val c = Offset(size.width / 2f, size.height / 2f)
                            val arcSize = Size(r * 2f, r * 2f)
                            val topLeft = Offset(c.x - r, c.y - r)
                            drawArc(color = Color(0xFF65A30D), startAngle = 180f, sweepAngle = 90f, useCenter = true, topLeft = topLeft, size = arcSize)
                            drawArc(color = Color(0xFF0284C7), startAngle = 270f, sweepAngle = 90f, useCenter = true, topLeft = topLeft, size = arcSize)
                            drawArc(color = Color(0xFF7C3AED), startAngle = 0f, sweepAngle = 90f, useCenter = true, topLeft = topLeft, size = arcSize)
                            drawArc(color = Color(0xFFD97706), startAngle = 90f, sweepAngle = 90f, useCenter = true, topLeft = topLeft, size = arcSize)
                        }
                    } else if (option == AppPrimaryColorOption.CUSTOM_COLOR) {
                        // Rainbow ring + custom selected center
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2f
                            val c = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF65A30D),
                                        Color(0xFF0284C7),
                                        Color(0xFF7C3AED),
                                        Color(0xFFDB2777),
                                        Color(0xFFFF5A5F),
                                        Color(0xFFF59E0B),
                                        Color(0xFF65A30D)
                                    )
                                ),
                                radius = r,
                                center = c
                            )
                            drawCircle(
                                color = swatchColor,
                                radius = r * 0.68f,
                                center = c
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(swatchColor, CircleShape)
                        )
                    }

                    // Selection & Icon Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isSelected -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = option.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            option == AppPrimaryColorOption.SYSTEM_DYNAMIC -> {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = option.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            option == AppPrimaryColorOption.CUSTOM_COLOR -> {
                                Icon(
                                    imageVector = Icons.Default.Colorize,
                                    contentDescription = option.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomColorDialog) {
        CustomPrimaryColorPickerDialog(
            initialColorArgb = customPrimaryColorArgb,
            onDismiss = { showCustomColorDialog = false },
            onConfirmColor = { chosenArgb ->
                onSelectCustomColorArgb(chosenArgb)
                showCustomColorDialog = false
            }
        )
    }
}

private val CUSTOM_COLOR_SPECTRUM_SWATCHES = listOf(
    0xFF65A30DL, // Kiwi Green
    0xFF84CC16L, // Fresh Lime
    0xFF16A34AL, // Forest Green
    0xFF0D9488L, // Emerald Teal
    0xFF0284C7L, // Ocean Blue
    0xFF2563EBL, // Cobalt Blue
    0xFF4F46E5L, // Royal Indigo
    0xFF7C3AEDL, // Violet Berry
    0xFF9333EAL, // Electric Purple
    0xFFDB2777L, // Rose Pink
    0xFFE11D48L, // Crimson Ruby
    0xFFFF5A5FL, // Coral Pop
    0xFFEA580CL, // Tangerine Orange
    0xFFD97706L, // Sunny Amber
    0xFFCA8A04L, // Golden Olive
    0xFF475569L  // Slate Charcoal
)

@Composable
fun CustomPrimaryColorPickerDialog(
    initialColorArgb: Long,
    onDismiss: () -> Unit,
    onConfirmColor: (Long) -> Unit
) {
    val cleanInitial = initialColorArgb or 0xFF000000L
    var red by remember { mutableFloatStateOf(((cleanInitial shr 16) and 0xFF).toFloat()) }
    var green by remember { mutableFloatStateOf(((cleanInitial shr 8) and 0xFF).toFloat()) }
    var blue by remember { mutableFloatStateOf((cleanInitial and 0xFF).toFloat()) }

    val currentArgb = (0xFFL shl 24) or
        ((red.roundToInt().coerceIn(0, 255).toLong()) shl 16) or
        ((green.roundToInt().coerceIn(0, 255).toLong()) shl 8) or
        (blue.roundToInt().coerceIn(0, 255).toLong())

    var hexInput by remember(currentArgb) {
        mutableStateOf(String.format(Locale.US, "#%06X", currentArgb and 0xFFFFFFL))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("custom_primary_color_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(currentArgb))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                Text(
                    text = hexInput,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Curated 16-swatch spectrum grid (4 rows of 4 for >= 48dp accessible touch targets)
                CUSTOM_COLOR_SPECTRUM_SWATCHES.chunked(4).forEach { rowSwatches ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowSwatches.forEach { swatchArgb ->
                            val isMatch = (currentArgb and 0xFFFFFFL) == (swatchArgb and 0xFFFFFFL)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        red = ((swatchArgb shr 16) and 0xFF).toFloat()
                                        green = ((swatchArgb shr 8) and 0xFF).toFloat()
                                        blue = (swatchArgb and 0xFF).toFloat()
                                    }
                                    .testTag("custom_swatch_${String.format(Locale.US, "%06x", swatchArgb and 0xFFFFFFL)}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(swatchArgb))
                                        .border(
                                            width = if (isMatch) 2.5.dp else 1.dp,
                                            color = if (isMatch) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                // RGB Sliders for any custom primary color
                Slider(
                    value = red,
                    onValueChange = { red = it },
                    valueRange = 0f..255f,
                    modifier = Modifier.testTag("custom_color_red_slider")
                )
                Slider(
                    value = green,
                    onValueChange = { green = it },
                    valueRange = 0f..255f,
                    modifier = Modifier.testTag("custom_color_green_slider")
                )
                Slider(
                    value = blue,
                    onValueChange = { blue = it },
                    valueRange = 0f..255f,
                    modifier = Modifier.testTag("custom_color_blue_slider")
                )

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { raw ->
                        val cleaned = raw.trim().uppercase(Locale.US)
                        hexInput = cleaned.take(7)
                        val hexDigits = cleaned.removePrefix("#")
                        if (hexDigits.length == 6) {
                            runCatching {
                                val parsed = hexDigits.toLong(16)
                                red = ((parsed shr 16) and 0xFF).toFloat()
                                green = ((parsed shr 8) and 0xFF).toFloat()
                                blue = (parsed and 0xFF).toFloat()
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_color_hex_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmColor(currentArgb) },
                modifier = Modifier.testTag("apply_custom_primary_color_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Apply Color"
                )
            }
        },
        dismissButton = {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Cancel"
                )
            }
        }
    )
}

private val FINANCE_WALLPAPER_EMOJIS = listOf(
    "💳", "🧾", "🪙", "📊", "🏡", "💰", "🛍️", "📈", "🏦", "🏷️", "🛒", "💎"
)

private fun resolveWallpaperPalette(
    accent: WallpaperColorAccent,
    isDark: Boolean,
    primaryThemeColor: Color,
    secondaryThemeColor: Color,
    tertiaryThemeColor: Color
): List<Color> {
    return when (accent) {
        WallpaperColorAccent.THEME_ADAPTIVE -> if (isDark) {
            listOf(
                primaryThemeColor,
                secondaryThemeColor,
                Color(0xFF38BDF8),
                Color(0xFF34D399)
            )
        } else {
            listOf(
                Color(0xFF8C6D53),
                primaryThemeColor,
                secondaryThemeColor,
                tertiaryThemeColor
            )
        }

        WallpaperColorAccent.EMERALD_MINT -> if (isDark) {
            listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF059669), Color(0xFF6EE7B7))
        } else {
            listOf(Color(0xFF047857), Color(0xFF059669), Color(0xFF0D9488), Color(0xFF10B981))
        }

        WallpaperColorAccent.ROYAL_GOLD -> if (isDark) {
            listOf(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFFDE68A))
        } else {
            listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFF92400E), Color(0xFFF59E0B))
        }

        WallpaperColorAccent.OCEAN_SLATE -> if (isDark) {
            listOf(Color(0xFF38BDF8), Color(0xFF60A5FA), Color(0xFF818CF8), Color(0xFF2DD4BF))
        } else {
            listOf(Color(0xFF0369A1), Color(0xFF1D4ED8), Color(0xFF4338CA), Color(0xFF0F766E))
        }

        WallpaperColorAccent.WARM_CORAL -> if (isDark) {
            listOf(Color(0xFFFF6B6B), Color(0xFFFF9F43), Color(0xFFF472B6), Color(0xFFFBBF24))
        } else {
            listOf(Color(0xFFE11D48), Color(0xFFEA580C), Color(0xFFDB2777), Color(0xFFD97706))
        }
    }
}

/**
 * Draws a single WhatsApp-style vector finance line-art doodle inside a square tile of radius [r]
 * centered at [center].
 */
private fun DrawScope.drawFinanceDoodleMotif(
    motifIndex: Int,
    center: Offset,
    r: Float,
    color: Color,
    strokeWidth: Float
) {
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    when (motifIndex % 10) {
        // 0: Credit / Debit Card with EMV Chip & Stripe
        0 -> {
            val w = r * 1.65f
            val h = r * 1.12f
            val topLeft = Offset(center.x - w / 2f, center.y - h / 2f)
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = Size(w, h),
                cornerRadius = CornerRadius(r * 0.22f, r * 0.22f),
                style = stroke
            )
            drawLine(
                color = color,
                start = Offset(topLeft.x, topLeft.y + h * 0.34f),
                end = Offset(topLeft.x + w, topLeft.y + h * 0.34f),
                strokeWidth = strokeWidth
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(topLeft.x + w * 0.16f, topLeft.y + h * 0.54f),
                size = Size(w * 0.24f, h * 0.24f),
                cornerRadius = CornerRadius(r * 0.06f, r * 0.06f),
                style = stroke
            )
        }

        // 1: Paper Receipt Ticket with Zigzag Top & Item Lines
        1 -> {
            val w = r * 1.25f
            val h = r * 1.65f
            val left = center.x - w / 2f
            val top = center.y - h / 2f
            val right = center.x + w / 2f
            val bottom = center.y + h / 2f
            val path = Path().apply {
                moveTo(left, top + h * 0.1f)
                lineTo(left + w * 0.25f, top)
                lineTo(left + w * 0.5f, top + h * 0.1f)
                lineTo(left + w * 0.75f, top)
                lineTo(right, top + h * 0.1f)
                lineTo(right, bottom - h * 0.1f)
                lineTo(left + w * 0.75f, bottom)
                lineTo(left + w * 0.5f, bottom - h * 0.1f)
                lineTo(left + w * 0.25f, bottom)
                lineTo(left, bottom - h * 0.1f)
                close()
            }
            drawPath(path = path, color = color, style = stroke)
            drawLine(
                color = color,
                start = Offset(left + w * 0.22f, center.y - h * 0.14f),
                end = Offset(right - w * 0.22f, center.y - h * 0.14f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(left + w * 0.22f, center.y + h * 0.04f),
                end = Offset(right - w * 0.34f, center.y + h * 0.04f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(left + w * 0.22f, center.y + h * 0.22f),
                end = Offset(right - w * 0.22f, center.y + h * 0.22f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // 2: Currency Coin with Inner Ring & Rupee / Bar Motif
        2 -> {
            drawCircle(
                color = color,
                radius = r * 0.78f,
                center = center,
                style = stroke
            )
            drawCircle(
                color = color,
                radius = r * 0.56f,
                center = center,
                style = Stroke(width = strokeWidth * 0.75f)
            )
            val s = r * 0.26f
            drawLine(
                color = color,
                start = Offset(center.x - s, center.y - s),
                end = Offset(center.x + s, center.y - s),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(center.x - s, center.y - s * 0.25f),
                end = Offset(center.x + s * 0.8f, center.y - s * 0.25f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(center.x - s * 0.4f, center.y - s * 0.25f),
                end = Offset(center.x + s * 0.7f, center.y + s * 1.05f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // 3: Bar Chart & Upward Growth Trend Line
        3 -> {
            val base = center.y + r * 0.65f
            val left = center.x - r * 0.68f
            val right = center.x + r * 0.68f
            drawLine(
                color = color,
                start = Offset(left, base),
                end = Offset(right, base),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            val bw = r * 0.24f
            drawRoundRect(
                color = color,
                topLeft = Offset(left + r * 0.1f, base - r * 0.48f),
                size = Size(bw, r * 0.48f),
                cornerRadius = CornerRadius(4f, 4f),
                style = stroke
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - bw / 2f, base - r * 0.82f),
                size = Size(bw, r * 0.82f),
                cornerRadius = CornerRadius(4f, 4f),
                style = stroke
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(right - r * 0.1f - bw, base - r * 1.18f),
                size = Size(bw, r * 1.18f),
                cornerRadius = CornerRadius(4f, 4f),
                style = stroke
            )
        }

        // 4: Shopping Bag with Curved Handle
        4 -> {
            val w = r * 1.26f
            val h = r * 1.24f
            val top = center.y - h * 0.35f
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - w / 2f, top),
                size = Size(w, h),
                cornerRadius = CornerRadius(r * 0.18f, r * 0.18f),
                style = stroke
            )
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - w * 0.26f, top - r * 0.42f),
                size = Size(w * 0.52f, r * 0.84f),
                style = stroke
            )
        }

        // 5: Wallet with Clasp Flap
        5 -> {
            val w = r * 1.56f
            val h = r * 1.14f
            val topLeft = Offset(center.x - w / 2f, center.y - h / 2f)
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = Size(w, h),
                cornerRadius = CornerRadius(r * 0.2f, r * 0.2f),
                style = stroke
            )
            val flapW = w * 0.42f
            val flapH = h * 0.44f
            drawRoundRect(
                color = color,
                topLeft = Offset(topLeft.x + w - flapW, center.y - flapH / 2f),
                size = Size(flapW, flapH),
                cornerRadius = CornerRadius(r * 0.14f, r * 0.14f),
                style = stroke
            )
            drawCircle(
                color = color,
                radius = strokeWidth * 1.1f,
                center = Offset(topLeft.x + w - flapW * 0.62f, center.y)
            )
        }

        // 6: Family Home / Household Roof & Pillars
        6 -> {
            val roofW = r * 1.56f
            val roofTop = center.y - r * 0.76f
            val roofBase = center.y - r * 0.08f
            val roofPath = Path().apply {
                moveTo(center.x - roofW / 2f, roofBase)
                lineTo(center.x, roofTop)
                lineTo(center.x + roofW / 2f, roofBase)
            }
            drawPath(path = roofPath, color = color, style = stroke)
            val bodyW = r * 1.12f
            val bodyH = r * 0.78f
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - bodyW / 2f, roofBase),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(r * 0.1f, r * 0.1f),
                style = stroke
            )
        }

        // 7: Price Tag / Category Badge
        7 -> {
            val w = r * 1.44f
            val h = r * 0.94f
            val left = center.x - w / 2f
            val right = center.x + w / 2f
            val top = center.y - h / 2f
            val bottom = center.y + h / 2f
            val path = Path().apply {
                moveTo(left + w * 0.28f, top)
                lineTo(right, top)
                lineTo(right, bottom)
                lineTo(left + w * 0.28f, bottom)
                lineTo(left, center.y)
                close()
            }
            drawPath(path = path, color = color, style = stroke)
            drawCircle(
                color = color,
                radius = strokeWidth * 1.15f,
                center = Offset(left + w * 0.34f, center.y)
            )
        }

        // 8: Pie / Donut Budget Chart
        8 -> {
            drawCircle(
                color = color,
                radius = r * 0.76f,
                center = center,
                style = stroke
            )
            drawLine(
                color = color,
                start = center,
                end = Offset(center.x, center.y - r * 0.76f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = color,
                start = center,
                end = Offset(center.x + r * 0.64f, center.y + r * 0.38f),
                strokeWidth = strokeWidth
            )
        }

        // 9: Upward Trend Spark Arrow
        else -> {
            val path = Path().apply {
                moveTo(center.x - r * 0.68f, center.y + r * 0.44f)
                lineTo(center.x - r * 0.16f, center.y - r * 0.08f)
                lineTo(center.x + r * 0.14f, center.y + r * 0.20f)
                lineTo(center.x + r * 0.68f, center.y - r * 0.44f)
            }
            drawPath(path = path, color = color, style = stroke)
            drawLine(
                color = color,
                start = Offset(center.x + r * 0.32f, center.y - r * 0.44f),
                end = Offset(center.x + r * 0.68f, center.y - r * 0.44f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(center.x + r * 0.68f, center.y - r * 0.44f),
                end = Offset(center.x + r * 0.68f, center.y - r * 0.08f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Draws the Minimal Ledger Geometric pattern inside a tile.
 */
private fun DrawScope.drawMinimalLedgerMotif(
    motifIndex: Int,
    center: Offset,
    r: Float,
    color: Color,
    strokeWidth: Float
) {
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    when (motifIndex % 4) {
        0 -> {
            drawCircle(color = color, radius = r * 0.6f, center = center, style = stroke)
            drawCircle(color = color, radius = r * 0.22f, center = center, style = stroke)
        }
        1 -> {
            val d = r * 0.62f
            val path = Path().apply {
                moveTo(center.x, center.y - d)
                lineTo(center.x + d, center.y)
                lineTo(center.x, center.y + d)
                lineTo(center.x - d, center.y)
                close()
            }
            drawPath(path = path, color = color, style = stroke)
        }
        2 -> {
            val s = r * 0.48f
            drawLine(color = color, start = Offset(center.x - s, center.y), end = Offset(center.x + s, center.y), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(center.x, center.y - s), end = Offset(center.x, center.y + s), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
        else -> {
            val w = r * 0.9f
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - w / 2f, center.y - w / 2f),
                size = Size(w, w),
                cornerRadius = CornerRadius(r * 0.2f, r * 0.2f),
                style = stroke
            )
        }
    }
}

/**
 * WhatsApp-Style Finance Emoji & Icon Tiled Wallpaper (`AppDecoratedBackground`):
 * - Renders a repeating staggered grid of crisp vector finance doodles and/or finance emojis across the app.
 * - Automatically changes colors and contrast based on the active App Theme (Light vs. AMOLED Dark `#000000`).
 * - Fully customizable in Settings: Turn On/Off, Pattern Style, Color Accent, Density, and Intensity/Opacity.
 */
@Composable
fun AppDecoratedBackground(
    modifier: Modifier = Modifier,
    overrideSettings: WallpaperSettings? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val observedSettings by LocalAccountSessionManager.wallpaperSettings.collectAsStateWithLifecycle()
    val settings = overrideSettings ?: observedSettings
    val bgColor = MaterialTheme.colorScheme.background
    val isDark = bgColor.luminance() < 0.2f
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val palette = remember(settings.colorAccent, isDark, primaryColor, secondaryColor, tertiaryColor) {
        resolveWallpaperPalette(
            accent = settings.colorAccent,
            isDark = isDark,
            primaryThemeColor = primaryColor,
            secondaryThemeColor = secondaryColor,
            tertiaryThemeColor = tertiaryColor
        )
    }

    val effectiveAlpha = (settings.opacityPercent.coerceIn(4, 28) / 100f) * if (isDark) 0.92f else 1.0f
    val cellSpacingDp = when (settings.density) {
        WallpaperDensity.COMPACT -> 66.dp
        WallpaperDensity.BALANCED -> 86.dp
        WallpaperDensity.AIRY -> 110.dp
    }

    // Subtle WhatsApp-inspired ambient radial tint over the theme background
    val wallpaperTintColor = palette.firstOrNull()?.copy(alpha = if (isDark) effectiveAlpha * 0.16f else effectiveAlpha * 0.12f)
        ?: Color.Transparent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        if (settings.enabled) {
            // 1. Subtle Ambient Theme-Adaptive Gradient Wash
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                wallpaperTintColor,
                                Color.Transparent,
                                wallpaperTintColor.copy(alpha = wallpaperTintColor.alpha * 0.65f)
                            )
                        )
                    )
            )

            // 2. Vector Doodle & Micro-Sparkle Tiled Canvas Layer
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("whatsapp_finance_wallpaper_canvas")
            ) {
                val stepPx = cellSpacingDp.toPx().coerceAtLeast(48f)
                val cols = (size.width / stepPx).toInt() + 2
                val rows = (size.height / stepPx).toInt() + 2
                val iconRadius = stepPx * 0.22f
                val strokeWidth = (stepPx * 0.024f).coerceIn(1.6f, 3.4f)

                for (row in 0 until rows) {
                    val staggerX = if (row % 2 == 1) stepPx * 0.5f else 0f
                    for (col in 0 until cols) {
                        val hash = (row * 31 + col * 17).let { if (it < 0) -it else it }
                        val cx = col * stepPx + staggerX + (stepPx * 0.25f)
                        val cy = row * stepPx + (stepPx * 0.25f)
                        if (cx > size.width + stepPx || cy > size.height + stepPx) continue

                        val tileColor = palette[hash % palette.size].copy(alpha = effectiveAlpha)
                        val angle = ((hash % 5) - 2) * 11f

                        // In FINANCE_ICONS_AND_EMOJIS mode, odd cells are drawn as emojis in the overlay below
                        val skipVectorForEmoji = settings.style == WallpaperPatternStyle.FINANCE_ICONS_AND_EMOJIS &&
                            ((row + col) % 2 == 1)

                        if (!skipVectorForEmoji) {
                            rotate(degrees = angle, pivot = Offset(cx, cy)) {
                                if (settings.style == WallpaperPatternStyle.MINIMAL_GEOMETRIC) {
                                    drawMinimalLedgerMotif(
                                        motifIndex = hash,
                                        center = Offset(cx, cy),
                                        r = iconRadius,
                                        color = tileColor,
                                        strokeWidth = strokeWidth
                                    )
                                } else {
                                    drawFinanceDoodleMotif(
                                        motifIndex = hash,
                                        center = Offset(cx, cy),
                                        r = iconRadius,
                                        color = tileColor,
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }
                        }

                        // WhatsApp-style micro-dots & tiny stars between primary tiles
                        val dotCenter = Offset(cx + stepPx * 0.48f, cy + stepPx * 0.46f)
                        if (dotCenter.x < size.width && dotCenter.y < size.height) {
                            val dotColor = palette[(hash + 1) % palette.size].copy(alpha = effectiveAlpha * 0.55f)
                            if (hash % 3 == 0) {
                                val arm = iconRadius * 0.22f
                                drawLine(
                                    color = dotColor,
                                    start = Offset(dotCenter.x - arm, dotCenter.y),
                                    end = Offset(dotCenter.x + arm, dotCenter.y),
                                    strokeWidth = strokeWidth * 0.7f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = dotColor,
                                    start = Offset(dotCenter.x, dotCenter.y - arm),
                                    end = Offset(dotCenter.x, dotCenter.y + arm),
                                    strokeWidth = strokeWidth * 0.7f,
                                    cap = StrokeCap.Round
                                )
                            } else {
                                drawCircle(
                                    color = dotColor,
                                    radius = strokeWidth * 0.9f,
                                    center = dotCenter
                                )
                            }
                        }
                    }
                }
            }

            // 3. Tiled Finance Emojis Layer (when FINANCE_ICONS_AND_EMOJIS is selected)
            if (settings.style == WallpaperPatternStyle.FINANCE_ICONS_AND_EMOJIS) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val cols = (maxWidth / cellSpacingDp).toInt().coerceIn(2, 10) + 1
                    val rows = (maxHeight / cellSpacingDp).toInt().coerceIn(3, 18) + 1
                    val emojiFontSize = when (settings.density) {
                        WallpaperDensity.COMPACT -> 16.sp
                        WallpaperDensity.BALANCED -> 19.sp
                        WallpaperDensity.AIRY -> 22.sp
                    }
                    for (r in 0 until rows) {
                        val staggerDp = if (r % 2 == 1) cellSpacingDp * 0.5f else 0.dp
                        for (c in 0 until cols) {
                            if ((r + c) % 2 == 1) {
                                val idx = (r * 7 + c * 13) % FINANCE_WALLPAPER_EMOJIS.size
                                val rot = (((r * 11 + c * 5) % 5) - 2) * 10f
                                Text(
                                    text = FINANCE_WALLPAPER_EMOJIS[idx],
                                    fontSize = emojiFontSize,
                                    modifier = Modifier
                                        .offset(
                                            x = cellSpacingDp * c + staggerDp + cellSpacingDp * 0.14f,
                                            y = cellSpacingDp * r + cellSpacingDp * 0.14f
                                        )
                                        .rotate(rot)
                                        .alpha((effectiveAlpha * 1.15f).coerceIn(0.04f, 0.34f))
                                )
                            }
                        }
                    }
                }
            }
        }

        content()
    }
}

/**
 * Compact live preview banner used inside Settings -> Appearance so the user can immediately
 * see how their WhatsApp-style Finance Wallpaper looks as they customize it.
 */
@Composable
fun FinanceWallpaperPreviewBanner(
    settings: WallpaperSettings,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val styleSymbol = when (settings.style) {
        WallpaperPatternStyle.WHATSAPP_DOODLES -> "💳 🧾"
        WallpaperPatternStyle.FINANCE_ICONS_AND_EMOJIS -> "💰 📊"
        WallpaperPatternStyle.MINIMAL_GEOMETRIC -> "❖ ◇"
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(dimens.barChartHeight * 0.56f)
            .clip(RoundedCornerShape(dimens.smallCornerRadius))
            .testTag("wallpaper_live_preview_banner"),
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        color = MaterialTheme.colorScheme.background
    ) {
        AppDecoratedBackground(
            modifier = Modifier.fillMaxSize(),
            overrideSettings = settings
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimens.horizontalPadding * 0.65f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = if (settings.enabled) {
                            "$styleSymbol  ${settings.opacityPercent}%"
                        } else {
                            "⊘"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            horizontal = dimens.horizontalPadding * 0.75f,
                            vertical = dimens.itemSpacing * 0.35f
                        )
                    )
                }
            }
        }
    }
}

fun formatDayMonthLabel(millis: Long): String {
    return SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))
}

enum class TransactionPageSubTab(val label: String) {
    TRANSACTIONS("Transactions"),
    CATEGORIES("Categories"),
    MERCHANTS("Merchants")
}

/**
 * 3-Tab Sub-Navigation Bar (`Transactions` | `Categories` | `Merchants`) with a glowing
 * green underline indicator on the selected tab, matching the reference design.
 */
@Composable
fun TransactionPageSubTabBar(
    selectedTab: TransactionPageSubTab,
    onSelectTab: (TransactionPageSubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val performHaptic = rememberAppHapticPerformer()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tx_subtab_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionPageSubTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        BudgetSafeGreen
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "subtab_text_color"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .clickable {
                            performHaptic()
                            onSelectTab(tab)
                        }
                        .padding(top = 10.dp)
                        .testTag("tx_subtab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            BudgetSafeGreen,
                                            BudgetSafeGreen,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        )
    }
}

/**
 * Horizontally scrollable monthly spend bar strip (e.g. May, Jun 2.20L, Jul 1.67L, Aug 2.58L, Sep 2.09L, Oct 2.41L, Nov 40.9K)
 * where the selected month is highlighted in a tall dark rounded card and clicking any month bar selects that month.
 */
@Composable
fun AxioMonthlySpendBarStrip(
    bars: List<MonthlySpendBar>,
    @Suppress("UNUSED_PARAMETER") currencySymbol: String,
    onSelectMonth: (YearMonthPeriod) -> Unit,
    modifier: Modifier = Modifier,
    useIncomeBars: Boolean = false
) {
    if (bars.isEmpty()) return
    val performHaptic = rememberAppHapticPerformer()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val maxMetric = remember(bars, useIncomeBars) {
        (bars.maxOfOrNull { if (useIncomeBars) it.incomeTotal else it.expenseTotal } ?: 1.0).coerceAtLeast(1.0)
    }
    val barColor = if (useIncomeBars) BudgetSafeGreen else Color(0xFF4C6EF5)

    LaunchedEffect(bars.size) {
        val selectedIdx = bars.indexOfFirst { it.isSelected }.takeIf { it >= 0 } ?: (bars.size - 1)
        val scrollPx = with(density) { ((selectedIdx - 2).coerceAtLeast(0) * 62).dp.roundToPx() }
        scrollState.scrollTo(scrollPx)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("axio_monthly_spend_bar_strip"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            val metricValue = if (useIncomeBars) bar.incomeTotal else bar.expenseTotal
            val fraction = if (metricValue > 0.0) {
                (metricValue / maxMetric).toFloat().coerceIn(0.10f, 1f)
            } else {
                0.08f
            }
            val barHeight = (74.dp * fraction).coerceAtLeast(8.dp)
            val amountCompact = if (metricValue > 0.0) {
                formatCompactCurrency(metricValue, "").trim()
            } else {
                "0"
            }
            val selectedBg = if (bar.isSelected) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                Color.Transparent
            }
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .height(132.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(selectedBg)
                    .clickable {
                        performHaptic()
                        onSelectMonth(bar.period)
                    }
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .testTag("axio_month_bar_${bar.period.year}_${bar.period.month}"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = amountCompact,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = if (bar.isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (bar.isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (bar.isSelected) {
                                barColor
                            } else {
                                barColor.copy(alpha = 0.82f)
                            }
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (bar.isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (bar.isSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Circular Spend & Budget Ring Gauge + Clickable Income | Budget 2-Column Summary Row
 * matching Screenshot 1 (`Screenshot_20261115_000840_axio_a1d8c879.jpg`).
 */
@Composable
fun CircularSpendBudgetRingGauge(
    totalSpent: Double,
    totalIncome: Double,
    monthlyBudget: Double,
    currencySymbol: String,
    selectedTypeFilter: String = "All",
    onSpendRingClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val performHaptic = rememberAppHapticPerformer()
    val hasBudget = monthlyBudget > 0.0
    val budgetRatio = if (hasBudget) (totalSpent / monthlyBudget).toFloat().coerceAtLeast(0f) else 0.72f
    val budgetPercent = if (hasBudget) ((totalSpent / monthlyBudget) * 100.0).roundToInt() else 0
    val isOverBudget = hasBudget && totalSpent > monthlyBudget
    val blueRingColor = Color(0xFF4C6EF5)
    val coralOverflowColor = Color(0xFFFF8787)
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
    val surfaceBg = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("tx_page_spend_ring_gauge"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(214.dp)
                .clip(CircleShape)
                .clickable {
                    performHaptic()
                    onSpendRingClick()
                }
                .testTag("tx_ring_spend_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                val strokePx = 14.dp.toPx()
                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension - strokePx) / 2f

                // Background ring track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Primary blue spend arc
                val primarySweep = if (!hasBudget) {
                    if (totalSpent > 0.0) 270f else 18f
                } else {
                    (budgetRatio.coerceIn(0.04f, 1f) * 360f)
                }
                drawArc(
                    color = blueRingColor,
                    startAngle = -90f,
                    sweepAngle = primarySweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Coral overflow arc when over budget (or coral tip segment matching Screenshot 1)
                val tipAngleDeg = if (isOverBudget) {
                    val overFraction = ((budgetRatio - 1f).coerceIn(0.08f, 0.45f))
                    val overflowSweep = overFraction * 360f
                    drawArc(
                        color = coralOverflowColor,
                        startAngle = -90f,
                        sweepAngle = overflowSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    -90f + overflowSweep
                } else {
                    -90f + primarySweep
                }

                // Glowing red/coral indicator dot on the arc tip
                val rad = Math.toRadians(tipAngleDeg.toDouble())
                val dotCenter = Offset(
                    x = center.x + (radius * kotlin.math.cos(rad)).toFloat(),
                    y = center.y + (radius * kotlin.math.sin(rad)).toFloat()
                )
                drawCircle(
                    color = Color.White,
                    radius = 6.5.dp.toPx(),
                    center = dotCenter
                )
                drawCircle(
                    color = if (isOverBudget) BudgetDangerCoral else blueRingColor,
                    radius = 4.5.dp.toPx(),
                    center = dotCenter
                )
                drawCircle(
                    color = surfaceBg,
                    radius = 1.5.dp.toPx(),
                    center = dotCenter
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedTypeFilter.equals("INCOME", ignoreCase = true)) "↙" else "↗",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatCurrency(totalSpent, currencySymbol),
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clickable {
                        performHaptic()
                        onBudgetClick()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "⏲",
                            style = MaterialTheme.typography.labelSmall,
                            color = blueRingColor
                        )
                        Text(
                            text = if (hasBudget) "$budgetPercent%" else "Set budget",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) BudgetDangerCoral else blueRingColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Two-Column Clickable Summary Row: Income | Budget
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        performHaptic()
                        onIncomeClick()
                    }
                    .padding(horizontal = 6.dp, vertical = 8.dp)
                    .testTag("tx_ring_income_button"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCurrency(totalIncome, currencySymbol),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = if (selectedTypeFilter.equals("INCOME", ignoreCase = true)) {
                        BudgetSafeGreen
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        performHaptic()
                        onBudgetClick()
                    }
                    .padding(horizontal = 6.dp, vertical = 8.dp)
                    .testTag("tx_ring_budget_button"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasBudget) formatCurrency(monthlyBudget, currencySymbol) else "Set budget >",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = if (hasBudget) MaterialTheme.colorScheme.onSurface else BudgetSafeGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class CategorySubTabItem(
    val category: String,
    val spent: Double,
    val spendCount: Int,
    val budgetLimit: Double
)

/**
 * Categories Sub-Tab Content matching Screenshot 2 (`Screenshot_20261115_000921_axio_c0979023.jpg`):
 * - Multi-segment Category Donut Ring on the left + Clickable Percentage Legend on the right.
 * - Two-column `Spends | Budget` summary row.
 * - Clickable Category Cards with icon badge, Category Name, `"N Spends"`, total amount (coral if over budget),
 *   and `"of ₹X >"` or green `"Set budget >"`.
 */
@Composable
fun TransactionCategoriesSubTabContent(
    categories: List<CategorySubTabItem>,
    totalSpent: Double,
    monthlyBudget: Double,
    currencySymbol: String,
    selectedCategory: String = "All",
    onCategoryClick: (String) -> Unit,
    onSetCategoryBudgetClick: (String, Double) -> Unit,
    onEditMonthlyBudgetClick: () -> Unit = {},
    onAddCustomCategoryClick: () -> Unit = {},
    incomeCategories: List<CategorySubTabItem> = emptyList(),
    totalIncome: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val customEmojis = rememberCustomCategoryEmojis()
    val totalCategorySpend = remember(categories) {
        categories.sumOf { it.spent }.coerceAtLeast(1.0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding, vertical = 8.dp)
            .testTag("tx_categories_subtab_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (categories.isNotEmpty()) {
            // 1. Multi-Segment Donut Chart on Left + Clickable Percentage Legend on Right (responsive width)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val donutSize = if (maxWidth < 340.dp) 128.dp else 160.dp
                val donutGap = if (maxWidth < 340.dp) 12.dp else 16.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(donutGap)
                ) {
                    Box(
                        modifier = Modifier
                            .size(donutSize)
                            .testTag("tx_categories_donut_chart"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .pointerInput(categories) {
                                    detectTapGestures { tapOffset ->
                                        val cx = size.width / 2f
                                        val cy = size.height / 2f
                                        val dx = tapOffset.x - cx
                                        val dy = tapOffset.y - cy
                                        val dist = hypot(dx, dy)
                                        val outerR = minOf(size.width, size.height).toFloat() / 2f
                                        val innerR = outerR * 0.48f
                                        if (dist in innerR..outerR) {
                                            var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                            if (deg < 0f) deg += 360f
                                            var running = 0f
                                            for (item in categories) {
                                                val sweep = ((item.spent / totalCategorySpend) * 360.0).toFloat()
                                                if (deg >= running && deg <= running + sweep) {
                                                    performHaptic()
                                                    onCategoryClick(item.category)
                                                    break
                                                }
                                                running += sweep
                                            }
                                        }
                                    }
                                }
                        ) {
                            val strokePx = if (donutSize < 140.dp) 22.dp.toPx() else 26.dp.toPx()
                            val arcSize = Size(size.width - strokePx, size.height - strokePx)
                            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                            var startAngle = -90f
                            categories.forEach { item ->
                                val sweep = ((item.spent / totalCategorySpend) * 360.0).toFloat().coerceAtLeast(2f)
                                val color = categoryAccentColor(item.category)
                                val isSelected = selectedCategory != "All" &&
                                    selectedCategory.equals(item.category, ignoreCase = true)
                                drawArc(
                                    color = if (selectedCategory == "All" || isSelected) color else color.copy(alpha = 0.35f),
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(
                                        width = if (isSelected) strokePx * 1.12f else strokePx,
                                        cap = StrokeCap.Butt
                                    )
                                )
                                startAngle += sweep
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        categories.take(9).forEach { item ->
                            val pct = (item.spent / totalCategorySpend) * 100.0
                            val pctText = String.format(Locale.US, "%.1f%%", pct)
                            val catColor = categoryAccentColor(item.category)
                            val safeCatId = item.category.lowercase().replace(Regex("[^a-z0-9]+"), "_")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        performHaptic()
                                        onCategoryClick(item.category)
                                    }
                                    .padding(horizontal = 4.dp, vertical = 3.dp)
                                    .testTag("tx_category_legend_$safeCatId"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${item.category} $pctText",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (selectedCategory.equals(item.category, ignoreCase = true)) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Medium
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Spends | Budget Two-Column Summary Row (adapts to selected category when drilled in)
        val hasSelectedCat = selectedCategory != "All" && selectedCategory.isNotBlank()
        val selectedExpenseCat = if (hasSelectedCat) {
            categories.firstOrNull { it.category.equals(selectedCategory, ignoreCase = true) }
        } else null
        val selectedIncomeCat = if (hasSelectedCat) {
            incomeCategories.firstOrNull { it.category.equals(selectedCategory, ignoreCase = true) }
        } else null

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        selectedIncomeCat != null -> selectedIncomeCat.category
                        selectedExpenseCat != null -> selectedExpenseCat.category
                        hasSelectedCat -> selectedCategory
                        else -> "Spends"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val displayedTotal = when {
                    selectedIncomeCat != null -> selectedIncomeCat.spent
                    selectedExpenseCat != null -> selectedExpenseCat.spent
                    hasSelectedCat -> 0.0
                    else -> totalSpent
                }
                Text(
                    text = formatCurrency(displayedTotal, currencySymbol),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = if (selectedIncomeCat != null) BudgetSafeGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        performHaptic()
                        when {
                            selectedExpenseCat != null -> onSetCategoryBudgetClick(
                                selectedExpenseCat.category,
                                selectedExpenseCat.budgetLimit
                            )
                            selectedIncomeCat == null && !hasSelectedCat -> onEditMonthlyBudgetClick()
                        }
                    }
                    .padding(vertical = 4.dp)
                    .testTag("tx_categories_monthly_budget_button"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedIncomeCat != null) {
                    Text(
                        text = "Entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${selectedIncomeCat.spendCount}",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    val activeCap = selectedExpenseCat?.budgetLimit ?: monthlyBudget
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (activeCap > 0.0) formatCurrency(activeCap, currencySymbol) else "Set budget >",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = if (activeCap > 0.0) MaterialTheme.colorScheme.onSurface else BudgetSafeGreen
                    )
                }
            }
        }

        // 2B. Category Budgets & Custom Category Action Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tap any category limit to set or edit its budget",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = {
                    performHaptic()
                    onAddCustomCategoryClick()
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("add_category_budget_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Custom Category",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Custom Category")
            }
        }

        // 3. Category Cards List (when a category is selected, show ONLY that category's card)
        val displayedExpenseCategories = if (hasSelectedCat) {
            categories.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        } else {
            categories
        }
        displayedExpenseCategories.forEach { item ->
            val catColor = categoryAccentColor(item.category)
            val customEmoji = customEmojis[item.category.trim().lowercase()]
            val isOverLimit = item.budgetLimit > 0.0 && item.spent > item.budgetLimit
            val safeCatId = item.category.lowercase().replace(Regex("[^a-z0-9]+"), "_")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        performHaptic()
                        onCategoryClick(item.category)
                    }
                    .testTag("tx_category_card_$safeCatId"),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selectedCategory.equals(item.category, ignoreCase = true)) {
                        catColor
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SquircleIconBadge(
                        icon = categoryIcon(item.category),
                        tint = catColor,
                        containerColor = catColor.copy(alpha = 0.16f),
                        size = 44.dp,
                        iconSize = 22.dp,
                        emoji = customEmoji
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.spendCount} ${if (item.spendCount == 1) "Spend" else "Spends"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatCurrency(item.spent, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                            fontWeight = FontWeight.Bold,
                            color = if (isOverLimit) BudgetDangerCoral else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    performHaptic()
                                    onSetCategoryBudgetClick(item.category, item.budgetLimit)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("tx_category_set_budget_$safeCatId")
                        ) {
                            Text(
                                text = if (item.budgetLimit > 0.0) {
                                    "of ${formatCurrency(item.budgetLimit, currencySymbol)}"
                                } else {
                                    "Set budget"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.budgetLimit > 0.0) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    BudgetSafeGreen
                                }
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (item.budgetLimit > 0.0) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    BudgetSafeGreen
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Income vs Spend & Cashback Breakdown (filtered cleanly when a category is selected)
        val displayedIncomeCategories = if (hasSelectedCat) {
            incomeCategories.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        } else {
            incomeCategories
        }
        if (!hasSelectedCat && (incomeCategories.isNotEmpty() || totalIncome > 0.0)) {
            val resolvedIncomeTotal = if (totalIncome > 0.0) totalIncome else incomeCategories.sumOf { it.spent }
            val netFlow = resolvedIncomeTotal - totalSpent
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_income_spend_division_card"),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Income vs Spend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (netFlow >= 0.0) {
                                BudgetSafeGreen.copy(alpha = 0.14f)
                            } else {
                                BudgetDangerCoral.copy(alpha = 0.14f)
                            }
                        ) {
                            Text(
                                text = if (netFlow >= 0.0) {
                                    "Net +${formatCurrency(netFlow, currencySymbol)}"
                                } else {
                                    "Net -${formatCurrency(-netFlow, currencySymbol)}"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = if (netFlow >= 0.0) BudgetSafeGreen else BudgetDangerCoral,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = BudgetSafeGreen.copy(alpha = 0.12f)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    text = "Income & Cashback",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BudgetSafeGreen,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "+${formatCurrency(resolvedIncomeTotal, currencySymbol)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = BudgetSafeGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("analytics_division_income_total")
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    text = "Total Spend",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "-${formatCurrency(totalSpent, currencySymbol)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("analytics_division_spend_total")
                                )
                            }
                        }
                    }
                }
            }
        }

        if (displayedIncomeCategories.isNotEmpty()) {
            Text(
                text = "Income & Cashback Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("analytics_income_categories_section")
            )
            displayedIncomeCategories.forEach { item ->
                    val catColor = categoryAccentColor(item.category)
                    val customEmoji = customEmojis[item.category.trim().lowercase()]
                    val safeCatId = item.category.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable {
                                performHaptic()
                                onCategoryClick(item.category)
                            }
                            .testTag("analysis_income_category_card_$safeCatId"),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selectedCategory.equals(item.category, ignoreCase = true)) {
                                catColor
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SquircleIconBadge(
                                icon = categoryIcon(item.category),
                                tint = catColor,
                                containerColor = catColor.copy(alpha = 0.16f),
                                size = 44.dp,
                                iconSize = 22.dp,
                                emoji = customEmoji
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${item.spendCount} ${if (item.spendCount == 1) "Entry" else "Entries"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "+${formatCurrency(item.spent, currencySymbol)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = BudgetSafeGreen
                            )
                        }
                    }
                }
            }
    }
}

data class MerchantSubTabItem(
    val merchantName: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val primaryCategory: String = "Shopping"
)

/**
 * Merchants Sub-Tab Content matching Screenshot 3 (`Screenshot_20261115_000929_axio_510d00fc.jpg`):
 * Lists each merchant with a green storefront icon badge, Merchant Name, `"N Spends"`, and bold total amount.
 * Clicking any merchant card filters transactions to show that same merchant / transaction.
 */
@Composable
fun TransactionMerchantsSubTabContent(
    merchants: List<MerchantSubTabItem>,
    currencySymbol: String,
    selectedMerchant: String = "All",
    onMerchantClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()

    val hasSelectedMerchant = selectedMerchant != "All" && selectedMerchant.isNotBlank()
    val normalizedSelectedKey = selectedMerchant.trim().replace(Regex("\\s+"), " ").lowercase()
    val displayedMerchants = remember(merchants, selectedMerchant, hasSelectedMerchant, normalizedSelectedKey) {
        if (hasSelectedMerchant) {
            merchants.filter {
                it.merchantName.trim().replace(Regex("\\s+"), " ").lowercase() == normalizedSelectedKey
            }
        } else {
            merchants
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding, vertical = 8.dp)
            .testTag("tx_merchants_subtab_content"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (displayedMerchants.isEmpty() && !hasSelectedMerchant) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No merchant transactions in this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            displayedMerchants.forEach { item ->
                val safeMerchantId = item.merchantName.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                val isSelected = selectedMerchant != "All" &&
                    selectedMerchant.equals(item.merchantName, ignoreCase = true)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable {
                            performHaptic()
                            onMerchantClick(item.merchantName)
                        }
                        .testTag("tx_merchant_card_$safeMerchantId"),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) {
                            BudgetSafeGreen
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Default.ShoppingBag,
                            tint = BudgetSafeGreen,
                            containerColor = BudgetSafeGreen.copy(alpha = 0.16f),
                            size = 44.dp,
                            iconSize = 22.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.merchantName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.transactionCount} ${if (item.transactionCount == 1) "Spend" else "Spends"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formatCurrency(item.totalAmount, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}



