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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.LocalAccountDataStore
import com.example.data.local.SmsRawRecord
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.components.categoryIcon
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.PersonalBadgeTeal
import com.example.ui.theme.categoryAccentColor
import com.example.ui.viewmodel.CategorySpendProgress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun formatExactDate(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
}

private fun formatExactTime(millis: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))
}

private fun formatFullTimestamp(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date(millis))
}

/**
 * Merges a UTC date-picker selection (`utcDateMillis`) with the existing local time-of-day
 * (hour, minute, second, millisecond) from `currentTimestampMillis` so the exact timestamp is preserved.
 */
internal fun mergeSelectedDatePreservingTime(
    utcDateMillis: Long,
    currentTimestampMillis: Long
): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcDateMillis
    }
    val localCal = Calendar.getInstance().apply {
        timeInMillis = currentTimestampMillis
        set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
    }
    return localCal.timeInMillis
}

/**
 * Updates the hour and minute of `currentTimestampMillis` while preserving its date and seconds.
 */
internal fun mergeSelectedTimePreservingDate(
    hourOfDay: Int,
    minute: Int,
    currentTimestampMillis: Long
): Long {
    val localCal = Calendar.getInstance().apply {
        timeInMillis = currentTimestampMillis
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
    }
    return localCal.timeInMillis
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InteractiveReceiptSheet(
    initialTitle: String,
    initialAmountText: String,
    initialType: String,
    initialCategory: String,
    initialPaymentMode: String,
    initialAccountName: String,
    initialTagsInput: String,
    initialNotes: String,
    initialIsShared: Boolean,
    initialDateMillis: Long,
    rawSmsRecord: SmsRawRecord? = null,
    hasHousehold: Boolean,
    availableCategories: List<String>,
    currencySymbol: String,
    showDeleteButton: Boolean,
    amountTestTag: String,
    titleTestTag: String,
    saveTestTag: String,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tagsInput: String,
        notes: String,
        isShared: Boolean,
        transactionDateMillis: Long
    ) -> Unit
) {
    val dimens = LocalAppDimens.current
    var title by remember { mutableStateOf(initialTitle) }
    var amountText by remember { mutableStateOf(initialAmountText) }
    var type by remember { mutableStateOf(initialType) }
    var category by remember { mutableStateOf(initialCategory) }
    var customCategoryInput by remember { mutableStateOf("") }
    var showCustomCategoryField by remember { mutableStateOf(false) }
    var paymentMode by remember { mutableStateOf(initialPaymentMode) }
    var accountName by remember { mutableStateOf(initialAccountName) }
    var tagsInput by remember { mutableStateOf(initialTagsInput) }
    var notes by remember { mutableStateOf(initialNotes) }
    var isShared by remember { mutableStateOf(initialIsShared && hasHousehold) }
    var txDateMillis by remember { mutableLongStateOf(initialDateMillis) }
    var showExtraDetails by remember { mutableStateOf(initialNotes.isNotBlank() || initialTagsInput.isNotBlank()) }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var showTimePickerModal by remember { mutableStateOf(false) }

    val catAccent = categoryAccentColor(category)
    val allCats = remember(availableCategories, category, customCategoryInput) {
        (availableCategories + listOf(category) + listOfNotNull(customCategoryInput.trim().takeIf { it.isNotBlank() }))
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = dimens.itemSpacing)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReceiptTicketCard(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.cardPadding, vertical = dimens.itemSpacing)
                        .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Action Row: Receipt Stamp + Category Emblem + Close/Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stampColor = if (isShared) HouseholdBadgeIndigo else PersonalBadgeTeal
                        Surface(
                            shape = RoundedCornerShape(dimens.smallCornerRadius * 0.6f),
                            color = stampColor.copy(alpha = 0.14f),
                            modifier = Modifier
                                .border(1.5.dp, stampColor, RoundedCornerShape(dimens.smallCornerRadius * 0.6f))
                                .clickable(enabled = hasHousehold) { isShared = !isShared }
                                .testTag("transaction_shared_switch")
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = dimens.horizontalPadding * 0.65f,
                                    vertical = dimens.itemSpacing * 0.45f
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isShared) Icons.Default.Groups else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = stampColor,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.68f)
                                )
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                                Text(
                                    text = if (isShared) "HOUSEHOLD" else "PERSONAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = stampColor
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(dimens.rowIconBoxSize)
                                .clip(CircleShape)
                                .background(catAccent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon(category),
                                contentDescription = category,
                                tint = catAccent,
                                modifier = Modifier.size(dimens.rowIconSize)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (showDeleteButton && onDelete != null) {
                                IconButton(
                                    onClick = onDelete,
                                    modifier = Modifier
                                        .size(dimens.rowIconBoxSize * 0.85f)
                                        .testTag("edit_dialog_delete_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Receipt",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(dimens.rowIconBoxSize * 0.85f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Expense vs Income Pill Toggle
                    Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.7f))
                    Row(horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.5f)) {
                        FilterChip(
                            selected = type == "EXPENSE",
                            onClick = { type = "EXPENSE" },
                            label = { Text("Expense") }
                        )
                        FilterChip(
                            selected = type == "INCOME",
                            onClick = {
                                type = "INCOME"
                                category = "Salary & Income"
                            },
                            label = { Text("Income") }
                        )
                    }

                    // Original SMS Section when editing an SMS-backed transaction
                    if (rawSmsRecord != null && rawSmsRecord.body.isNotBlank()) {
                        Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.7f))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_dialog_raw_sms_box"),
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ) {
                            Column(
                                modifier = Modifier.padding(dimens.cardPadding * 0.75f),
                                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Message,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(dimens.rowIconSize * 0.72f)
                                        )
                                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                                        Text(
                                            text = rawSmsRecord.sender.ifBlank { "SMS" },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = formatFullTimestamp(
                                            if (rawSmsRecord.timestamp > 0L) rawSmsRecord.timestamp else txDateMillis
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = rawSmsRecord.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    ReceiptDashedDividerLine()

                    // Big Receipt Total & Merchant Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Total ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .weight(0.45f)
                                .testTag(amountTestTag)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Merchant") },
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .weight(0.55f)
                                .testTag(titleTestTag)
                        )
                    }

                    ReceiptDashedDividerLine()

                    // Category Section on Receipt
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { showCustomCategoryField = !showCustomCategoryField }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(dimens.rowIconSize * 0.65f)
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.25f))
                            Text("Custom", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    AnimatedVisibility(visible = showCustomCategoryField) {
                        OutlinedTextField(
                            value = customCategoryInput,
                            onValueChange = {
                                customCategoryInput = it
                                if (it.isNotBlank()) category = it.trim()
                            },
                            label = { Text("Custom Category") },
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = dimens.itemSpacing * 0.6f)
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f),
                        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.5f)
                    ) {
                        allCats.forEach { cat ->
                            val isSelected = category.equals(cat, ignoreCase = true)
                            val chipAccent = categoryAccentColor(cat)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) chipAccent else chipAccent.copy(alpha = 0.14f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { category = cat }
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else chipAccent,
                                    modifier = Modifier.padding(
                                        horizontal = dimens.horizontalPadding * 0.75f,
                                        vertical = dimens.itemSpacing * 0.5f
                                    )
                                )
                            }
                        }
                    }

                    ReceiptDashedDividerLine()

                    // Payment Method Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("UPI", "CARD", "BANK", "CASH", "WALLET").forEach { mode ->
                            FilterChip(
                                selected = paymentMode.equals(mode, ignoreCase = true),
                                onClick = {
                                    paymentMode = mode
                                    if (mode == "CASH") accountName = "Cash Wallet"
                                },
                                label = { Text(mode, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimens.itemSpacing * 0.5f))

                    // Exact Date & Timestamp Selector Row (No Today/Yesterday/-1 Day buttons)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDatePickerModal = true },
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tx_date_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(dimens.rowIconSize * 0.75f)
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                            Text(
                                text = formatExactDate(txDateMillis),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { showTimePickerModal = true },
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tx_time_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(dimens.rowIconSize * 0.75f)
                            )
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                            Text(
                                text = formatExactTime(txDateMillis),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Progressive Disclosure: Expandable More Details
                    TextButton(
                        onClick = { showExtraDetails = !showExtraDetails },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showExtraDetails) "Less" else "More",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.25f))
                        Icon(
                            imageVector = if (showExtraDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                        )
                    }

                    AnimatedVisibility(visible = showExtraDetails) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.65f)
                        ) {
                            OutlinedTextField(
                                value = accountName,
                                onValueChange = { accountName = it },
                                label = { Text("Account / Card") },
                                singleLine = true,
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = tagsInput,
                                onValueChange = { tagsInput = it },
                                label = { Text("#Tags") },
                                singleLine = true,
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_tx_tags_input")
                            )
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Note") },
                                maxLines = 3,
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    ReceiptDashedDividerLine()

                    // Save Receipt Button
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                val finalCat = customCategoryInput.trim().ifBlank { category }
                                onSave(
                                    title.ifBlank { finalCat },
                                    amt,
                                    type,
                                    finalCat,
                                    accountName,
                                    paymentMode,
                                    tagsInput,
                                    notes,
                                    isShared,
                                    txDateMillis
                                )
                            }
                        },
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                        shape = RoundedCornerShape(dimens.smallCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") BudgetSafeGreen else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimens.navPillHeight)
                            .testTag(saveTestTag)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                        )
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showDatePickerModal) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = txDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedUtcMillis = datePickerState.selectedDateMillis
                        if (selectedUtcMillis != null) {
                            txDateMillis = mergeSelectedDatePreservingTime(
                                utcDateMillis = selectedUtcMillis,
                                currentTimestampMillis = txDateMillis
                            )
                        }
                        showDatePickerModal = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerModal = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePickerModal) {
        val cal = remember(txDateMillis) {
            Calendar.getInstance().apply { timeInMillis = txDateMillis }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePickerModal = false },
            confirmButton = {
                Button(
                    onClick = {
                        txDateMillis = mergeSelectedTimePreservingDate(
                            hourOfDay = timePickerState.hour,
                            minute = timePickerState.minute,
                            currentTimestampMillis = txDateMillis
                        )
                        showTimePickerModal = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerModal = false }) {
                    Text("Cancel")
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}

@Composable
fun AddTransactionDialog(
    hasHousehold: Boolean,
    defaultShared: Boolean,
    accounts: List<FinancialAccount>,
    availableCategories: List<String>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tagsInput: String,
        notes: String,
        isShared: Boolean,
        transactionDateMillis: Long
    ) -> Unit
) {
    InteractiveReceiptSheet(
        initialTitle = "",
        initialAmountText = "",
        initialType = "EXPENSE",
        initialCategory = "Groceries",
        initialPaymentMode = "UPI",
        initialAccountName = accounts.firstOrNull()?.name ?: "HDFC Bank ••4821",
        initialTagsInput = "",
        initialNotes = "",
        initialIsShared = hasHousehold && defaultShared,
        initialDateMillis = System.currentTimeMillis(),
        rawSmsRecord = null,
        hasHousehold = hasHousehold,
        availableCategories = availableCategories,
        currencySymbol = currencySymbol,
        showDeleteButton = false,
        amountTestTag = "add_tx_amount_input",
        titleTestTag = "add_tx_title_input",
        saveTestTag = "save_transaction_button",
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
fun EditTransactionDialog(
    transaction: ExpenseTransaction,
    hasHousehold: Boolean,
    availableCategories: List<String>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountName: String,
        paymentMode: String,
        tagsInput: String,
        notes: String,
        transactionDateMillis: Long,
        targetShared: Boolean
    ) -> Unit
) {
    val initialAmt = if (transaction.amount % 1.0 == 0.0) {
        transaction.amount.toLong().toString()
    } else {
        transaction.amount.toString()
    }
    val rawSmsRecord = remember(transaction.id, transaction.smsSignature, transaction.notes) {
        LocalAccountDataStore.getSmsMessageForTransaction(transaction)
    }

    InteractiveReceiptSheet(
        initialTitle = transaction.title,
        initialAmountText = initialAmt,
        initialType = transaction.type,
        initialCategory = transaction.category,
        initialPaymentMode = transaction.paymentMode,
        initialAccountName = transaction.accountName,
        initialTagsInput = transaction.tags.joinToString(", "),
        initialNotes = transaction.notes,
        initialIsShared = transaction.visibility == "SHARED" && hasHousehold,
        initialDateMillis = transaction.transactionDateMillis,
        rawSmsRecord = rawSmsRecord,
        hasHousehold = hasHousehold,
        availableCategories = availableCategories,
        currencySymbol = currencySymbol,
        showDeleteButton = true,
        amountTestTag = "edit_tx_amount_input",
        titleTestTag = "edit_tx_title_input",
        saveTestTag = "save_edited_transaction_button",
        onDelete = onDelete,
        onDismiss = onDismiss,
        onSave = { title, amount, type, category, accountName, paymentMode, tagsInput, notes, isShared, txDateMillis ->
            onSave(
                title,
                amount,
                type,
                category,
                accountName,
                paymentMode,
                tagsInput,
                notes,
                txDateMillis,
                isShared
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickCategoryPickerDialog(
    transaction: ExpenseTransaction,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenFullEditor: () -> Unit
) {
    val dimens = LocalAppDimens.current
    var selectedCat by remember(transaction.id) { mutableStateOf(transaction.category) }
    var customCategory by remember { mutableStateOf("") }

    val categories = remember(availableCategories, transaction.category) {
        (availableCategories + listOf(transaction.category))
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }

    Dialog(onDismissRequest = onDismiss) {
        ReceiptTicketCard(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.cardPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                ReceiptDashedDividerLine()

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.5f),
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.65f)
                ) {
                    categories.forEach { cat ->
                        val accent = categoryAccentColor(cat)
                        val isSel = selectedCat.equals(cat, ignoreCase = true) && customCategory.isBlank()
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSel) accent else accent.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    selectedCat = cat
                                    onSelectCategory(cat)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = dimens.horizontalPadding * 0.75f,
                                    vertical = dimens.itemSpacing * 0.65f
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon(cat),
                                    contentDescription = null,
                                    tint = if (isSel) Color.White else accent,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.72f)
                                )
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSel) Color.White else accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("New Category") },
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    TextButton(onClick = onOpenFullEditor) {
                        Text("Edit Full Receipt")
                    }
                    if (customCategory.isNotBlank()) {
                        Button(onClick = { onSelectCategory(customCategory.trim()) }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSettingsDialog(
    currentMode: AppThemeMode,
    onSelectMode: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val dimens = LocalAppDimens.current
    Dialog(onDismissRequest = onDismiss) {
        ReceiptTicketCard(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                ReceiptDashedDividerLine()

                ThemeModeSelectorBar(
                    currentMode = currentMode,
                    onSelectMode = { mode ->
                        onSelectMode(mode)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryBudgetDialog(
    initialProgress: CategorySpendProgress?,
    hasHousehold: Boolean,
    defaultShared: Boolean,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double, alertPct: Int, isShared: Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    var category by remember { mutableStateOf(initialProgress?.category ?: "Groceries") }
    var customCategory by remember { mutableStateOf("") }
    var limitText by remember {
        mutableStateOf(
            if ((initialProgress?.limit ?: 0.0) > 0) initialProgress!!.limit.toInt().toString() else ""
        )
    }
    var isShared by remember { mutableStateOf(hasHousehold && defaultShared) }

    Dialog(onDismissRequest = onDismiss) {
        ReceiptTicketCard(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.cardPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.85f)
            ) {
                Text(
                    text = "Category Limit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ReceiptDashedDividerLine()

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f),
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.5f)
                ) {
                    ExpenseCategories.EXPENSE_ONLY.forEach { cat ->
                        FilterChip(
                            selected = category.equals(cat, ignoreCase = true) && customCategory.isBlank(),
                            onClick = {
                                category = cat
                                customCategory = ""
                            },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("Custom Category") },
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Limit ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasHousehold) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Household",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(checked = isShared, onCheckedChange = { isShared = it })
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                    Button(
                        onClick = {
                            val limit = limitText.toDoubleOrNull() ?: 0.0
                            val targetCat = customCategory.trim().ifBlank { category }
                            if (limit > 0) {
                                onSave(targetCat, limit, 80, isShared)
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun EditOverallBudgetDialog(
    personalBudget: Double,
    householdBudget: Double?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (personalLimit: Double, householdLimit: Double?, currency: String) -> Unit
) {
    val dimens = LocalAppDimens.current
    var personalText by remember {
        mutableStateOf(if (personalBudget > 0.0) personalBudget.toInt().toString() else "")
    }
    var householdText by remember {
        mutableStateOf(if ((householdBudget ?: 0.0) > 0.0) householdBudget!!.toInt().toString() else "")
    }
    var currency by remember { mutableStateOf(currencySymbol) }

    Dialog(onDismissRequest = onDismiss) {
        ReceiptTicketCard(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.85f)
            ) {
                Text(
                    text = "Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ReceiptDashedDividerLine()

                if (householdBudget != null) {
                    OutlinedTextField(
                        value = householdText,
                        onValueChange = { householdText = it },
                        label = { Text("Household") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(dimens.smallCornerRadius),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = personalText,
                    onValueChange = { personalText = it },
                    label = { Text("Personal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it.take(4) },
                    label = { Text("Currency") },
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                    Button(
                        onClick = {
                            val pLimit = personalText.toDoubleOrNull() ?: 0.0
                            val hLimit = if (householdBudget != null) (householdText.toDoubleOrNull() ?: 0.0) else null
                            onSave(pLimit, hLimit, currency.ifBlank { "₹" })
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccountDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        bankName: String,
        accountType: String,
        lastFour: String,
        balance: Double,
        creditLimit: Double
    ) -> Unit
) {
    val dimens = LocalAppDimens.current
    var bankName by remember { mutableStateOf("HDFC Bank") }
    var accountType by remember { mutableStateOf("BANK") }
    var lastFour by remember { mutableStateOf("4821") }
    var balanceText by remember { mutableStateOf("45000") }
    var creditLimitText by remember { mutableStateOf("150000") }

    Dialog(onDismissRequest = onDismiss) {
        ReceiptTicketCard(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.cardPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.85f)
            ) {
                Text(
                    text = "Add Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ReceiptDashedDividerLine()

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f)
                ) {
                    listOf("BANK", "CREDIT_CARD", "UPI", "CASH").forEach { type ->
                        FilterChip(
                            selected = accountType == type,
                            onClick = { accountType = type },
                            label = { Text(type) }
                        )
                    }
                }
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { lastFour = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Last 4 Digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Balance ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                    Button(
                        onClick = {
                            val suffix = if (lastFour.isNotBlank()) " ••$lastFour" else ""
                            val displayName = if (accountType == "CREDIT_CARD") "$bankName Card$suffix" else "$bankName$suffix"
                            onSave(
                                displayName,
                                bankName,
                                accountType,
                                lastFour,
                                balanceText.toDoubleOrNull() ?: 0.0,
                                if (accountType == "CREDIT_CARD") creditLimitText.toDoubleOrNull() ?: 0.0 else 0.0
                            )
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
