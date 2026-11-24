package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.rememberAppHapticPerformer
import com.example.ui.viewmodel.AccountLimitPeriodUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Material 3 Calendar View (7-column month grid showing all days 1..31) for selecting
 * a credit card's monthly billing cycle start date without scrollable chip tabs.
 */
@Composable
fun BillingCycleCalendarGrid(
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
    referenceMillis: Long = System.currentTimeMillis(),
    tagPrefix: String = "billing_cycle_calendar"
) {
    val performHaptic = rememberAppHapticPerformer()
    val clampedSelected = selectedDay.coerceIn(1, 31)
    var showNativeDatePicker by rememberSaveable { mutableStateOf(false) }

    val monthMeta = remember(referenceMillis) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = referenceMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOffset = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY).coerceIn(0, 6)
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        val todayCal = Calendar.getInstance().apply { timeInMillis = referenceMillis }
        Triple(firstDayOffset, monthName, todayCal.get(Calendar.DAY_OF_MONTH))
    }
    val (firstDayOffset, monthTitle, todayDay) = monthMeta
    val cycleRangeText = remember(clampedSelected, referenceMillis) {
        AccountLimitPeriodUtils.formatBillingCycleRange(
            billingCycleDay = clampedSelected,
            cycleOffset = 0,
            referenceMillis = referenceMillis,
            includeYear = false
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${tagPrefix}_grid"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select Billing Cycle Start Date",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$monthTitle • Days 1–31",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = {
                        performHaptic()
                        showNativeDatePicker = true
                    },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.testTag("${tagPrefix}_selected_badge")
                ) {
                    Text(
                        text = "Day $clampedSelected",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (showNativeDatePicker) {
                val initialDateMs = remember(clampedSelected, referenceMillis) {
                    Calendar.getInstance().apply {
                        timeInMillis = referenceMillis
                        set(
                            Calendar.DAY_OF_MONTH,
                            clampedSelected.coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH))
                        )
                    }.timeInMillis
                }
                AppDatePickerDialog(
                    initialDateMillis = initialDateMs,
                    onDismiss = { showNativeDatePicker = false },
                    onConfirmDate = { _, _, dayOfMonth, _ ->
                        performHaptic()
                        onSelectDay(dayOfMonth.coerceIn(1, 31))
                    }
                )
            }

            // Weekday Headers (S M T W T F S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayHeader ->
                    Text(
                        text = dayHeader,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Calendar Grid of Days 1..31 aligned to current month's starting weekday
            val totalCells = firstDayOffset + 31
            val weekRows = (totalCells + 6) / 7
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (row in 0 until weekRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOffset + 1
                            if (dayNumber in 1..31) {
                                val isSelected = dayNumber == clampedSelected
                                val isToday = dayNumber == todayDay
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            performHaptic()
                                            onSelectDay(dayNumber)
                                        }
                                        .testTag("${tagPrefix}_day_$dayNumber"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .then(
                                                if (isToday && !isSelected) {
                                                    Modifier.border(
                                                        width = 1.dp,
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                                        shape = CircleShape
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = ReceiptMonoFontFamily
                                            ),
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${tagPrefix}_cycle_preview")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Monthly Cycle: $cycleRangeText",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (clampedSelected >= 29) {
                            "Resets monthly on Day $clampedSelected (or last day in 28–30 day months)."
                        } else {
                            "Always monthly • starts on Day $clampedSelected through Day ${if (clampedSelected == 1) "end of month" else clampedSelected - 1} of next month."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Modal Dialog displaying [BillingCycleCalendarGrid] so users can pick a monthly billing cycle
 * start date (1..31) in a genuine Calendar View from Account Detail or Account Limit settings.
 */
@Composable
fun BillingCycleCalendarDialog(
    initialDay: Int,
    accountName: String,
    onDismiss: () -> Unit,
    onConfirmDay: (Int) -> Unit,
    referenceMillis: Long = System.currentTimeMillis()
) {
    val performHaptic = rememberAppHapticPerformer()
    var pickedDay by rememberSaveable(initialDay) { mutableIntStateOf(initialDay.coerceIn(1, 31)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.testTag("billing_cycle_calendar_dialog"),
        title = {
            Column {
                Text(
                    text = "Credit Card Cycle Date",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (accountName.isNotBlank()) {
                    Text(
                        text = accountName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                BillingCycleCalendarGrid(
                    selectedDay = pickedDay,
                    onSelectDay = { pickedDay = it },
                    referenceMillis = referenceMillis,
                    tagPrefix = "billing_cycle_dialog_calendar"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    performHaptic()
                    onConfirmDay(pickedDay)
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("billing_cycle_calendar_confirm_button")
            ) {
                Text("Save Day $pickedDay")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("billing_cycle_calendar_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
