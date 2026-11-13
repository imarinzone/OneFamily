package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.export.CsvExportScope
import com.example.data.export.CsvTransactionExporter
import com.example.data.local.LocalAccountDataStore
import com.example.data.local.SmsRawRecord
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.ui.components.CustomDateRangePickerModal
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.PersonalBadgeTeal
import com.example.ui.theme.categoryAccentColor
import com.example.ui.viewmodel.CategorySpendProgress
import com.example.ui.viewmodel.DateRangeFilterUtils
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.OneFamilyDashboardUiState
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

private fun formatExactDateTime(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))
}

private fun formatFullTimestamp(millis: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date(millis))
}

@Composable
private fun AnimatedModalSheetContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        entered = true
    }
    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "modal_sheet_slide_up"
    )
    Box(
        modifier = modifier.graphicsLayer {
            translationY = (1f - progress) * 42.dp.toPx()
            alpha = 0.25f + 0.75f * progress
        }
    ) {
        content()
    }
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
    linkedSmsRecords: List<SmsRawRecord> = emptyList(),
    hasHousehold: Boolean,
    availableCategories: List<String>,
    currencySymbol: String,
    showDeleteButton: Boolean,
    amountTestTag: String,
    titleTestTag: String,
    saveTestTag: String,
    onDelete: (() -> Unit)? = null,
    onLinkDuplicate: (() -> Unit)? = null,
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
        AnimatedModalSheetContainer(
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            if (onLinkDuplicate != null) {
                                IconButton(
                                    onClick = onLinkDuplicate,
                                    modifier = Modifier
                                        .size(dimens.rowIconBoxSize * 0.85f)
                                        .testTag("edit_dialog_link_duplicate_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = "Link Duplicate SMS / Transaction",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
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

                    // Original & Linked Duplicate SMS Section when editing an SMS-backed transaction
                    val allSmsRecords = remember(rawSmsRecord, linkedSmsRecords) {
                        val combined = buildList {
                            if (rawSmsRecord != null && rawSmsRecord.body.isNotBlank()) add(rawSmsRecord)
                            linkedSmsRecords.filter { it.body.isNotBlank() }.forEach { rec ->
                                if (none { it.signature == rec.signature || it.body == rec.body }) {
                                    add(rec)
                                }
                            }
                        }
                        combined
                    }
                    if (allSmsRecords.isNotEmpty()) {
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
                                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.5f)
                            ) {
                                if (allSmsRecords.size > 1) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(dimens.rowIconSize * 0.68f)
                                        )
                                        Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.3f))
                                        Text(
                                            text = "Linked SMS Alerts (${allSmsRecords.size})",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                allSmsRecords.forEachIndexed { idx, rec ->
                                    if (idx > 0) {
                                        ReceiptDashedDividerLine()
                                    }
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
                                                text = rec.sender.ifBlank { "SMS" },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = formatFullTimestamp(
                                                if (rec.timestamp > 0L) rec.timestamp else txDateMillis
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = rec.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
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
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = dimens.horizontalPadding * 0.7f,
                                        vertical = dimens.itemSpacing * 0.45f
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = categoryIcon(cat),
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else chipAccent,
                                        modifier = Modifier.size(dimens.rowIconSize * 0.65f)
                                    )
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else chipAccent
                                    )
                                }
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
    onLinkDuplicate: (() -> Unit)? = null,
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
    val linkedSmsRecords = remember(transaction.id, transaction.smsSignature, transaction.linkedSmsSignatures, transaction.notes) {
        LocalAccountDataStore.getLinkedSmsMessagesForTransaction(transaction)
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
        linkedSmsRecords = linkedSmsRecords,
        hasHousehold = hasHousehold,
        availableCategories = availableCategories,
        currencySymbol = currencySymbol,
        showDeleteButton = true,
        amountTestTag = "edit_tx_amount_input",
        titleTestTag = "edit_tx_title_input",
        saveTestTag = "save_edited_transaction_button",
        onDelete = onDelete,
        onLinkDuplicate = onLinkDuplicate,
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
        AnimatedModalSheetContainer {
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
        AnimatedModalSheetContainer {
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
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(cat),
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.68f)
                                )
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
        AnimatedModalSheetContainer {
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
        AnimatedModalSheetContainer {
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
}

/**
 * First-Time SMS Sync Setup & Live Progress Screen (`FirstTimeSmsSyncScreen`):
 * - Prompts the user to sync their Bank & UPI SMS messages when setting up for the first time.
 * - Uses parallel reader threads (>= 5) + parallel writer threads (>= 5) in batches.
 * - Places compact sync controls/telemetry at the top and expands the downward-scanning
 *   "Transaction Being Read" animation (`sms_transaction_reading_animation`) at the bottom
 *   to fill the rest of the screen.
 */
@Composable
fun FirstTimeSmsSyncScreen(
    syncState: SmsSyncProgressState,
    onStartSyncClick: () -> Unit,
    onContinueToAppClick: () -> Unit,
    onSkipClick: () -> Unit,
    currencySymbol: String = "₹"
) {
    val dimens = LocalAppDimens.current
    val density = LocalDensity.current
    val hasStartedOrFinished = syncState.isSyncing || syncState.isCompleted
    var topControlsHeightPx by remember { mutableIntStateOf(0) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("first_time_sms_sync_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val viewportHeight = maxHeight
            val topControlsDp = with(density) { topControlsHeightPx.toDp() }
            val outerVerticalPad = dimens.itemSpacing * 1.1f
            val sectionGap = dimens.itemSpacing * 0.95f
            val minScannerHeight = (viewportHeight * 0.46f).coerceIn(200.dp, 360.dp)
            val scannerCardHeight = if (topControlsHeightPx > 0) {
                (viewportHeight - topControlsDp - (outerVerticalPad * 2) - sectionGap)
                    .coerceAtLeast(minScannerHeight)
            } else {
                (viewportHeight * 0.62f).coerceAtLeast(minScannerHeight)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = dimens.horizontalPadding * 1.15f,
                        vertical = outerVerticalPad
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(sectionGap)
            ) {
                // 1. Top Controls & Telemetry Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { topControlsHeightPx = it.height },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.75f)
                ) {
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

                    // Compact status & batch progress pill (zero thread counts exposed in UI)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = dimens.horizontalPadding * 0.85f,
                                vertical = dimens.itemSpacing * 0.4f
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.55f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "SMS Receipt Scanner",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                            )
                            Text(
                                text = if (hasStartedOrFinished) {
                                    "📦 Batch ${syncState.processedBatchesCount}/${syncState.totalBatchesCount.coerceAtLeast(1)}  •  Bank & UPI Scanner"
                                } else {
                                    "Bank & UPI Receipt Scanner"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.testTag("sms_parallel_threads_badge")
                            )
                        }
                    }

                    if (hasStartedOrFinished) {
                        ReceiptTicketCard(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = dimens.cardPadding,
                                        vertical = dimens.itemSpacing * 0.75f
                                    ),
                                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.65f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.5f)
                                    ) {
                                        Icon(
                                            imageVector = if (syncState.isSyncing) Icons.Default.Message else Icons.Default.Check,
                                            contentDescription = if (syncState.isSyncing) "Reading SMS Messages" else "Completed",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                                        )
                                        Text(
                                            text = if (syncState.isSyncing) "Reading SMS Messages" else "Completed",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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
                                        .height(dimens.itemSpacing * 0.8f)
                                        .clip(RoundedCornerShape(50))
                                        .testTag("sms_sync_progress_bar")
                                )

                                ReceiptDashedDividerLine()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "📨 Read",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${syncState.readSmsCount}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.testTag("sms_sync_read_count")
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "⏳",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${syncState.remainingSmsCount}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.testTag("sms_sync_remaining_count")
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "📥 Σ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${syncState.totalSmsCount}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.testTag("sms_sync_total_count")
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(dimens.smallCornerRadius),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = dimens.horizontalPadding * 0.55f,
                                                vertical = dimens.itemSpacing * 0.35f
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "💳 ✓",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "${syncState.savedTransactionsCount}/${syncState.matchedTransactionsCount}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.testTag("sms_sync_saved_tx_count")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!hasStartedOrFinished) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.5f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onStartSyncClick,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dimens.navPillHeight)
                                    .testTag("start_first_time_sms_sync_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Start SMS Sync Now",
                                    modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                                )
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                                Text(
                                    text = "Start SMS Sync Now",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            TextButton(
                                onClick = onSkipClick,
                                modifier = Modifier.testTag("skip_first_time_sms_sync_button")
                            ) {
                                Text("Skip for Now")
                            }
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
                                contentDescription = "Continue to Home",
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

                // 2. Bottom Expanded Downward-Scanning Animation Card — shown ONLY while actively scanning
                if (syncState.isSyncing) {
                    TransactionReadingAnimationCard(
                        syncState = syncState,
                        currencySymbol = currencySymbol,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(scannerCardHeight)
                    )
                }
            }
        }
    }
}

/**
 * Expanded "Transaction Being Read" Downward Laser & Receipt Scanner Animation Card:
 * - Positioned in the lower portion of `FirstTimeSmsSyncScreen` and sized to fill all remaining screen height.
 * - Sweeps a glowing laser scan beam downward (`top -> bottom`) across a large multi-line SMS receipt viewport
 *   with viewfinder corner brackets and concentric radar pulse rings.
 * - Uses continuous downward infinite transition on device runtime and deterministic spring phase under
 *   Robolectric so `composeTestRule.waitForIdle()` never hangs.
 */
@Composable
private fun TransactionReadingAnimationCard(
    syncState: SmsSyncProgressState,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val isRobolectric = remember { Build.FINGERPRINT.equals("robolectric", ignoreCase = true) }

    val deterministicTargetPhase = if (syncState.isCompleted) {
        0.94f
    } else if (syncState.isSyncing) {
        val raw = ((syncState.readSmsCount % 15) / 15f) + (syncState.progressFraction * 0.35f)
        (raw % 1f).coerceIn(0.12f, 0.92f)
    } else {
        0.35f
    }
    val deterministicPhase by animateFloatAsState(
        targetValue = deterministicTargetPhase,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sms_scan_beam_deterministic_phase"
    )

    val animatedScanPhase = if (!isRobolectric && !syncState.isCompleted) {
        val infiniteTransition = rememberInfiniteTransition(label = "sms_downward_scanner_transition")
        val sweepingDownPhase by infiniteTransition.animateFloat(
            initialValue = 0.06f,
            targetValue = 0.94f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = if (syncState.isSyncing) 1450 else 2100,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "sms_downward_laser_phase"
        )
        sweepingDownPhase
    } else {
        deterministicPhase
    }

    val activeTitle = syncState.currentReadingTransactionTitle.ifBlank {
        if (syncState.isCompleted) "All SMS Transactions Indexed"
        else if (syncState.isSyncing) "Scanning Bank & UPI SMS..."
        else "Ready to Scan SMS Inbox"
    }
    val activeBank = syncState.currentReadingBank.ifBlank { "BANK / UPI" }
    val activeCategory = if (syncState.currentReadingCategory.isBlank()) {
        "Unknown"
    } else {
        ExpenseCategories.normalizeCategory(syncState.currentReadingCategory)
    }
    val activeAmount = syncState.currentReadingAmount

    Surface(
        shape = RoundedCornerShape(dimens.cardCornerRadius * 1.15f),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = primaryColor.copy(alpha = 0.32f),
                shape = RoundedCornerShape(dimens.cardCornerRadius * 1.15f)
            )
            .testTag("sms_transaction_reading_animation")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimens.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.8f)
        ) {
            // Live Transaction Being Read Ticker Pill (at top of scanner card for immediate visibility)
            Surface(
                shape = RoundedCornerShape(dimens.smallCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sms_live_transaction_ticker")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimens.horizontalPadding * 0.75f,
                            vertical = dimens.itemSpacing * 0.65f
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.65f),
                        modifier = Modifier.weight(1f)
                    ) {
                        val catAccent = categoryAccentColor(activeCategory)
                        Box(
                            modifier = Modifier
                                .size(dimens.rowIconBoxSize * 0.85f)
                                .clip(CircleShape)
                                .background(catAccent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon(activeCategory),
                                contentDescription = activeCategory,
                                tint = catAccent,
                                modifier = Modifier.size(dimens.rowIconSize * 0.8f)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeTitle,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "🏦 $activeBank  •  $activeCategory",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (activeAmount > 0.0) {
                        Text(
                            text = formatCurrency(activeAmount, currencySymbol),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = if (syncState.isCompleted) Icons.Default.Check else Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                        )
                    }
                }
            }

            // Recent Read Transactions Stream Pills
            if (syncState.recentReadTransactions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.55f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    syncState.recentReadTransactions.take(4).forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = dimens.horizontalPadding * 0.6f,
                                    vertical = dimens.itemSpacing * 0.35f
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category),
                                    contentDescription = item.category,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.65f)
                                )
                                Text(
                                    text = "${item.merchant.take(12)} • ${formatCurrency(item.amount, currencySymbol)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Large Full-Space Downward Laser & SMS Receipt Scanner Viewport (expands to fill the rest of the card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("sms_scanner_viewport_canvas"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val w = size.width
                    val h = size.height
                    val centerX = w / 2f
                    val centerY = h / 2f
                    val minDim = minOf(w, h)

                    // Concentric radar pulse rings scaled to the expanded viewport
                    val maxRadius = minDim * 0.47f
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.08f),
                        radius = maxRadius,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.14f),
                        radius = maxRadius * 0.74f,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.22f),
                        radius = maxRadius * (0.42f + 0.50f * animatedScanPhase),
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Large Stylized SMS Receipt Document filling the scanner viewport
                    val cardW = (w * 0.80f).coerceAtMost(380.dp.toPx())
                    val cardH = (h * 0.86f).coerceAtLeast(80.dp.toPx())
                    val cardLeft = centerX - cardW / 2f
                    val cardTop = centerY - cardH / 2f
                    val cardRight = cardLeft + cardW
                    val cardBottom = cardTop + cardH
                    val cornerRad = 16.dp.toPx()

                    drawRoundRect(
                        color = surfaceVariantColor.copy(alpha = 0.92f),
                        topLeft = Offset(cardLeft, cardTop),
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(cornerRad, cornerRad)
                    )
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.38f),
                        topLeft = Offset(cardLeft, cardTop),
                        size = Size(cardW, cardH),
                        cornerRadius = CornerRadius(cornerRad, cornerRad),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Viewfinder Corner Brackets around the scanner document
                    val bracketOffset = 8.dp.toPx()
                    val bracketLen = (minDim * 0.09f).coerceIn(14.dp.toPx(), 28.dp.toPx())
                    val bracketStroke = 3.dp.toPx()
                    val bLeft = (cardLeft - bracketOffset).coerceAtLeast(2.dp.toPx())
                    val bRight = (cardRight + bracketOffset).coerceAtMost(w - 2.dp.toPx())
                    val bTop = (cardTop - bracketOffset).coerceAtLeast(2.dp.toPx())
                    val bBottom = (cardBottom + bracketOffset).coerceAtMost(h - 2.dp.toPx())

                    // Top-Left Bracket
                    drawLine(primaryColor, Offset(bLeft, bTop), Offset(bLeft + bracketLen, bTop), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bLeft, bTop), Offset(bLeft, bTop + bracketLen), bracketStroke, StrokeCap.Round)
                    // Top-Right Bracket
                    drawLine(primaryColor, Offset(bRight, bTop), Offset(bRight - bracketLen, bTop), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bRight, bTop), Offset(bRight, bTop + bracketLen), bracketStroke, StrokeCap.Round)
                    // Bottom-Left Bracket
                    drawLine(primaryColor, Offset(bLeft, bBottom), Offset(bLeft + bracketLen, bBottom), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bLeft, bBottom), Offset(bLeft, bBottom - bracketLen), bracketStroke, StrokeCap.Round)
                    // Bottom-Right Bracket
                    drawLine(primaryColor, Offset(bRight, bBottom), Offset(bRight - bracketLen, bBottom), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bRight, bBottom), Offset(bRight, bBottom - bracketLen), bracketStroke, StrokeCap.Round)

                    // Simulated SMS Header + Multi-line Transaction Rows (illuminate as downward laser passes them)
                    val lineStartX = cardLeft + cardW * 0.10f
                    val lineMaxW = cardW * 0.80f
                    val lineFractions = listOf(0.16f, 0.29f, 0.42f, 0.55f, 0.68f, 0.81f)
                    val widthFactors = listOf(0.52f, 0.90f, 0.76f, 0.94f, 0.64f, 0.82f)
                    val lineHeightPx = (cardH * 0.036f).coerceIn(4.dp.toPx(), 8.dp.toPx())

                    lineFractions.forEachIndexed { idx, yFrac ->
                        val lineY = cardTop + cardH * yFrac
                        val isScanned = yFrac <= animatedScanPhase
                        val rowAlpha = if (isScanned) 0.62f else 0.22f
                        val rowColor = if (isScanned) primaryColor else secondaryColor
                        drawRoundRect(
                            color = rowColor.copy(alpha = rowAlpha),
                            topLeft = Offset(lineStartX, lineY),
                            size = Size(lineMaxW * widthFactors[idx], lineHeightPx),
                            cornerRadius = CornerRadius(lineHeightPx / 2f, lineHeightPx / 2f)
                        )
                    }

                    // Downward-Sweeping Horizontal Laser Scan Beam + Trailing Luminous Wake Above Beam
                    val beamY = cardTop + (cardH * animatedScanPhase)
                    val trailHeight = (cardH * 0.22f).coerceIn(22.dp.toPx(), 68.dp.toPx())
                    val trailTop = (beamY - trailHeight).coerceAtLeast(cardTop)
                    val actualTrailHeight = (beamY - trailTop).coerceAtLeast(1f)

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                secondaryColor.copy(alpha = 0.16f),
                                primaryColor.copy(alpha = 0.38f)
                            ),
                            startY = trailTop,
                            endY = beamY
                        ),
                        topLeft = Offset(cardLeft, trailTop),
                        size = Size(cardW, actualTrailHeight)
                    )
                    drawLine(
                        color = primaryColor,
                        start = Offset((cardLeft - 8.dp.toPx()).coerceAtLeast(0f), beamY),
                        end = Offset((cardRight + 8.dp.toPx()).coerceAtMost(w), beamY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Floating center status icon badge scaled for the larger scanner
                Box(
                    modifier = Modifier
                        .size(dimens.rowIconBoxSize * 1.45f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f))
                        .border(2.dp, primaryColor.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            syncState.isCompleted -> Icons.Default.Check
                            syncState.currentReadingCategory.isNotBlank() -> categoryIcon(activeCategory)
                            else -> Icons.Default.Sync
                        },
                        contentDescription = activeTitle,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimens.rowIconSize * 1.35f)
                    )
                }
            }
        }
    }
}

/**
 * Link Duplicate SMS / Transaction Dialog (`LinkDuplicateTransactionDialog`):
 * - Opened when the user swipes right on any transaction row (or taps the Link Duplicate button).
 * - Displays the swiped transaction and highlights suggested duplicate matches (matching amount or close timestamp).
 * - Consolidates both transactions' SMS signatures, raw SMS messages, tags, and notes into a single primary record.
 */
@Composable
fun LinkDuplicateTransactionDialog(
    sourceTransaction: ExpenseTransaction,
    allTransactions: List<ExpenseTransaction>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirmLink: (primaryTx: ExpenseTransaction, duplicateTx: ExpenseTransaction) -> Unit
) {
    val dimens = LocalAppDimens.current
    var searchQuery by remember { mutableStateOf("") }

    val candidates = remember(sourceTransaction.id, allTransactions, searchQuery) {
        val others = allTransactions.filter { it.id != sourceTransaction.id }
        val filtered = if (searchQuery.isBlank()) {
            others
        } else {
            val q = searchQuery.trim()
            others.filter { tx ->
                tx.title.contains(q, ignoreCase = true) ||
                    tx.accountName.contains(q, ignoreCase = true) ||
                    tx.amount.toLong().toString().contains(q)
            }
        }
        // Score candidates: exact amount match + close timestamp come first
        filtered.sortedWith(
            compareByDescending<ExpenseTransaction> {
                kotlin.math.abs(it.amount - sourceTransaction.amount) < 0.01
            }.thenByDescending {
                kotlin.math.abs(it.transactionDateMillis - sourceTransaction.transactionDateMillis) <= 48L * 3_600_000L
            }.thenBy {
                kotlin.math.abs(it.amount - sourceTransaction.amount)
            }.thenBy {
                kotlin.math.abs(it.transactionDateMillis - sourceTransaction.transactionDateMillis)
            }
        ).take(25)
    }

    var selectedTargetId by remember(candidates) {
        mutableStateOf(candidates.firstOrNull()?.id.orEmpty())
    }
    var keepTargetAsPrimary by remember { mutableStateOf(true) }

    val selectedTarget = candidates.firstOrNull { it.id == selectedTargetId }
    val sourceSmsRecords = remember(
        sourceTransaction.id,
        sourceTransaction.smsSignature,
        sourceTransaction.linkedSmsSignatures,
        sourceTransaction.notes
    ) {
        LocalAccountDataStore.getLinkedSmsMessagesForTransaction(sourceTransaction)
    }
    val selectedTargetSmsRecords = remember(
        selectedTarget?.id,
        selectedTarget?.smsSignature,
        selectedTarget?.linkedSmsSignatures,
        selectedTarget?.notes
    ) {
        selectedTarget?.let { LocalAccountDataStore.getLinkedSmsMessagesForTransaction(it) }.orEmpty()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedModalSheetContainer(
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.itemSpacing)
                    .verticalScroll(rememberScrollState())
                    .testTag("link_duplicate_transaction_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(dimens.rowIconBoxSize * 0.85f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.55f))
                                Column {
                                    Text(
                                        text = "Link Duplicate SMS / Tx",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Verify both raw SMS messages before linking",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        ReceiptDashedDividerLine()

                        // Top Raw SMS Verification Panel: Full Original SMS + Full Selected Target SMS
                        Surface(
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(dimens.smallCornerRadius)
                                )
                                .testTag("link_duplicate_sms_verification_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimens.cardPadding * 0.8f),
                                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.65f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Message,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(dimens.rowIconSize * 0.72f)
                                    )
                                    Text(
                                        text = "Raw SMS Verification",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // 1. Full Untruncated Original SMS Box
                                Surface(
                                    shape = RoundedCornerShape(dimens.smallCornerRadius * 0.85f),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("link_duplicate_original_sms_box")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(dimens.cardPadding * 0.7f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val primaryOrigSms = sourceSmsRecords.firstOrNull()
                                        val origHeaderMeta = if (primaryOrigSms != null && primaryOrigSms.sender.isNotBlank()) {
                                            "Original SMS • ${primaryOrigSms.sender} (${formatExactDateTime(primaryOrigSms.timestamp)})"
                                        } else {
                                            "Original SMS • ${formatExactDateTime(sourceTransaction.transactionDateMillis)}"
                                        }
                                        Text(
                                            text = origHeaderMeta,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        val fullOriginalSmsText = if (sourceSmsRecords.isNotEmpty()) {
                                            sourceSmsRecords.joinToString("\n\n") { it.body }
                                        } else {
                                            sourceTransaction.notes.ifBlank { "Manual transaction (no raw SMS attached)" }
                                        }
                                        Text(
                                            text = fullOriginalSmsText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // 2. Full Untruncated Selected Target SMS Box (animates smoothly when switching candidate)
                                AnimatedContent(
                                    targetState = selectedTarget?.id.orEmpty(),
                                    transitionSpec = {
                                        (slideInHorizontally(tween(200)) { it / 8 } + fadeIn(tween(180)))
                                            .togetherWith(slideOutHorizontally(tween(160)) { -it / 8 } + fadeOut(tween(140)))
                                    },
                                    label = "selected_duplicate_sms_transition"
                                ) { targetId ->
                                    val activeTarget = candidates.firstOrNull { it.id == targetId } ?: selectedTarget
                                    val activeTargetSms = if (activeTarget?.id == selectedTarget?.id) {
                                        selectedTargetSmsRecords
                                    } else {
                                        activeTarget?.let { LocalAccountDataStore.getLinkedSmsMessagesForTransaction(it) }.orEmpty()
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(dimens.smallCornerRadius * 0.85f),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("link_duplicate_selected_sms_box")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(dimens.cardPadding * 0.7f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (activeTarget != null) {
                                                val primarySelSms = activeTargetSms.firstOrNull()
                                                val selHeaderMeta = if (primarySelSms != null && primarySelSms.sender.isNotBlank()) {
                                                    "Selected SMS • ${primarySelSms.sender} (${formatExactDateTime(primarySelSms.timestamp)})"
                                                } else {
                                                    "Selected SMS • ${formatExactDateTime(activeTarget.transactionDateMillis)}"
                                                }
                                                Text(
                                                    text = selHeaderMeta,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                val fullSelectedSmsText = if (activeTargetSms.isNotEmpty()) {
                                                    activeTargetSms.joinToString("\n\n") { it.body }
                                                } else {
                                                    activeTarget.notes.ifBlank { "Manual transaction (no raw SMS attached)" }
                                                }
                                                Text(
                                                    text = fullSelectedSmsText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            } else {
                                                Text(
                                                    text = "Selected SMS",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Text(
                                                    text = "Select a transaction below to verify its raw SMS.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Concise Original Transaction Summary (Merchant, DateTime, Account, Amount ONLY)
                        Surface(
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("link_source_summary_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = dimens.horizontalPadding * 0.85f,
                                    vertical = dimens.itemSpacing * 0.7f
                                ),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "Original Transaction",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sourceTransaction.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatCurrency(sourceTransaction.amount, currencySymbol),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${formatExactDateTime(sourceTransaction.transactionDateMillis)} • ${sourceTransaction.accountName.ifBlank { "Account" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it.take(60) },
                            label = { Text("Search transaction to link with") },
                            placeholder = { Text("Merchant, amount, or account...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(dimens.smallCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("link_duplicate_search_input")
                        )

                        if (candidates.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No other transactions found to link with.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(dimens.cardPadding)
                                )
                            }
                        } else {
                            Text(
                                text = "Select Target Transaction:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.55f)
                            ) {
                                candidates.take(8).forEach { candidate ->
                                    val isSelected = candidate.id == selectedTargetId

                                    Surface(
                                        shape = RoundedCornerShape(dimens.smallCornerRadius),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(dimens.smallCornerRadius))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(dimens.smallCornerRadius)
                                            )
                                            .clickable { selectedTargetId = candidate.id }
                                            .testTag("link_candidate_row_${candidate.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = dimens.horizontalPadding * 0.85f,
                                                    vertical = dimens.itemSpacing * 0.75f
                                                ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = candidate.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${formatExactDateTime(candidate.transactionDateMillis)} • ${candidate.accountName.ifBlank { "Account" }}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = formatCurrency(candidate.amount, currencySymbol),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            if (selectedTarget != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Keep '${selectedTarget.title}' as Primary Receipt",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = keepTargetAsPrimary,
                                        onCheckedChange = { keepTargetAsPrimary = it },
                                        modifier = Modifier.testTag("keep_target_primary_switch")
                                    )
                                }
                            }
                        }

                        ReceiptDashedDividerLine()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.5f))
                            Button(
                                onClick = {
                                    val target = selectedTarget ?: return@Button
                                    if (keepTargetAsPrimary) {
                                        onConfirmLink(target, sourceTransaction)
                                    } else {
                                        onConfirmLink(sourceTransaction, target)
                                    }
                                },
                                enabled = selectedTarget != null,
                                shape = RoundedCornerShape(dimens.smallCornerRadius),
                                modifier = Modifier.testTag("confirm_link_duplicate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.78f)
                                )
                                Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.35f))
                                Text("Link & Merge Duplicate")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * CSV Data Export Dialog (`ExportTransactionsCsvDialog`):
 * - Lets the user pick a period / date range (`Current Month`, `Current FY`, `Past FY`, `This Year`,
 *   `Past Year`, `Last 30 Days`, `Last 3 Months`, `Last 7 Days`, `All Time`, or `Custom Range`)
 *   and scope (`Current Scope` vs `Family + Personal`).
 * - Previews matching transaction count, total expenses, and total income before saving via
 *   Storage Access Framework (`CreateDocument("text/csv")`) or sharing via Android Share sheet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportTransactionsCsvDialog(
    uiState: OneFamilyDashboardUiState,
    onDismiss: () -> Unit,
    onSaveCsvToFile: (csvContent: String, suggestedFileName: String, count: Int, periodLabel: String) -> Unit,
    onShareCsv: (csvContent: String, suggestedFileName: String, count: Int, periodLabel: String) -> Unit = { _, _, _, _ -> }
) {
    val dimens = LocalAppDimens.current
    val currency = uiState.activeCurrencySymbol

    var selectedPreset by remember { mutableStateOf(uiState.selectedDateRangePreset) }
    var customRange by remember {
        mutableStateOf(
            if (uiState.customStartDateMillis != null && uiState.customEndDateMillis != null) {
                uiState.customStartDateMillis to uiState.customEndDateMillis
            } else {
                val now = System.currentTimeMillis()
                (now - 29L * 86_400_000L) to now
            }
        )
    }
    var exportScope by remember { mutableStateOf(CsvExportScope.CURRENT_VIEW) }
    var showCustomPicker by remember { mutableStateOf(false) }

    val baseTransactions = remember(exportScope, uiState.allScopedExpenses, uiState.sharedExpenses, uiState.personalExpenses) {
        when (exportScope) {
            CsvExportScope.CURRENT_VIEW -> uiState.allScopedExpenses
            CsvExportScope.ALL_TRANSACTIONS -> (uiState.sharedExpenses + uiState.personalExpenses)
                .distinctBy { it.id }
                .sortedByDescending { it.transactionDateMillis }
        }
    }

    val filteredTransactions = remember(baseTransactions, selectedPreset, uiState.selectedPeriod, customRange) {
        CsvTransactionExporter.filterTransactionsForExport(
            transactions = baseTransactions,
            preset = selectedPreset,
            selectedPeriod = uiState.selectedPeriod,
            customRange = customRange
        )
    }

    val periodDisplayLabel = remember(selectedPreset, uiState.selectedPeriod, customRange) {
        DateRangeFilterUtils.formatPeriodLabel(
            preset = selectedPreset,
            selectedPeriod = uiState.selectedPeriod,
            customRange = customRange
        )
    }

    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount }
    }
    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type.equals("INCOME", ignoreCase = true) }.sumOf { it.amount }
    }

    val exportPresets = remember {
        listOf(
            DateRangePreset.MONTH,
            DateRangePreset.CURRENT_FY,
            DateRangePreset.PAST_FY,
            DateRangePreset.THIS_YEAR,
            DateRangePreset.PAST_YEAR,
            DateRangePreset.LAST_30_DAYS,
            DateRangePreset.LAST_90_DAYS,
            DateRangePreset.LAST_7_DAYS,
            DateRangePreset.ALL_TIME,
            DateRangePreset.CUSTOM
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedModalSheetContainer(
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.itemSpacing)
                    .verticalScroll(rememberScrollState())
                    .testTag("export_csv_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(dimens.rowIconBoxSize * 0.85f)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Export CSV",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(dimens.rowIconSize * 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.width(dimens.horizontalPadding * 0.55f))
                            Column {
                                Text(
                                    text = "Export Transactions (CSV)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Select financial year, calendar year, or date range",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    ReceiptDashedDividerLine()

                    Text(
                        text = "Period / Date Range",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.4f),
                        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.45f)
                    ) {
                        exportPresets.forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = {
                                    if (preset == DateRangePreset.CUSTOM) {
                                        selectedPreset = DateRangePreset.CUSTOM
                                        showCustomPicker = true
                                    } else {
                                        selectedPreset = preset
                                    }
                                },
                                leadingIcon = if (preset == DateRangePreset.CUSTOM) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            modifier = Modifier.size(dimens.rowIconSize * 0.65f)
                                        )
                                    }
                                } else {
                                    null
                                },
                                label = {
                                    Text(
                                        text = if (preset == DateRangePreset.CUSTOM && selectedPreset == DateRangePreset.CUSTOM) {
                                            periodDisplayLabel
                                        } else {
                                            preset.label
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("export_preset_chip_${preset.name.lowercase()}")
                            )
                        }
                    }

                    if (uiState.household != null) {
                        Text(
                            text = "Transactions Scope",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.45f)
                        ) {
                            CsvExportScope.entries.forEach { scopeOption ->
                                FilterChip(
                                    selected = exportScope == scopeOption,
                                    onClick = { exportScope = scopeOption },
                                    label = { Text(scopeOption.label, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("export_scope_chip_${scopeOption.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Live Export Summary Card
                    Surface(
                        shape = RoundedCornerShape(dimens.smallCornerRadius),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(dimens.cardPadding * 0.8f),
                            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing * 0.45f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = periodDisplayLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "${filteredTransactions.size} rows",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("export_matching_count_text")
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spent: ${formatCurrency(totalExpense, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Income: ${formatCurrency(totalIncome, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BudgetSafeGreen
                                )
                            }
                        }
                    }

                    ReceiptDashedDividerLine()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimens.horizontalPadding * 0.45f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val csvText = CsvTransactionExporter.buildTransactionsCsv(
                                        transactions = filteredTransactions,
                                        currencySymbol = currency
                                    )
                                    val fileName = CsvTransactionExporter.suggestCsvFileName(
                                        preset = selectedPreset,
                                        selectedPeriod = uiState.selectedPeriod,
                                        customRange = customRange
                                    )
                                    onShareCsv(csvText, fileName, filteredTransactions.size, periodDisplayLabel)
                                },
                                enabled = filteredTransactions.isNotEmpty(),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("share_export_csv_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share CSV",
                                    modifier = Modifier.size(dimens.rowIconSize * 0.75f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }

                            Button(
                                onClick = {
                                    val csvText = CsvTransactionExporter.buildTransactionsCsv(
                                        transactions = filteredTransactions,
                                        currencySymbol = currency
                                    )
                                    val fileName = CsvTransactionExporter.suggestCsvFileName(
                                        preset = selectedPreset,
                                        selectedPeriod = uiState.selectedPeriod,
                                        customRange = customRange
                                    )
                                    onSaveCsvToFile(csvText, fileName, filteredTransactions.size, periodDisplayLabel)
                                },
                                enabled = filteredTransactions.isNotEmpty(),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("confirm_export_csv_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Save CSV",
                                    modifier = Modifier.size(dimens.rowIconSize * 0.78f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save CSV")
                            }
                        }
                    }
                }
            }
            }
        }
    }

    if (showCustomPicker) {
        CustomDateRangePickerModal(
            initialStartMillis = customRange.first,
            initialEndMillis = customRange.second,
            onDismiss = { showCustomPicker = false },
            onApplyRange = { startMillis, endMillis ->
                customRange = startMillis to endMillis
                selectedPreset = DateRangePreset.CUSTOM
                showCustomPicker = false
            }
        )
    }
}



