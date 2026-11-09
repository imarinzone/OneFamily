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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.ui.viewmodel.SmsSyncProgressState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Sanitizes numeric decimal text input so only digits and at most one decimal point (up to 2 decimal places) are accepted.
 */
internal fun sanitizeDecimalInput(raw: String, maxIntegerDigits: Int = 9): String {
    val filtered = raw.filter { it.isDigit() || it == '.' }
    val parts = filtered.split('.')
    val intPart = parts.firstOrNull().orEmpty().take(maxIntegerDigits)
    return if (parts.size > 1) {
        val decPart = parts.drop(1).joinToString("").take(2)
        "$intPart.$decPart"
    } else {
        intPart
    }
}

internal fun validatePositiveAmount(raw: String, maxAllowed: Double = 100_000_000.0): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "Please enter an amount."
    val value = trimmed.toDoubleOrNull() ?: return "Enter a valid numeric amount."
    if (value <= 0.0) return "Amount must be greater than 0."
    if (value > maxAllowed) return "Amount cannot exceed ${maxAllowed.toLong()}."
    return null
}

internal fun validateNonNegativeAmount(raw: String, maxAllowed: Double = 100_000_000.0): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null // Empty means unassigned (0.0)
    val value = trimmed.toDoubleOrNull() ?: return "Enter a valid numeric amount."
    if (value < 0.0) return "Amount cannot be negative."
    if (value > maxAllowed) return "Amount cannot exceed ${maxAllowed.toLong()}."
    return null
}

internal fun validateCategoryName(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "Category name cannot be empty."
    if (trimmed.length < 2) return "Category name must be at least 2 characters."
    if (trimmed.length > 40) return "Category name must be 40 characters or fewer."
    if (!trimmed.any { it.isLetter() }) return "Category name must contain letters."
    return null
}

internal fun validateCurrencySymbol(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "Enter a currency symbol (e.g. ₹ or $)."
    if (trimmed.length > 4) return "Currency symbol must be 1–4 characters."
    if (trimmed.any { it.isDigit() }) return "Currency symbol cannot contain numbers."
    return null
}

internal fun validateDisplayName(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "Name cannot be empty."
    if (trimmed.length < 2) return "Name must be at least 2 characters."
    if (trimmed.length > 60) return "Name must be 60 characters or fewer."
    return null
}

internal fun validateEmailAddress(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "Email address cannot be empty."
    val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")
    if (!emailRegex.matches(trimmed)) return "Enter a valid email address."
    return null
}

internal fun validateHouseholdJoinCode(raw: String): String? {
    val trimmed = raw.trim().uppercase()
    if (trimmed.length != 6 || !trimmed.all { it.isLetterOrDigit() }) {
        return "Enter a valid 6-character household code."
    }
    return null
}

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

                    var attemptedSubmit by remember { mutableStateOf(false) }
                    val amountError = if (attemptedSubmit || amountText.isNotEmpty()) {
                        validatePositiveAmount(amountText)
                    } else {
                        null
                    }
                    val customCatError = if (showCustomCategoryField && (attemptedSubmit || customCategoryInput.isNotEmpty())) {
                        validateCategoryName(customCategoryInput)
                    } else {
                        null
                    }

                    // Big Receipt Total & Merchant Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.6f),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = sanitizeDecimalInput(it) },
                            label = { Text("Total ($currencySymbol)") },
                            isError = amountError != null,
                            supportingText = amountError?.let { err -> { Text(err) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .weight(0.48f)
                                .testTag(amountTestTag)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it.take(100) },
                            label = { Text("Merchant") },
                            supportingText = if (title.length >= 95) {
                                { Text("${title.length}/100") }
                            } else {
                                null
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .weight(0.52f)
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
                                customCategoryInput = it.take(40)
                                if (it.isNotBlank()) category = it.trim()
                            },
                            label = { Text("Custom Category") },
                            isError = customCatError != null,
                            supportingText = customCatError?.let { err -> { Text(err) } },
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
                                onValueChange = { accountName = it.take(80) },
                                label = { Text("Account / Card") },
                                singleLine = true,
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = tagsInput,
                                onValueChange = { tagsInput = it.take(120) },
                                label = { Text("#Tags") },
                                singleLine = true,
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_tx_tags_input")
                            )
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it.take(500) },
                                label = { Text("Note (${notes.length}/500)") },
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
                            attemptedSubmit = true
                            val amtErr = validatePositiveAmount(amountText)
                            val catErr = if (showCustomCategoryField && customCategoryInput.isNotBlank()) {
                                validateCategoryName(customCategoryInput)
                            } else {
                                null
                            }
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amtErr == null && catErr == null && amt > 0.0) {
                                val finalCat = if (showCustomCategoryField && customCategoryInput.isNotBlank()) {
                                    customCategoryInput.trim()
                                } else {
                                    category
                                }
                                onSave(
                                    title.trim().ifBlank { finalCat },
                                    amt,
                                    type,
                                    finalCat,
                                    accountName.trim().ifBlank { "Cash" },
                                    paymentMode,
                                    tagsInput.trim(),
                                    notes.trim(),
                                    isShared,
                                    txDateMillis
                                )
                            }
                        },
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

                val customCatErr = if (customCategory.isNotEmpty()) validateCategoryName(customCategory) else null
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it.take(40) },
                    label = { Text("New Category") },
                    isError = customCatErr != null,
                    supportingText = customCatErr?.let { err -> { Text(err) } },
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
                        Button(
                            onClick = {
                                if (validateCategoryName(customCategory) == null) {
                                    onSelectCategory(customCategory.trim())
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
    var category by remember(initialProgress?.category) {
        mutableStateOf(initialProgress?.category ?: "Groceries")
    }
    var customCategory by remember { mutableStateOf("") }
    var limitText by remember(initialProgress?.limit) {
        mutableStateOf(
            if ((initialProgress?.limit ?: 0.0) > 0.0) {
                if (initialProgress!!.limit % 1.0 == 0.0) {
                    initialProgress.limit.toLong().toString()
                } else {
                    initialProgress.limit.toString()
                }
            } else {
                ""
            }
        )
    }
    var alertPctText by remember(initialProgress?.alertThresholdPercent) {
        mutableStateOf((initialProgress?.alertThresholdPercent ?: 80).toString())
    }
    var isShared by remember { mutableStateOf(hasHousehold && defaultShared) }
    var attemptedSave by remember { mutableStateOf(false) }

    val customCatError = if (customCategory.isNotBlank()) {
        validateCategoryName(customCategory)
    } else {
        null
    }
    val limitError = if (attemptedSave || limitText.isNotEmpty()) {
        validatePositiveAmount(limitText)
    } else {
        null
    }
    val alertPctInt = alertPctText.toIntOrNull()
    val alertPctError = if (attemptedSave || alertPctText.isNotEmpty()) {
        if (alertPctInt == null || alertPctInt !in 10..100) {
            "Enter an alert threshold between 10% and 100%."
        } else {
            null
        }
    } else {
        null
    }

    val categoryOptions = remember(initialProgress?.category) {
        (ExpenseCategories.EXPENSE_ONLY + listOfNotNull(initialProgress?.category))
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
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.85f)
            ) {
                Text(
                    text = if ((initialProgress?.limit ?: 0.0) > 0.0) {
                        "Edit Category Budget"
                    } else {
                        "Set Category Budget"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ReceiptDashedDividerLine()

                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f),
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.5f)
                ) {
                    categoryOptions.forEach { cat ->
                        FilterChip(
                            selected = category.equals(cat, ignoreCase = true) && customCategory.isBlank(),
                            onClick = {
                                category = cat
                                customCategory = ""
                            },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("budget_cat_chip_${cat.lowercase().replace(" ", "_")}")
                        )
                    }
                }

                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it.take(40) },
                    label = { Text("Or Custom Category Name") },
                    isError = customCatError != null,
                    supportingText = customCatError?.let { err -> { Text(err) } },
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_budget_custom_name_input")
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = sanitizeDecimalInput(it) },
                    label = { Text("Monthly Limit ($currencySymbol)") },
                    isError = limitError != null,
                    supportingText = limitError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_budget_limit_input")
                )

                OutlinedTextField(
                    value = alertPctText,
                    onValueChange = { alertPctText = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = { Text("Alert Threshold (%)") },
                    isError = alertPctError != null,
                    supportingText = alertPctError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_budget_alert_pct_input")
                )

                if (hasHousehold) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Apply to Family Household",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(checked = isShared, onCheckedChange = { isShared = it })
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if ((initialProgress?.limit ?: 0.0) > 0.0) {
                        TextButton(
                            onClick = {
                                val targetCat = customCategory.trim().ifBlank { category }
                                onSave(targetCat, 0.0, 80, isShared)
                            },
                            modifier = Modifier.testTag("clear_category_budget_button")
                        ) {
                            Text("Clear Limit", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                        Button(
                            onClick = {
                                attemptedSave = true
                                val lErr = validatePositiveAmount(limitText)
                                val cErr = if (customCategory.isNotBlank()) validateCategoryName(customCategory) else null
                                val pct = alertPctText.toIntOrNull()
                                val pErr = if (pct == null || pct !in 10..100) "Invalid" else null
                                if (lErr == null && cErr == null && pErr == null) {
                                    val limit = limitText.toDoubleOrNull() ?: 0.0
                                    val targetCat = customCategory.trim().ifBlank { category }
                                    onSave(targetCat, limit, pct ?: 80, isShared)
                                }
                            },
                            modifier = Modifier.testTag("save_category_budget_button")
                        ) {
                            Text("Save")
                        }
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
        mutableStateOf(if (personalBudget > 0.0) personalBudget.toLong().toString() else "")
    }
    var householdText by remember {
        mutableStateOf(if ((householdBudget ?: 0.0) > 0.0) householdBudget!!.toLong().toString() else "")
    }
    var currency by remember { mutableStateOf(currencySymbol.ifBlank { "₹" }) }
    var attemptedSave by remember { mutableStateOf(false) }

    val householdError = if (householdBudget != null && (attemptedSave || householdText.isNotEmpty())) {
        validateNonNegativeAmount(householdText)
    } else {
        null
    }
    val personalError = if (attemptedSave || personalText.isNotEmpty()) {
        validateNonNegativeAmount(personalText)
    } else {
        null
    }
    val currencyError = if (attemptedSave || currency.isNotEmpty()) {
        validateCurrencySymbol(currency)
    } else {
        null
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
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.85f)
            ) {
                Text(
                    text = "Monthly Budgets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ReceiptDashedDividerLine()

                if (householdBudget != null) {
                    OutlinedTextField(
                        value = householdText,
                        onValueChange = { householdText = sanitizeDecimalInput(it) },
                        label = { Text("Household Monthly Limit ($currency)") },
                        placeholder = { Text("Leave blank for unassigned") },
                        isError = householdError != null,
                        supportingText = householdError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(dimens.smallCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overall_household_budget_input")
                    )
                }
                OutlinedTextField(
                    value = personalText,
                    onValueChange = { personalText = sanitizeDecimalInput(it) },
                    label = { Text("Personal Monthly Limit ($currency)") },
                    placeholder = { Text("Leave blank for unassigned") },
                    isError = personalError != null,
                    supportingText = personalError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overall_personal_budget_input")
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it.filterNot { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Currency Symbol") },
                    isError = currencyError != null,
                    supportingText = currencyError?.let { err -> { Text(err) } },
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overall_currency_input")
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
                            attemptedSave = true
                            val hErr = if (householdBudget != null) validateNonNegativeAmount(householdText) else null
                            val pErr = validateNonNegativeAmount(personalText)
                            val cErr = validateCurrencySymbol(currency)
                            if (hErr == null && pErr == null && cErr == null) {
                                val pLimit = personalText.trim().toDoubleOrNull() ?: 0.0
                                val hLimit = if (householdBudget != null) {
                                    householdText.trim().toDoubleOrNull() ?: 0.0
                                } else {
                                    null
                                }
                                onSave(pLimit, hLimit, currency.trim().ifBlank { "₹" })
                            }
                        },
                        modifier = Modifier.testTag("save_overall_budget_button")
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
    var bankName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("BANK") }
    var lastFour by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var creditLimitText by remember { mutableStateOf("") }
    var attemptedSave by remember { mutableStateOf(false) }

    val bankNameError = if (attemptedSave || bankName.isNotEmpty()) {
        if (bankName.trim().length < 2) "Enter a valid bank or account name (min 2 chars)." else null
    } else {
        null
    }
    val requiresDigits = accountType == "BANK" || accountType == "CREDIT_CARD"
    val lastFourError = if (attemptedSave || lastFour.isNotEmpty()) {
        if (requiresDigits && lastFour.length != 4) {
            "Enter the last 4 digits of your account/card."
        } else if (!requiresDigits && lastFour.isNotEmpty() && lastFour.length != 4) {
            "Last 4 digits must be 4 numbers or left blank."
        } else {
            null
        }
    } else {
        null
    }
    val balanceError = if (attemptedSave || balanceText.isNotEmpty()) {
        validateNonNegativeAmount(balanceText, maxAllowed = 1_000_000_000.0)
    } else {
        null
    }
    val creditLimitError = if (accountType == "CREDIT_CARD" && (attemptedSave || creditLimitText.isNotEmpty())) {
        validatePositiveAmount(creditLimitText, maxAllowed = 100_000_000.0)
    } else {
        null
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
                    onValueChange = { bankName = it.take(60) },
                    label = { Text("Bank / Account Name") },
                    placeholder = { Text("e.g. HDFC Bank") },
                    isError = bankNameError != null,
                    supportingText = bankNameError?.let { err -> { Text(err) } },
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_account_name_input")
                )
                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { lastFour = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text(if (requiresDigits) "Last 4 Digits" else "Last 4 Digits (Optional)") },
                    isError = lastFourError != null,
                    supportingText = lastFourError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_account_last_four_input")
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = sanitizeDecimalInput(it, maxIntegerDigits = 10) },
                    label = { Text("Current Balance ($currencySymbol)") },
                    isError = balanceError != null,
                    supportingText = balanceError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(dimens.smallCornerRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_account_balance_input")
                )
                if (accountType == "CREDIT_CARD") {
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = sanitizeDecimalInput(it, maxIntegerDigits = 9) },
                        label = { Text("Credit Limit ($currencySymbol)") },
                        isError = creditLimitError != null,
                        supportingText = creditLimitError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(dimens.smallCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_account_credit_limit_input")
                    )
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
                            attemptedSave = true
                            val nameErr = if (bankName.trim().length < 2) "Invalid" else null
                            val digitsErr = if (requiresDigits && lastFour.length != 4) {
                                "Invalid"
                            } else if (!requiresDigits && lastFour.isNotEmpty() && lastFour.length != 4) {
                                "Invalid"
                            } else {
                                null
                            }
                            val balErr = validateNonNegativeAmount(balanceText, maxAllowed = 1_000_000_000.0)
                            val limErr = if (accountType == "CREDIT_CARD") {
                                validatePositiveAmount(creditLimitText, maxAllowed = 100_000_000.0)
                            } else {
                                null
                            }

                            if (nameErr == null && digitsErr == null && balErr == null && limErr == null) {
                                val cleanBank = bankName.trim()
                                val suffix = if (lastFour.isNotBlank()) " ••$lastFour" else ""
                                val displayName = if (accountType == "CREDIT_CARD") "$cleanBank Card$suffix" else "$cleanBank$suffix"
                                onSave(
                                    displayName,
                                    cleanBank,
                                    accountType,
                                    lastFour,
                                    balanceText.trim().toDoubleOrNull() ?: 0.0,
                                    if (accountType == "CREDIT_CARD") creditLimitText.trim().toDoubleOrNull() ?: 0.0 else 0.0
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_account_button")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * First-Time SMS Sync Setup & Live Progress Screen (`FirstTimeSmsSyncScreen`):
 * - Prompts the user to sync their Bank & UPI SMS messages when setting up for the first time.
 * - Stays on this page until ALL SMS messages have been read and synced in paced background batches.
 * - Displays live progress bar, percentage, SMS Read count, SMS Remaining count, Total SMS count,
 *   and Transactions Synced count so the phone never hangs or crashes.
 */
@Composable
fun FirstTimeSmsSyncScreen(
    syncState: SmsSyncProgressState,
    onStartSyncClick: () -> Unit,
    onContinueToAppClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val dimens = LocalAppDimens.current
    val hasStartedOrFinished = syncState.isSyncing || syncState.isCompleted

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("first_time_sms_sync_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimens.horizontalPadding * 1.25f,
                    vertical = dimens.itemSpacing * 2f
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 1.2f)
        ) {
            Box(
                modifier = Modifier
                    .size(dimens.rowIconBoxSize * 1.75f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (syncState.isCompleted) Icons.Default.Check else Icons.Default.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimens.rowIconSize * 1.6f)
                )
            }

            Text(
                text = when {
                    syncState.isCompleted -> "SMS Sync Complete"
                    syncState.isSyncing -> "Syncing Your SMS Inbox..."
                    else -> "Sync Bank & UPI SMS"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = when {
                    syncState.isCompleted -> syncState.statusMessage.ifBlank {
                        "All your bank and UPI SMS messages have been safely processed."
                    }
                    syncState.isSyncing -> "Please stay on this screen while we safely process your SMS messages in paced batches so your phone stays smooth and responsive."
                    else -> "Automatically detect your bank, card, and UPI transactions from your SMS inbox. We process messages in paced batches to keep your phone fast."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (hasStartedOrFinished) {
                ReceiptTicketCard(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (syncState.isSyncing) "Reading SMS Messages" else "Completed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val pct = (syncState.progressFraction * 100f).roundToInt().coerceIn(0, 100)
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("sms_sync_percentage_text")
                            )
                        }

                        LinearProgressIndicator(
                            progress = { syncState.progressFraction },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimens.itemSpacing * 0.9f)
                                .clip(RoundedCornerShape(50))
                                .testTag("sms_sync_progress_bar")
                        )

                        ReceiptDashedDividerLine()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "SMS Read",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${syncState.readSmsCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("sms_sync_read_count")
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${syncState.remainingSmsCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("sms_sync_remaining_count")
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total SMS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${syncState.totalSmsCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("sms_sync_total_count")
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = dimens.horizontalPadding * 0.8f,
                                        vertical = dimens.itemSpacing * 0.7f
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Transactions Found / Synced",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${syncState.savedTransactionsCount} / ${syncState.matchedTransactionsCount}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("sms_sync_saved_tx_count")
                                )
                            }
                        }

                        if (syncState.statusMessage.isNotBlank()) {
                            Text(
                                text = syncState.statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (!hasStartedOrFinished) {
                Button(
                    onClick = onStartSyncClick,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.navPillHeight)
                        .testTag("start_first_time_sms_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                    )
                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.45f))
                    Text(
                        text = "Start SMS Sync Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onSkipClick,
                    modifier = Modifier.testTag("skip_first_time_sms_sync_button")
                ) {
                    Text("Skip for Now")
                }
            } else if (syncState.isCompleted && !syncState.isSyncing) {
                Button(
                    onClick = onContinueToAppClick,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.navPillHeight)
                        .testTag("finish_sms_sync_continue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                    )
                    Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.45f))
                    Text(
                        text = "Continue to Home",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

