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
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.example.data.model.BudgetScope
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.data.model.Household
import com.example.ui.theme.BudgetDangerCoral
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.BudgetWarnAmber
import com.example.ui.theme.HouseholdBadgeIndigo
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        .padding(vertical = 10.dp)
                        .testTag("scope_chip_${scope.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (scope == BudgetScope.HOUSEHOLD_SHARED) Icons.Default.Groups else Icons.Default.Lock,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
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
                .height(440.dp)
        )
    }
}

@Composable
fun MonthPeriodSelectorBar(
    selectedPeriod: YearMonthPeriod?,
    periodLabel: String,
    onStepMonth: (Int) -> Unit,
    onSelectCurrentMonth: () -> Unit,
    onToggleAllTime: () -> Unit,
    selectedDateRangePreset: DateRangePreset = if (selectedPeriod == null) DateRangePreset.ALL_TIME else DateRangePreset.MONTH,
    onSelectDateRangePreset: ((DateRangePreset) -> Unit)? = null,
    onSelectMonthPeriod: ((YearMonthPeriod) -> Unit)? = null,
    customStartDateMillis: Long? = null,
    customEndDateMillis: Long? = null,
    onSelectCustomDateRange: ((Long, Long) -> Unit)? = null
) {
    val activePeriod = selectedPeriod ?: YearMonthPeriod.current()
    val activeMonthLabel = activePeriod.formatDisplay()
    var showCustomRangePicker by rememberSaveable { mutableStateOf(false) }

    // Build a smooth horizontally scrollable timeline of 31 months (-24 to +6 around current month)
    val monthTimeline = remember {
        val current = YearMonthPeriod.current()
        val list = mutableListOf<YearMonthPeriod>()
        var cursor = current
        repeat(24) { cursor = cursor.previousMonth() }
        repeat(31) {
            list.add(cursor)
            cursor = cursor.nextMonth()
        }
        list.toList()
    }

    val activeIndex = remember(activePeriod, monthTimeline) {
        monthTimeline.indexOf(activePeriod).takeIf { it >= 0 } ?: 24
    }

    val monthListState = rememberLazyListState(
        initialFirstVisibleItemIndex = (activeIndex - 1).coerceAtLeast(0)
    )
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = monthListState)

    // Smoothly scroll the month carousel whenever the active month changes
    LaunchedEffect(activeIndex) {
        val targetScrollIndex = (activeIndex - 1).coerceAtLeast(0)
        runCatching {
            monthListState.animateScrollToItem(targetScrollIndex)
        }
    }

    var dragAccumulated by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Smoothly Scrollable & Swipeable Month Selector Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Swipeable & Animated Month Stepper Pill (< MMM yyyy >)
            Surface(
                shape = RoundedCornerShape(50),
                color = if (selectedDateRangePreset == DateRangePreset.MONTH) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.pointerInput(activePeriod) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccumulated = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulated += dragAmount
                        },
                        onDragEnd = {
                            if (dragAccumulated > 48f) {
                                onStepMonth(-1)
                            } else if (dragAccumulated < -48f) {
                                onStepMonth(1)
                            }
                            dragAccumulated = 0f
                        }
                    )
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { onStepMonth(-1) },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month"
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                if (onSelectDateRangePreset != null) {
                                    onSelectDateRangePreset(DateRangePreset.MONTH)
                                }
                                onSelectCurrentMonth()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AnimatedContent(
                            targetState = activeMonthLabel,
                            transitionSpec = {
                                (slideInHorizontally { width -> width / 2 } + fadeIn())
                                    .togetherWith(slideOutHorizontally { width -> -width / 2 } + fadeOut())
                            },
                            label = "smooth_month_label"
                        ) { targetLabel ->
                            Text(
                                text = targetLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { onStepMonth(1) },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month"
                        )
                    }
                }
            }

            // Smooth Horizontally Scrollable Month Carousel (Snapping LazyRow)
            LazyRow(
                state = monthListState,
                flingBehavior = snapFlingBehavior,
                modifier = Modifier
                    .weight(1f)
                    .testTag("smooth_month_scroll_row"),
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(
                    items = monthTimeline,
                    key = { _, ym -> "${ym.year}_${ym.month}" }
                ) { _, ym ->
                    val isSelectedMonth = selectedDateRangePreset == DateRangePreset.MONTH && ym == activePeriod
                    val bg by animateColorAsState(
                        targetValue = if (isSelectedMonth) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "month_pill_bg"
                    )
                    val fg by animateColorAsState(
                        targetValue = if (isSelectedMonth) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "month_pill_fg"
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = bg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                if (onSelectMonthPeriod != null) {
                                    onSelectMonthPeriod(ym)
                                } else {
                                    val delta = (ym.year - activePeriod.year) * 12 + (ym.month - activePeriod.month)
                                    if (delta == 0) {
                                        onSelectDateRangePreset?.invoke(DateRangePreset.MONTH)
                                    } else {
                                        val stepDir = if (delta > 0) 1 else -1
                                        repeat(kotlin.math.abs(delta)) { onStepMonth(stepDir) }
                                    }
                                }
                            }
                    ) {
                        Text(
                            text = ym.formatDisplay(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelectedMonth) FontWeight.Bold else FontWeight.Medium,
                            color = fg,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Row 2: Date Range Filter Chips + Custom Date Range Picker Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Custom Date Range Chip (opens Material 3 DateRangePicker modal)
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
                        modifier = Modifier.size(16.dp)
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
            .padding(horizontal = 16.dp)
            .clickable { onEditBudgetClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                .padding(20.dp)
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
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onEditBudgetClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_monthly_budget_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (hasAssignedBudget) {
                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(modifier = Modifier.size(84.dp)) {
                            val strokeWidth = 11.dp.toPx()
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Set Budget")
                    }
                }
            }

            if (hasAssignedBudget) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
    val maxExpense = bars.maxOfOrNull { it.expenseTotal }?.coerceAtLeast(1.0) ?: 1.0
    var dragAccum by remember { mutableFloatStateOf(0f) }

    Column(
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
                        if (dragAccum > 56f) {
                            if (onStepMonth != null) {
                                onStepMonth(-1)
                            } else if (selectedBar != null) {
                                onSelectMonth(selectedBar.period.previousMonth())
                            }
                        } else if (dragAccum < -56f) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .padding(top = 8.dp),
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
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectMonth(bar.period) }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height((84.dp * heightFraction).coerceAtLeast(8.dp))
                            .clip(RoundedCornerShape(50))
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
            OutlinedButton(
                onClick = onAddAccountClick,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .height(52.dp)
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
    val icon = when (accountType) {
        "CREDIT_CARD" -> Icons.Default.CreditCard
        "UPI" -> Icons.Default.QrCode2
        "CASH" -> Icons.Default.Payments
        else -> Icons.Default.AccountBalance
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

/**
 * Single-Line Transaction Row with Gesture Controls:
 * - Shows ONLY Category Icon, Merchant Name, and Amount on one clean horizontal line.
 * - Tap Icon -> Quick Category Picker
 * - Tap Row -> Interactive Receipt Editor
 * - Long-Press Row -> Quick Category Picker
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
    val isIncome = expense.type == "INCOME"
    val catColor = categoryAccentColor(expense.category)
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tx_swipe_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
    ) {
        // Gesture Reveal Background (Swipe Right = Share/Private, Swipe Left = Delete)
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
                    .padding(horizontal = 20.dp),
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
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .clip(RoundedCornerShape(22.dp))
                .pointerInput(expense.id, canTogglePrivacy) {
                    detectTapGestures(
                        onTap = { onEditTransaction() },
                        onLongPress = { onQuickChangeCategory() }
                    )
                }
                .pointerInput(expense.id, canTogglePrivacy) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val next = dragOffsetX + dragAmount
                            dragOffsetX = if (canTogglePrivacy) {
                                next.coerceIn(-180f, 180f)
                            } else {
                                next.coerceIn(-180f, 0f)
                            }
                        },
                        onDragEnd = {
                            when {
                                dragOffsetX > 100f && canTogglePrivacy -> {
                                    dragOffsetX = 0f
                                    onTogglePrivacy()
                                }
                                dragOffsetX < -100f -> {
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
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Punchy Category Icon Squircle (tap to quick-change category)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(catColor.copy(alpha = 0.16f))
                        .clickable { onQuickChangeCategory() }
                        .testTag("category_pill_${expense.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(expense.category),
                        contentDescription = expense.category,
                        tint = catColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Single-Line Merchant Name Only
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Bold Amount Only
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
 * Interactive Pie / Donut Chart:
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
    val palette = listOf(
        Color(0xFFFF5A5F), // Warm Coral
        Color(0xFFFFB703), // Sunny Marigold
        Color(0xFF06D6A0), // Fresh Mint
        Color(0xFF8338EC), // Berry Pop
        Color(0xFFEC4899), // Pink Pop
        Color(0xFF118AB2), // Ocean Pop
        Color(0xFFF97316)  // Tangerine
    )
    val visibleSlices = categoryTotals.take(palette.size)
    val grandTotal = visibleSlices.sumOf { it.second }.coerceAtLeast(1.0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .testTag("category_pie_chart_canvas"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(116.dp)
                    .pointerInput(visibleSlices) {
                        detectTapGestures { tapOffset ->
                            if (visibleSlices.isEmpty()) return@detectTapGestures
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val dx = tapOffset.x - cx
                            val dy = tapOffset.y - cy
                            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                            val outerRadius = size.width / 2f
                            // Ignore taps far outside the circle
                            if (distance > outerRadius * 1.15f) return@detectTapGestures

                            // Convert tap angle to [0, 360) clockwise starting from -90 deg (12 o'clock)
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
                val baseStroke = 22.dp.toPx()
                val selectedStroke = 27.dp.toPx()
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            visibleSlices.forEachIndexed { idx, (category, amount) ->
                val pct = ((amount / grandTotal) * 100).toInt()
                val isSelected = selectedCategory != null && selectedCategory.equals(category, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        palette[idx % palette.size].copy(alpha = 0.16f)
                    } else {
                        Color.Transparent
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCategoryClick(category) }
                        .testTag("pie_slice_legend_${category.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
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
                                    .clip(CircleShape)
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
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
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
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        .padding(vertical = 8.dp)
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
