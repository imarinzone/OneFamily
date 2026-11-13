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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalAccountSessionManager
import com.example.data.model.BudgetScope
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
import com.example.ui.theme.categoryAccentColor
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.MonthlySpendBar
import com.example.ui.viewmodel.YearMonthPeriod
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
        lower.contains("salary") || lower.contains("income") -> Icons.Default.Payments
        else -> Icons.Default.Category
    }
}

@Composable
fun ScopeSwitcherBar(
    selectedScope: BudgetScope,
    hasHousehold: Boolean,
    householdName: String?,
    onScopeSelected: (BudgetScope) -> Unit
) {
    val dimens = LocalAppDimens.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding, vertical = dimens.itemSpacing * 0.35f),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.horizontalPadding * 0.25f),
            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f)
        ) {
            BudgetScope.entries.forEach { scope ->
                val isSelected = selectedScope == scope
                val activeColor = if (scope == BudgetScope.HOUSEHOLD_SHARED) {
                    HouseholdBadgeIndigo
                } else {
                    PersonalBadgeTeal
                }
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) activeColor else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "scope_bg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
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
                        .clickable { onScopeSelected(scope) }
                        .padding(vertical = dimens.itemSpacing * 0.8f)
                        .testTag("scope_chip_${scope.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (scope == BudgetScope.HOUSEHOLD_SHARED) Icons.Default.Groups else Icons.Default.Lock,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerModal(
    initialStartMillis: Long? = null,
    initialEndMillis: Long? = null,
    onDismiss: () -> Unit,
    onApplyRange: (Long, Long) -> Unit
) {
    val pickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMillis,
        initialSelectedEndDateMillis = initialEndMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val start = pickerState.selectedStartDateMillis
                    val end = pickerState.selectedEndDateMillis ?: start
                    if (start != null && end != null) {
                        onApplyRange(start, end)
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
            title = null,
            showModeToggle = true,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
        )
    }
}

/**
 * Top Gesture Date Bar (`MonthPeriodSelectorBar`):
 * - Gesture-driven bar where the Year is displayed on top and the Month name is displayed below.
 * - Left: Previous Year (top) & `< PrevMonth` (bottom)
 * - Middle: Current Year (top) & Full Month Name (bottom)
 * - Right: Next Year (top) & `NextMonth >` (bottom)
 * - Full-bar directional swapping animation (`AnimatedContent` wrapping the entire bar) plus live finger-tracking drag offset.
 */
@Composable
fun MonthPeriodSelectorBar(
    selectedPeriod: YearMonthPeriod?,
    periodLabel: String,
    onStepMonth: (Int) -> Unit,
    onSelectCurrentMonth: () -> Unit,
    onToggleAllTime: () -> Unit = {},
    selectedDateRangePreset: DateRangePreset = if (selectedPeriod == null) DateRangePreset.ALL_TIME else DateRangePreset.MONTH,
    onSelectDateRangePreset: ((DateRangePreset) -> Unit)? = null,
    onSelectMonthPeriod: ((YearMonthPeriod) -> Unit)? = null,
    customStartDateMillis: Long? = null,
    customEndDateMillis: Long? = null,
    onSelectCustomDateRange: ((Long, Long) -> Unit)? = null,
    showDateRangeChips: Boolean = false
) {
    val dimens = LocalAppDimens.current
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
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.65f)
    ) {
        // Gesture Date Bar: Entire bar animates/swaps together, with Year on top and Month name below
        Surface(
            shape = RoundedCornerShape(dimens.cardCornerRadius * 1.15f),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            tonalElevation = 2.dp,
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
                                onStepMonth(-1)
                            } else if (dragAccumulated < -swipeThreshold) {
                                onStepMonth(1)
                            }
                            dragAccumulated = 0f
                        }
                    )
                }
                .testTag("top_month_switcher_bar")
        ) {
            AnimatedContent(
                targetState = Triple(activePeriod, selectedDateRangePreset, periodLabel),
                transitionSpec = {
                    val movingForward = targetState.first >= initialState.first
                    val enterSlide = slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetX = { fullWidth -> if (movingForward) fullWidth else -fullWidth }
                    ) + fadeIn()
                    val exitSlide = slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        targetOffsetX = { fullWidth -> if (movingForward) -fullWidth else fullWidth }
                    ) + fadeOut()
                    enterSlide.togetherWith(exitSlide)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(animatedDragOffset.roundToInt(), 0) },
                label = "full_bar_month_swap_animation"
            ) { (periodState, presetState, labelState) ->
                val prevPeriod = periodState.previousMonth()
                val nextPeriod = periodState.nextMonth()
                val topYearLabel = if (presetState == DateRangePreset.MONTH) {
                    periodState.year.toString()
                } else {
                    presetState.label
                }
                val bottomMonthLabel = if (presetState == DateRangePreset.MONTH) {
                    periodState.formatFullMonth()
                } else {
                    labelState
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimens.horizontalPadding * 0.45f,
                            vertical = dimens.itemSpacing * 0.45f
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Previous Month (Year on top, < Month below)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(dimens.smallCornerRadius))
                                .clickable { onStepMonth(-1) }
                                .padding(
                                    horizontal = dimens.horizontalPadding * 0.45f,
                                    vertical = dimens.itemSpacing * 0.35f
                                )
                                .testTag("prev_month_button"),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = prevPeriod.year.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Month",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.78f)
                                )
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.12f))
                                Text(
                                    text = prevPeriod.formatShortMonth(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        // Middle: Current Selected Period (Year on top, Month Name below)
                        Surface(
                            shape = RoundedCornerShape(dimens.cardCornerRadius),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(dimens.cardCornerRadius))
                                .clickable { showCustomRangePicker = true }
                                .testTag("center_month_year_pill")
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = dimens.horizontalPadding * 1.15f,
                                    vertical = dimens.itemSpacing * 0.45f
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = topYearLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Text(
                                    text = bottomMonthLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1
                                )
                            }
                        }

                        // Right: Next Month (Year on top, Month > below)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(dimens.smallCornerRadius))
                                .clickable { onStepMonth(1) }
                                .padding(
                                    horizontal = dimens.horizontalPadding * 0.45f,
                                    vertical = dimens.itemSpacing * 0.35f
                                )
                                .testTag("next_month_button"),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = nextPeriod.year.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nextPeriod.formatShortMonth(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.12f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Month",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.78f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.3f))

                    // Subtle Gesture Pill Handle at the bottom of the gesture bar
                    Box(
                        modifier = Modifier
                            .width(dimens.screenWidth * 0.11f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f))
                    )
                }
            }
        }

        if (showDateRangeChips) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.5f)
            ) {
                val isCustomSelected = selectedDateRangePreset == DateRangePreset.CUSTOM
                val customChipLabel = if (isCustomSelected && customStartDateMillis != null && customEndDateMillis != null) {
                    periodLabel
                } else {
                    "Custom Range"
                }
                FilterChip(
                    selected = isCustomSelected,
                    onClick = { showCustomRangePicker = true },
                    shape = RoundedCornerShape(50),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(dimens.rowIconSize * 0.72f)
                        )
                    },
                    label = { Text(customChipLabel) },
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
    remainingDays: Int,
    currencySymbol: String,
    periodLabel: String,
    household: Household?,
    monthlyTrendBars: List<MonthlySpendBar> = emptyList(),
    onEditBudgetClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val hasAssignedBudget = monthlyBudget > 0.0
    val rawProgress = if (hasAssignedBudget) (totalSpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "budget_ring_progress"
    )
    val pct = if (hasAssignedBudget) ((totalSpent / monthlyBudget) * 100).toInt() else 0
    val statusColor = when {
        pct >= 100 -> BudgetDangerCoral
        pct >= 80 -> BudgetWarnAmber
        else -> BudgetSafeGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.horizontalPadding)
            .clickable { onEditBudgetClick() },
        shape = RoundedCornerShape(dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(dimens.cardPadding)
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
                    Text(
                        text = formatCurrency(totalSpent, currencySymbol),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                    IconButton(
                        onClick = onEditBudgetClick,
                        modifier = Modifier
                            .size(dimens.rowIconBoxSize * 0.75f)
                            .testTag("edit_monthly_budget_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                        )
                    }
                }

                if (hasAssignedBudget) {
                    Box(
                        modifier = Modifier.size(dimens.pulseRingSize),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(modifier = Modifier.size(dimens.pulseRingSize * 0.9f)) {
                            val strokeWidth = size.minDimension * 0.13f
                            drawArc(
                                color = trackColor,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = statusColor,
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onEditBudgetClick,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(dimens.rowIconSize * 0.72f)
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                        Text("Set Budget")
                    }
                }
            }

            if (hasAssignedBudget) {
                Spacer(modifier = Modifier.height(dimens.itemSpacing))

                val isAboveBudget = totalSpent > monthlyBudget
                val diffAmount = kotlin.math.abs(monthlyBudget - totalSpent)
                val statusPillLabel = if (isAboveBudget) {
                    "▲ ${formatCurrency(diffAmount, currencySymbol)} above"
                } else {
                    "▼ ${formatCurrency(diffAmount, currencySymbol)} below"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f)
                ) {
                    MetricMiniPill(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_budget_status_pill"),
                        text = statusPillLabel,
                        accentColor = if (isAboveBudget) BudgetDangerCoral else BudgetSafeGreen
                    )
                    MetricMiniPill(
                        modifier = Modifier.weight(1f),
                        text = "${formatCurrency(dailySafeSpend, currencySymbol)}/d",
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }

                if (monthlyTrendBars.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.85f))
                    HomeBudgetTrendMiniStrip(
                        bars = monthlyTrendBars,
                        overallBudget = monthlyBudget,
                        currencySymbol = currencySymbol
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBudgetTrendMiniStrip(
    bars: List<MonthlySpendBar>,
    overallBudget: Double,
    currencySymbol: String
) {
    val dimens = LocalAppDimens.current
    val hasIncome = bars.any { it.incomeTotal > 0.0 }
    val maxIncome = bars.maxOfOrNull { it.incomeTotal } ?: 0.0
    val maxRef = maxOf(
        bars.maxOfOrNull { it.expenseTotal } ?: 1.0,
        overallBudget * 1.15,
        if (hasIncome) maxIncome * 1.12 else 0.0,
        1.0
    )
    val selectedBar = bars.firstOrNull { it.isSelected } ?: bars.lastOrNull()
    val selectedActual = selectedBar?.expenseTotal ?: 0.0
    val selectedIncome = selectedBar?.incomeTotal?.takeIf { it > 0.0 }
        ?: bars.lastOrNull { it.incomeTotal > 0.0 }?.incomeTotal
        ?: 0.0
    val isSelectedAbove = selectedActual > overallBudget
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_budget_trend_line_section"),
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimens.horizontalPadding * 0.75f,
                    vertical = dimens.itemSpacing * 0.6f
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget ${formatCompactCurrency(overallBudget, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasIncome) {
                        Text(
                            text = "● Income ${formatCompactCurrency(selectedIncome, currencySymbol)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PersonalBadgeTeal
                        )
                    }
                }
                Text(
                    text = if (isSelectedAbove) "▲ Above Budget" else "▼ Below Budget",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelectedAbove) BudgetDangerCoral else BudgetSafeGreen
                )
            }
            Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.35f))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("home_budget_trend_canvas")
            ) {
                val topPad = 5.dp.toPx()
                val bottomPad = 5.dp.toPx()
                val usableH = (size.height - topPad - bottomPad).coerceAtLeast(8f)
                val budgetFraction = (overallBudget / maxRef).toFloat().coerceIn(0.06f, 0.96f)
                val budgetY = size.height - bottomPad - usableH * budgetFraction

                // Dashed horizontal overall budget threshold line
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, budgetY),
                    end = Offset(size.width, budgetY),
                    strokeWidth = 1.8.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                )

                val count = bars.size.coerceAtLeast(1)
                val slotW = size.width / count

                // Optional Salary / Income trend line when income data is available
                if (hasIncome) {
                    val incomePts = bars.mapIndexed { idx, bar ->
                        val frac = if (bar.incomeTotal > 0.0) {
                            (bar.incomeTotal / maxRef).toFloat().coerceIn(0.06f, 0.98f)
                        } else {
                            0.04f
                        }
                        val x = slotW * (idx + 0.5f)
                        val y = size.height - bottomPad - usableH * frac
                        Offset(x, y)
                    }
                    if (incomePts.size >= 2) {
                        val incomePath = Path().apply {
                            moveTo(incomePts.first().x, incomePts.first().y)
                            for (i in 1 until incomePts.size) {
                                lineTo(incomePts[i].x, incomePts[i].y)
                            }
                        }
                        drawPath(
                            path = incomePath,
                            color = PersonalBadgeTeal,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                    incomePts.forEachIndexed { idx, pt ->
                        if (bars[idx].incomeTotal > 0.0) {
                            val r = if (bars[idx].isSelected) 3.8.dp.toPx() else 2.8.dp.toPx()
                            drawCircle(color = surfaceColor, radius = r + 1.2.dp.toPx(), center = pt)
                            drawCircle(color = PersonalBadgeTeal, radius = r, center = pt)
                        }
                    }
                }

                val points = bars.mapIndexed { idx, bar ->
                    val frac = (bar.expenseTotal / maxRef).toFloat().coerceIn(0.04f, 1f)
                    val x = slotW * (idx + 0.5f)
                    val y = size.height - bottomPad - usableH * frac
                    Offset(x, y)
                }

                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(
                            width = 2.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                points.forEachIndexed { idx, pt ->
                    val bar = bars[idx]
                    val nodeColor = if (bar.expenseTotal > overallBudget) BudgetDangerCoral else BudgetSafeGreen
                    val radius = if (bar.isSelected) 4.8.dp.toPx() else 3.4.dp.toPx()
                    drawCircle(
                        color = surfaceColor,
                        radius = radius + 1.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = nodeColor,
                        radius = radius,
                        center = pt
                    )
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
    val dimens = LocalAppDimens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = dimens.horizontalPadding * 0.85f,
                vertical = dimens.itemSpacing * 0.8f
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
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
    onStepMonth: ((Int) -> Unit)? = null
) {
    val dimens = LocalAppDimens.current
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
    var dragAccum by remember { mutableFloatStateOf(0f) }

    val selectedBar = bars.firstOrNull { it.isSelected } ?: bars.lastOrNull()
    val selectedSpend = selectedBar?.expenseTotal ?: 0.0
    val selectedIncomeDisplay = selectedBar?.incomeTotal?.takeIf { it > 0.0 }
        ?: bars.lastOrNull { it.incomeTotal > 0.0 }?.incomeTotal
        ?: 0.0
    val isSelectedAboveBudget = hasOverallBudget && selectedSpend > overallBudget
    val budgetDiff = kotlin.math.abs(overallBudget - selectedSpend)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val budgetLineColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.45f)
    ) {
        if (hasOverallBudget) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overall_budget_trend_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                ) {
                    Text(
                        text = "- - Budget ${formatCompactCurrency(overallBudget, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            horizontal = dimens.horizontalPadding * 0.65f,
                            vertical = dimens.itemSpacing * 0.3f
                        )
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
                    color = badgeColor.copy(alpha = 0.15f),
                    modifier = Modifier.testTag("overall_budget_status_pill")
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        modifier = Modifier.padding(
                            horizontal = dimens.horizontalPadding * 0.7f,
                            vertical = dimens.itemSpacing * 0.3f
                        )
                    )
                }
            }
        }

        if (hasIncome) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_income_trend_legend"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PersonalBadgeTeal.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "● Salary / Income ${formatCompactCurrency(selectedIncomeDisplay, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PersonalBadgeTeal,
                        modifier = Modifier.padding(
                            horizontal = dimens.horizontalPadding * 0.65f,
                            vertical = dimens.itemSpacing * 0.3f
                        )
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
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = netColor
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(bars) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccum = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccum += dragAmount
                        },
                        onDragEnd = {
                            val currentSelected = bars.firstOrNull { it.isSelected } ?: bars.lastOrNull()
                            val threshold = size.width * 0.14f
                            if (dragAccum > threshold) {
                                if (onStepMonth != null) {
                                    onStepMonth(-1)
                                } else if (currentSelected != null) {
                                    onSelectMonth(currentSelected.period.previousMonth())
                                }
                            } else if (dragAccum < -threshold) {
                                if (onStepMonth != null) {
                                    onStepMonth(1)
                                } else if (currentSelected != null) {
                                    onSelectMonth(currentSelected.period.nextMonth())
                                }
                            }
                            dragAccum = 0f
                        }
                    )
                }
        ) {
            val barWidth = (maxWidth / (bars.size.coerceAtLeast(1) * 2.2f)).coerceIn(18.dp, 36.dp)
            val maxBarHeight = dimens.barChartHeight * 0.58f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimens.barChartHeight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = dimens.itemSpacing * 0.5f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    bars.forEach { bar ->
                        val heightFraction = (bar.expenseTotal / maxReference).toFloat().coerceIn(0.08f, 1f)
                        val isBarAboveBudget = hasOverallBudget && bar.expenseTotal > overallBudget
                        val barColor = when {
                            hasOverallBudget && bar.isSelected -> if (isBarAboveBudget) BudgetDangerCoral else BudgetSafeGreen
                            hasOverallBudget -> if (isBarAboveBudget) {
                                BudgetDangerCoral.copy(alpha = 0.62f)
                            } else {
                                BudgetSafeGreen.copy(alpha = 0.58f)
                            }
                            bar.isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
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
                                .weight(1f)
                                .clip(RoundedCornerShape(dimens.smallCornerRadius * 0.8f))
                                .clickable { onSelectMonth(bar.period) }
                                .padding(
                                    horizontal = dimens.horizontalPadding * 0.15f,
                                    vertical = dimens.itemSpacing * 0.3f
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = amountText,
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    hasOverallBudget && bar.expenseTotal > 0 -> if (isBarAboveBudget) BudgetDangerCoral else BudgetSafeGreen
                                    bar.isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (bar.isSelected || hasOverallBudget) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.45f))
                            Box(
                                modifier = Modifier
                                    .width(barWidth)
                                    .height((maxBarHeight * heightFraction).coerceAtLeast(8.dp))
                                    .clip(RoundedCornerShape(50))
                                    .background(barColor)
                            )
                            Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.45f))
                            Text(
                                text = bar.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (bar.isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (bar.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Overlay Canvas for Salary / Income Trend Line (if available)
                if (hasIncome && bars.isNotEmpty()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("salary_income_trend_canvas")
                    ) {
                        val bottomLabelReservePx = (dimens.itemSpacing * 1.1f + 18.dp).toPx()
                        val maxBarAreaPx = maxBarHeight.toPx()
                        val barBottomY = (size.height - bottomLabelReservePx).coerceAtLeast(maxBarAreaPx)
                        val slotWidth = size.width / bars.size.coerceAtLeast(1)

                        val incomePoints = bars.mapIndexed { index, bar ->
                            val frac = if (bar.incomeTotal > 0.0) {
                                (bar.incomeTotal / maxReference).toFloat().coerceIn(0.08f, 1f)
                            } else {
                                0.04f
                            }
                            val cx = slotWidth * (index + 0.5f)
                            val cy = (barBottomY - maxBarAreaPx * frac).coerceIn(6.dp.toPx(), barBottomY - 4.dp.toPx())
                            Offset(cx, cy)
                        }

                        if (incomePoints.size >= 2) {
                            val incomePath = Path().apply {
                                moveTo(incomePoints.first().x, incomePoints.first().y)
                                for (i in 1 until incomePoints.size) {
                                    lineTo(incomePoints[i].x, incomePoints[i].y)
                                }
                            }
                            drawPath(
                                path = incomePath,
                                color = PersonalBadgeTeal,
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        incomePoints.forEachIndexed { index, point ->
                            val bar = bars[index]
                            if (bar.incomeTotal > 0.0) {
                                val nodeRadius = if (bar.isSelected) 5.2.dp.toPx() else 3.8.dp.toPx()
                                drawCircle(
                                    color = surfaceColor,
                                    radius = nodeRadius + 1.6.dp.toPx(),
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

                // Overlay Canvas for Overall Budget Reference Line & Actual Spend Trend Line
                if (hasOverallBudget && bars.isNotEmpty()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("overall_budget_trend_canvas")
                    ) {
                        val bottomLabelReservePx = (dimens.itemSpacing * 1.1f + 18.dp).toPx()
                        val maxBarAreaPx = maxBarHeight.toPx()
                        val barBottomY = (size.height - bottomLabelReservePx).coerceAtLeast(maxBarAreaPx)

                        val budgetFraction = (overallBudget / maxReference).toFloat().coerceIn(0.08f, 1f)
                        val budgetY = (barBottomY - maxBarAreaPx * budgetFraction).coerceIn(8.dp.toPx(), barBottomY - 4.dp.toPx())

                        // 1. Horizontal Dashed Overall Budget Threshold Line
                        drawLine(
                            color = budgetLineColor.copy(alpha = 0.78f),
                            start = Offset(4.dp.toPx(), budgetY),
                            end = Offset(size.width - 4.dp.toPx(), budgetY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )

                        // 2. Actual Spend Trend Line connecting each month's spend
                        val slotWidth = size.width / bars.size.coerceAtLeast(1)
                        val trendPoints = bars.mapIndexed { index, bar ->
                            val frac = (bar.expenseTotal / maxReference).toFloat().coerceIn(0.08f, 1f)
                            val cx = slotWidth * (index + 0.5f)
                            val cy = (barBottomY - maxBarAreaPx * frac).coerceIn(6.dp.toPx(), barBottomY - 4.dp.toPx())
                            Offset(cx, cy)
                        }

                        if (trendPoints.size >= 2) {
                            val trendPath = Path().apply {
                                moveTo(trendPoints.first().x, trendPoints.first().y)
                                for (i in 1 until trendPoints.size) {
                                    lineTo(trendPoints[i].x, trendPoints[i].y)
                                }
                            }
                            drawPath(
                                path = trendPath,
                                color = primaryColor,
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // 3. Trend Nodes colored by whether actual spend was Below or Above overall budget
                        trendPoints.forEachIndexed { index, point ->
                            val bar = bars[index]
                            val isAbove = bar.expenseTotal > overallBudget
                            val nodeColor = if (isAbove) BudgetDangerCoral else BudgetSafeGreen
                            val nodeRadius = if (bar.isSelected) 5.5.dp.toPx() else 4.dp.toPx()
                            drawCircle(
                                color = surfaceColor,
                                radius = nodeRadius + 1.8.dp.toPx(),
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
            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f),
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
            OutlinedButton(
                onClick = onAddAccountClick,
                shape = RoundedCornerShape(dimens.smallCornerRadius),
                modifier = Modifier
                    .height(dimens.navPillHeight)
                    .testTag("add_account_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(dimens.rowIconSize * 0.8f))
                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
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
    val dimens = LocalAppDimens.current
    val icon = when (accountType) {
        "CREDIT_CARD" -> Icons.Default.CreditCard
        "UPI" -> Icons.Default.QrCode2
        "CASH" -> Icons.Default.Payments
        else -> Icons.Default.AccountBalance
    }
    Surface(
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimens.horizontalPadding * 0.85f,
                vertical = dimens.itemSpacing * 0.8f
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimens.rowIconBoxSize * 0.78f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.6f))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.6f))
            Text(
                text = amountLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

/**
 * Single-Line Transaction Row with Relative Screen Sizing & Gesture Controls:
 * - Tap anywhere on Row -> Opens full Receipt Editor (`onEditTransaction`)
 * - Long-Press Row -> Opens Quick Category Picker (`onQuickChangeCategory`)
 * - Swipe Right -> Prompts user to Link Duplicate SMS / Transaction (`onLinkDuplicate`)
 * - Swipe Left -> Delete Transaction (`onDelete`)
 */
@Composable
fun ExpenseTransactionItemCard(
    expense: ExpenseTransaction,
    currencySymbol: String,
    canTogglePrivacy: Boolean = false,
    onTogglePrivacy: () -> Unit = {},
    onLinkDuplicate: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEditTransaction: () -> Unit = {},
    onQuickChangeCategory: () -> Unit = {},
    onTagClick: (String) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val isIncome = expense.type == "INCOME"
    val catColor = categoryAccentColor(expense.category)
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var cardWidthPx by remember { mutableFloatStateOf(1f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tx_swipe_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { cardWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .clip(RoundedCornerShape(dimens.cardCornerRadius))
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
                            modifier = Modifier.size(dimens.rowIconSize)
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.4f))
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
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.4f))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Transaction",
                            tint = bgRevealColor,
                            modifier = Modifier.size(dimens.rowIconSize)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .clip(RoundedCornerShape(dimens.cardCornerRadius))
                .pointerInput(expense.id) {
                    detectTapGestures(
                        onTap = { onEditTransaction() },
                        onLongPress = { onQuickChangeCategory() }
                    )
                }
                .pointerInput(expense.id, cardWidthPx) {
                    val maxDrag = cardWidthPx * 0.42f
                    val triggerThreshold = cardWidthPx * 0.20f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val next = dragOffsetX + dragAmount
                            dragOffsetX = next.coerceIn(-maxDrag, maxDrag)
                        },
                        onDragEnd = {
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
                            dragOffsetX = 0f
                        }
                    )
                }
                .testTag("expense_card_${expense.id}"),
            shape = RoundedCornerShape(dimens.cardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimens.horizontalPadding,
                        vertical = dimens.itemSpacing * 1.05f
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dimens.rowIconBoxSize)
                        .clip(RoundedCornerShape(dimens.smallCornerRadius))
                        .background(catColor.copy(alpha = 0.16f))
                        .clickable { onEditTransaction() }
                        .testTag("category_pill_${expense.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(expense.category),
                        contentDescription = expense.category,
                        tint = catColor,
                        modifier = Modifier.size(dimens.rowIconSize)
                    )
                }

                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.85f))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (expense.linkedSmsSignatures.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                            modifier = Modifier.testTag("linked_sms_badge_${expense.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = dimens.horizontalPadding * 0.45f,
                                    vertical = 1.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.58f)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
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

                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))

                Text(
                    text = "${if (isIncome) "+" else "-"}${formatCurrency(expense.amount, currencySymbol)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) BudgetSafeGreen else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.2f))

                IconButton(
                    onClick = onLinkDuplicate,
                    modifier = Modifier
                        .size(dimens.rowIconBoxSize * 0.72f)
                        .testTag("link_duplicate_button_${expense.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Link Duplicate SMS / Transaction",
                        tint = if (expense.linkedSmsSignatures.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        },
                        modifier = Modifier.size(dimens.rowIconSize * 0.78f)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Pie / Donut Chart with Relative Screen Sizing:
 * Clicking directly on any pie slice on the Canvas OR clicking any category in the legend
 * invokes `onCategoryClick(category)` to display that category's transactions.
 */
@Composable
fun CategoryDonutChart(
    categoryTotals: List<Pair<String, Double>>,
    currencySymbol: String,
    selectedCategory: String? = null,
    onCategoryClick: (String) -> Unit = {}
) {
    val dimens = LocalAppDimens.current
    val palette = listOf(
        Color(0xFFFF5A5F), // Warm Coral
        Color(0xFFFFB703), // Sunny Marigold
        Color(0xFF06D6A0), // Fresh Mint
        Color(0xFF8338EC), // Berry Pop
        Color(0xFFEC4899), // Pink Pop
        Color(0xFF118AB2), // Ocean Pop
        Color(0xFFF97316), // Tangerine
        Color(0xFF14B8A6), // Teal
        Color(0xFFA855F7), // Purple
        Color(0xFFEAB308)  // Gold
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
                                    onCategoryClick(cat)
                                    break
                                }
                                accumulated += sweep
                            }
                        }
                    }
            ) {
                val baseStroke = size.minDimension * 0.19f
                val selectedStroke = size.minDimension * 0.24f
                var startAngle = -90f
                visibleSlices.forEachIndexed { idx, (cat, amount) ->
                    val sweep = ((amount / grandTotal) * 360.0).toFloat()
                    val isSelected = selectedCategory != null && selectedCategory.equals(cat, ignoreCase = true)
                    val stroke = if (isSelected) selectedStroke else baseStroke
                    val sliceColor = if (selectedCategory == null || isSelected) {
                        palette[idx % palette.size]
                    } else {
                        palette[idx % palette.size].copy(alpha = 0.45f)
                    }
                    drawArc(
                        color = sliceColor,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt),
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke)
                    )
                    startAngle += sweep
                }
            }
            Text(
                text = "${categoryTotals.size}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(dimens.horizontalPadding))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.45f)
        ) {
            visibleSlices.forEachIndexed { idx, (category, amount) ->
                val pct = ((amount / grandTotal) * 100).toInt()
                val isSelected = selectedCategory != null && selectedCategory.equals(category, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(dimens.smallCornerRadius * 0.7f),
                    color = if (isSelected) {
                        palette[idx % palette.size].copy(alpha = 0.16f)
                    } else {
                        Color.Transparent
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimens.smallCornerRadius * 0.7f))
                        .clickable { onCategoryClick(category) }
                        .testTag("pie_slice_legend_${category.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimens.horizontalPadding * 0.4f,
                                vertical = dimens.itemSpacing * 0.35f
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(dimens.rowIconSize * 0.45f)
                                    .clip(CircleShape)
                                    .background(palette[idx % palette.size])
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "$pct% (${formatCurrency(amount, currencySymbol)})",
                            style = MaterialTheme.typography.labelSmall,
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
    paperColor: Color = MaterialTheme.colorScheme.surface,
    isTop: Boolean = true
) {
    val dimens = LocalAppDimens.current
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimens.itemSpacing * 0.8f)
    ) {
        val teethCount = 24
        val toothWidth = size.width / teethCount
        val path = androidx.compose.ui.graphics.Path().apply {
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
            .padding(vertical = dimens.itemSpacing * 0.65f)
            .height(2.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 2.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        )
    }
}

@Composable
fun ReceiptTicketCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ReceiptZigzagEdgeStrip(paperColor = containerColor, isTop = true)
        Surface(
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = 2.dp,
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
    val dimens = LocalAppDimens.current
    val activePrimary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("theme_color_swatch_bar"),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimens.horizontalPadding * 0.5f,
                    vertical = dimens.itemSpacing * 0.55f
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.example.ui.theme.AppThemeMode.entries.forEach { mode ->
                val isSelected = currentMode == mode
                val ringScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "theme_swatch_scale"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "theme_swatch_border"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onSelectMode(mode) }
                        .testTag("theme_mode_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.size((34.dp * ringScale).coerceIn(30.dp, 42.dp))
                    ) {
                        val radius = size.minDimension / 2f
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)

                        when (mode) {
                            com.example.ui.theme.AppThemeMode.SYSTEM -> {
                                // Dual-tone split swatch: Warm Cream (Light) + AMOLED Black (Dark) + Active Primary Dot
                                drawArc(
                                    color = Color(0xFFFAF6F0),
                                    startAngle = 90f,
                                    sweepAngle = 180f,
                                    useCenter = true,
                                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                                    size = Size(radius * 2f, radius * 2f)
                                )
                                drawArc(
                                    color = Color(0xFF050505),
                                    startAngle = -90f,
                                    sweepAngle = 180f,
                                    useCenter = true,
                                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                                    size = Size(radius * 2f, radius * 2f)
                                )
                                drawCircle(
                                    color = activePrimary,
                                    radius = radius * 0.42f,
                                    center = centerOffset
                                )
                            }
                            com.example.ui.theme.AppThemeMode.LIGHT -> {
                                // Warm Cream & Active Primary Light Swatch
                                drawCircle(
                                    color = Color(0xFFFAF6F0),
                                    radius = radius,
                                    center = centerOffset
                                )
                                drawCircle(
                                    color = activePrimary.copy(alpha = 0.35f),
                                    radius = radius * 0.62f,
                                    center = centerOffset
                                )
                                drawCircle(
                                    color = activePrimary,
                                    radius = radius * 0.36f,
                                    center = centerOffset
                                )
                            }
                            com.example.ui.theme.AppThemeMode.DARK -> {
                                // Pure AMOLED Black & Active Primary Dark Swatch
                                drawCircle(
                                    color = Color(0xFF000000),
                                    radius = radius,
                                    center = centerOffset
                                )
                                drawCircle(
                                    color = Color(0xFF1F1F26),
                                    radius = radius * 0.66f,
                                    center = centerOffset
                                )
                                drawCircle(
                                    color = activePrimary,
                                    radius = radius * 0.36f,
                                    center = centerOffset
                                )
                            }
                        }

                        // Outer selection ring
                        drawCircle(
                            color = borderColor,
                            radius = radius - 1.5.dp.toPx(),
                            center = centerOffset,
                            style = Stroke(width = if (isSelected) 3.dp.toPx() else 1.5.dp.toPx())
                        )
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
                // Curated 16-swatch spectrum grid (2 rows of 8)
                CUSTOM_COLOR_SPECTRUM_SWATCHES.chunked(8).forEach { rowSwatches ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowSwatches.forEach { swatchArgb ->
                            val isMatch = (currentArgb and 0xFFFFFFL) == (swatchArgb and 0xFFFFFFL)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(swatchArgb))
                                    .border(
                                        width = if (isMatch) 2.5.dp else 1.dp,
                                        color = if (isMatch) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        red = ((swatchArgb shr 16) and 0xFF).toFloat()
                                        green = ((swatchArgb shr 8) and 0xFF).toFloat()
                                        blue = (swatchArgb and 0xFF).toFloat()
                                    }
                                    .testTag("custom_swatch_${String.format(Locale.US, "%06x", swatchArgb and 0xFFFFFFL)}")
                            )
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


