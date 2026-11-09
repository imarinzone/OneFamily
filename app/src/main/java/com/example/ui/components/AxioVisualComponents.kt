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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.data.model.Household
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
    val lower = category.lowercase()
    return when {
        lower.contains("grocer") -> Icons.Default.LocalGroceryStore
        lower.contains("food") || lower.contains("dining") -> Icons.Default.Fastfood
        lower.contains("rent") || lower.contains("housing") -> Icons.Default.Home
        lower.contains("fuel") || lower.contains("travel") -> Icons.Default.DirectionsCar
        lower.contains("shop") -> Icons.Default.ShoppingBag
        lower.contains("health") -> Icons.Default.MedicalServices
        lower.contains("entertain") -> Icons.Default.Movie
        lower.contains("invest") || lower.contains("salary") || lower.contains("income") -> Icons.AutoMirrored.Filled.TrendingUp
        else -> Icons.AutoMirrored.Filled.ReceiptLong
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
                    DateRangePreset.LAST_30_DAYS to "Last 30 Days",
                    DateRangePreset.LAST_7_DAYS to "Last 7 Days",
                    DateRangePreset.LAST_90_DAYS to "Last 3 Months",
                    DateRangePreset.THIS_YEAR to "This Year",
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f)
                ) {
                    val leftAmount = (monthlyBudget - totalSpent).coerceAtLeast(0.0)
                    MetricMiniPill(
                        modifier = Modifier.weight(1f),
                        text = "${formatCurrency(leftAmount, currencySymbol)} left",
                        accentColor = statusColor
                    )
                    MetricMiniPill(
                        modifier = Modifier.weight(1f),
                        text = "${formatCurrency(dailySafeSpend, currencySymbol)}/d",
                        accentColor = MaterialTheme.colorScheme.primary
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
    onSelectMonth: (YearMonthPeriod) -> Unit,
    onStepMonth: ((Int) -> Unit)? = null
) {
    val dimens = LocalAppDimens.current
    val maxExpense = bars.maxOfOrNull { it.expenseTotal }?.coerceAtLeast(1.0) ?: 1.0
    var dragAccum by remember { mutableFloatStateOf(0f) }

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
                        val selectedBar = bars.firstOrNull { it.isSelected } ?: bars.lastOrNull()
                        val threshold = size.width * 0.14f
                        if (dragAccum > threshold) {
                            if (onStepMonth != null) {
                                onStepMonth(-1)
                            } else if (selectedBar != null) {
                                onSelectMonth(selectedBar.period.previousMonth())
                            }
                        } else if (dragAccum < -threshold) {
                            if (onStepMonth != null) {
                                onStepMonth(1)
                            } else if (selectedBar != null) {
                                onSelectMonth(selectedBar.period.nextMonth())
                            }
                        }
                        dragAccum = 0f
                    }
                )
            }
    ) {
        val barWidth = (maxWidth / (bars.size.coerceAtLeast(1) * 2.2f)).coerceIn(18.dp, 36.dp)
        val maxBarHeight = dimens.barChartHeight * 0.58f

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimens.barChartHeight)
                .padding(top = dimens.itemSpacing * 0.5f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { bar ->
                val heightFraction = (bar.expenseTotal / maxExpense).toFloat().coerceIn(0.08f, 1f)
                val barColor = if (bar.isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimens.smallCornerRadius * 0.8f))
                        .clickable { onSelectMonth(bar.period) }
                        .padding(
                            horizontal = dimens.horizontalPadding * 0.2f,
                            vertical = dimens.itemSpacing * 0.3f
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (bar.expenseTotal > 0) formatCompactCurrency(bar.expenseTotal, currencySymbol) else "-",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bar.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (bar.isSelected) FontWeight.Bold else FontWeight.Normal,
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
 * - Swipe Right -> Toggle Household / Personal Sharing (`onTogglePrivacy`)
 * - Swipe Left -> Delete Transaction (`onDelete`)
 */
@Composable
fun ExpenseTransactionItemCard(
    expense: ExpenseTransaction,
    currencySymbol: String,
    canTogglePrivacy: Boolean = false,
    onTogglePrivacy: () -> Unit = {},
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
                if (expense.visibility == "PERSONAL") HouseholdBadgeIndigo else PersonalBadgeTeal
            } else {
                BudgetDangerCoral
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(bgRevealColor.copy(alpha = 0.25f))
                    .padding(horizontal = dimens.horizontalPadding * 1.2f),
                contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = if (isSwipingRight) {
                        if (expense.visibility == "PERSONAL") Icons.Default.Groups else Icons.Default.Lock
                    } else {
                        Icons.Default.Delete
                    },
                    contentDescription = null,
                    tint = bgRevealColor,
                    modifier = Modifier.size(dimens.rowIconSize)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .clip(RoundedCornerShape(dimens.cardCornerRadius))
                .pointerInput(expense.id, canTogglePrivacy) {
                    detectTapGestures(
                        onTap = { onEditTransaction() },
                        onLongPress = { onQuickChangeCategory() }
                    )
                }
                .pointerInput(expense.id, canTogglePrivacy, cardWidthPx) {
                    val maxDrag = cardWidthPx * 0.38f
                    val triggerThreshold = cardWidthPx * 0.22f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val next = dragOffsetX + dragAmount
                            dragOffsetX = if (canTogglePrivacy) {
                                next.coerceIn(-maxDrag, maxDrag)
                            } else {
                                next.coerceIn(-maxDrag, 0f)
                            }
                        },
                        onDragEnd = {
                            when {
                                dragOffsetX > triggerThreshold && canTogglePrivacy -> {
                                    dragOffsetX = 0f
                                    onTogglePrivacy()
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
                        vertical = dimens.itemSpacing * 1.1f
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

                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.75f))

                Text(
                    text = "${if (isIncome) "+" else "-"}${formatCurrency(expense.amount, currencySymbol)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) BudgetSafeGreen else MaterialTheme.colorScheme.onSurface
                )
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.horizontalPadding * 0.25f),
            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f)
        ) {
            com.example.ui.theme.AppThemeMode.entries.forEach { mode ->
                val isSelected = currentMode == mode
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "theme_mode_bg"
                )
                val fgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "theme_mode_fg"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = dimens.itemSpacing * 0.65f)
                        .testTag("theme_mode_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = fgColor
                    )
                }
            }
        }
    }
}
