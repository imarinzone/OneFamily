package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Switch
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.export.CsvExportScope
import com.example.data.export.CsvTransactionExporter
import com.example.data.local.LocalAccountDataStore
import com.example.data.local.LocalAccountSessionManager
import com.example.data.local.SmsRawRecord
import com.example.data.model.AccountLimitPeriod
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.data.model.FinancialAccount
import com.example.data.sms.SmsInboxReader
import com.example.ui.components.AppTimePickerDialog
import com.example.ui.components.CategoryBadgeGlyph
import com.example.ui.components.CircularTonalIconButton
import com.example.ui.components.CustomDateRangePickerModal
import com.example.ui.components.M3CheckmarkSwitch
import com.example.ui.components.ReceiptDashedDividerLine
import com.example.ui.components.ReceiptTicketCard
import com.example.ui.components.SmsSyncPeriodExpressiveSlider
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.ThemeModeSelectorBar
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatCurrency
import com.example.ui.components.localMillisToUtcPickerDateMillis
import com.example.ui.components.rememberCustomCategoryEmojis
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BudgetSafeGreen
import com.example.ui.theme.HouseholdBadgeIndigo
import com.example.ui.theme.LocalAppDimens
import com.example.ui.theme.PersonalBadgeTeal
import com.example.ui.theme.ReceiptMonoFontFamily
import com.example.ui.theme.categoryAccentColor
import com.example.ui.theme.groupedCardShape
import com.example.ui.theme.rememberAppHapticPerformer
import com.example.ui.viewmodel.CategorySpendProgress
import com.example.ui.viewmodel.DateRangeFilterUtils
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.MerchantCategoryUpdatePrompt
import com.example.ui.viewmodel.OneFamilyDashboardUiState
import com.example.ui.viewmodel.SmsSyncPeriodOption
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
private fun ModalSheetDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 4.dp, bottom = 6.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    )
}

/**
 * Centered M3 Expressive Modal Hero Header (inspired by Screenshot 1):
 * Top 24dp hero icon + centered headlineSmall title + optional bodySmall subtitle.
 */
@Composable
private fun M3ModalHeroHeader(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconTint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
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
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "modal_sheet_spring_enter"
    )
    Box(
        modifier = modifier.graphicsLayer {
            translationY = (1f - progress) * 36.dp.toPx()
            scaleX = 0.96f + 0.04f * progress
            scaleY = 0.96f + 0.04f * progress
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
    initialIsExcludedFromExpense: Boolean = false,
    initialDateMillis: Long,
    rawSmsRecord: SmsRawRecord? = null,
    linkedSmsRecords: List<SmsRawRecord> = emptyList(),
    hasHousehold: Boolean,
    accounts: List<FinancialAccount> = emptyList(),
    availableCategories: List<String>,
    currencySymbol: String,
    showDeleteButton: Boolean,
    amountTestTag: String,
    titleTestTag: String,
    saveTestTag: String,
    onDelete: (() -> Unit)? = null,
    onLinkDuplicate: (() -> Unit)? = null,
    onCreateAccount: (
        (
            name: String,
            bankName: String,
            accountType: String,
            lastFour: String,
            balance: Double,
            creditLimit: Double,
            linkedBankAccountId: String,
            linkedBankAccountName: String,
            monthlySpendLimit: Double,
            billingCycleDay: Int
        ) -> Unit
    )? = null,
    onDismiss: () -> Unit,
    onSaveExcluded: (Boolean) -> Unit = {},
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
    val performHaptic = rememberAppHapticPerformer()
    var title by remember { mutableStateOf(initialTitle) }
    var amountText by remember { mutableStateOf(initialAmountText) }
    var type by remember { mutableStateOf(initialType) }
    var category by remember { mutableStateOf(ExpenseCategories.normalizeCategory(initialCategory)) }
    var customCategoryInput by remember { mutableStateOf("") }
    var showCustomCategoryField by remember { mutableStateOf(false) }
    var paymentMode by remember { mutableStateOf(initialPaymentMode) }
    var accountName by remember { mutableStateOf(initialAccountName) }
    var createdAccountsInSession by remember { mutableStateOf<List<FinancialAccount>>(emptyList()) }
    var showAccountPickerDialog by remember { mutableStateOf(false) }
    var showAddNewAccountFromPicker by remember { mutableStateOf(false) }
    var tagsInput by remember { mutableStateOf(initialTagsInput) }
    var notes by remember { mutableStateOf(initialNotes) }
    var isShared by remember { mutableStateOf(initialIsShared && hasHousehold) }
    var isExcludedFromExpense by remember { mutableStateOf(initialIsExcludedFromExpense) }
    var txDateMillis by remember { mutableLongStateOf(initialDateMillis) }
    val isEditMode = showDeleteButton
    var showMoreEditDetails by remember { mutableStateOf(false) }
    var showExtraDetails by remember { mutableStateOf(initialNotes.isNotBlank() || initialTagsInput.isNotBlank()) }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var showTimePickerModal by remember { mutableStateOf(false) }

    val selectableAccounts = remember(accounts, createdAccountsInSession) {
        (accounts + createdAccountsInSession).distinctBy { it.name.trim().lowercase() }
    }

    LaunchedEffect(selectableAccounts) {
        if (accountName.isBlank() && selectableAccounts.isNotEmpty()) {
            accountName = selectableAccounts.first().name
        }
    }

    val catAccent = categoryAccentColor(category)
    val receiptContext = LocalContext.current
    val customEmojis = rememberCustomCategoryEmojis()
    var customCategoryEmoji by remember { mutableStateOf("") }
    var attemptedSubmit by remember { mutableStateOf(false) }

    val allCats = remember(availableCategories, category, customCategoryInput) {
        (availableCategories + listOf(category) + listOfNotNull(customCategoryInput.trim().takeIf { it.isNotBlank() }))
            .map { ExpenseCategories.normalizeCategory(it) }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }
    val spendCategories = remember(allCats) {
        allCats.filter { !ExpenseCategories.isIncomeCategory(it) }
    }
    val incomeCategories = remember(allCats) {
        allCats.filter { ExpenseCategories.isIncomeCategory(it) }.ifEmpty {
            ExpenseCategories.INCOME_CATEGORIES
        }
    }

    val accountMissingError = attemptedSubmit && selectableAccounts.isNotEmpty() && accountName.isBlank()

    val submitReceipt: () -> Unit = {
        attemptedSubmit = true
        val amtErr = validatePositiveAmount(amountText)
        val catErr = if (showCustomCategoryField && customCategoryInput.isNotBlank()) {
            validateCategoryName(customCategoryInput)
        } else {
            null
        }
        val amt = amountText.toDoubleOrNull() ?: 0.0
        val hasValidAccount = selectableAccounts.isEmpty() || accountName.isNotBlank()
        if (amtErr == null && catErr == null && amt > 0.0 && hasValidAccount) {
            performHaptic()
            val finalCat = ExpenseCategories.normalizeCategory(
                if (showCustomCategoryField && customCategoryInput.isNotBlank()) {
                    customCategoryInput.trim()
                } else {
                    category
                }
            )
            if (showCustomCategoryField && customCategoryInput.isNotBlank() && customCategoryEmoji.isNotBlank()) {
                LocalAccountSessionManager.addCustomCategory(
                    context = receiptContext,
                    rawCategory = finalCat,
                    emoji = customCategoryEmoji
                )
            }
            val effectiveExcluded = isExcludedFromExpense
            onSaveExcluded(effectiveExcluded)
            val resolvedAccount = accountName.trim().ifBlank {
                selectableAccounts.firstOrNull()?.name ?: "Unassigned Account"
            }
            onSave(
                title.trim().ifBlank { finalCat },
                amt,
                type,
                finalCat,
                resolvedAccount,
                paymentMode,
                tagsInput.trim(),
                notes.trim(),
                isShared,
                txDateMillis
            )
        }
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimens.cardPadding, vertical = dimens.itemSpacing)
                            .animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ModalSheetDragHandle()

                        // Row 1: Primary Header (Category Emblem + Title + Close/Save always unobstructed on all phone sizes)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val activeCatEmoji = customEmojis[category.trim().lowercase()]
                                SquircleIconBadge(
                                    icon = categoryIcon(category),
                                    tint = catAccent,
                                    containerColor = catAccent.copy(alpha = 0.16f),
                                    size = 42.dp,
                                    iconSize = 20.dp,
                                    emoji = activeCatEmoji,
                                    contentDescription = category
                                )
                                Text(
                                    text = if (showDeleteButton) "Edit Transaction" else "New Transaction",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularTonalIconButton(
                                    icon = Icons.Default.Close,
                                    contentDescription = "Close",
                                    onClick = onDismiss
                                )
                                Button(
                                    onClick = submitReceipt,
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (type == "INCOME") BudgetSafeGreen else MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag(saveTestTag)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Save Receipt",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

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
                        val allSmsRecords = remember(rawSmsRecord, linkedSmsRecords) {
                            buildList {
                                if (rawSmsRecord != null && rawSmsRecord.body.isNotBlank()) add(rawSmsRecord)
                                linkedSmsRecords.filter { it.body.isNotBlank() }.forEach { rec ->
                                    if (none { it.signature == rec.signature || it.body == rec.body }) {
                                        add(rec)
                                    }
                                }
                            }
                        }
                        val selectedAccountObj = remember(selectableAccounts, accountName) {
                            selectableAccounts.firstOrNull {
                                it.name.equals(accountName.trim(), ignoreCase = true)
                            }
                        }
                        val isIncomeType = type.equals("INCOME", ignoreCase = true)

                        val renderCategorySection: @Composable () -> Unit = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Category",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = catAccent.copy(alpha = 0.16f),
                                        modifier = Modifier.testTag("receipt_selected_category_pill")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            CategoryBadgeGlyph(
                                                category = category,
                                                tint = catAccent,
                                                iconSize = 14.dp,
                                                customEmoji = customEmojis[category.trim().lowercase()],
                                                contentDescription = null
                                            )
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = catAccent,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                TextButton(
                                    onClick = {
                                        performHaptic()
                                        showCustomCategoryField = !showCustomCategoryField
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Custom", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            AnimatedVisibility(visible = showCustomCategoryField) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    OutlinedTextField(
                                        value = customCategoryEmoji,
                                        onValueChange = {
                                            customCategoryEmoji = LocalAccountSessionManager.extractFirstEmojiCluster(it)
                                        },
                                        label = { Text("Emoji") },
                                        placeholder = { Text("✨") },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .width(82.dp)
                                            .testTag("receipt_custom_category_emoji_input")
                                    )
                                    OutlinedTextField(
                                        value = customCategoryInput,
                                        onValueChange = {
                                            customCategoryInput = it.take(40)
                                            if (it.isNotBlank()) {
                                                val trimmed = ExpenseCategories.normalizeCategory(it.trim())
                                                category = trimmed
                                                if (ExpenseCategories.isIncomeCategory(trimmed)) {
                                                    type = "INCOME"
                                                }
                                            }
                                        },
                                        label = { Text("Custom Category") },
                                        isError = customCatError != null,
                                        supportingText = customCatError?.let { err -> { Text(err) } },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            val renderCategoryNode: @Composable (String) -> Unit = { cat ->
                                val isSelected = category.equals(cat, ignoreCase = true)
                                val chipAccent = categoryAccentColor(cat)
                                val neutralBg = MaterialTheme.colorScheme.surfaceContainerHighest
                                val chipEmoji = customEmojis[cat.trim().lowercase()]
                                val catChipTag = cat.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) chipAccent else neutralBg,
                                    shadowElevation = if (isSelected) 4.dp else 0.dp,
                                    border = if (isSelected) {
                                        BorderStroke(2.dp, Color.White.copy(alpha = 0.9f))
                                    } else {
                                        BorderStroke(1.dp, chipAccent.copy(alpha = 0.35f))
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .semantics { contentDescription = cat }
                                        .clickable {
                                            performHaptic()
                                            category = cat
                                            if (ExpenseCategories.isIncomeCategory(cat)) {
                                                type = "INCOME"
                                            } else {
                                                type = "EXPENSE"
                                            }
                                        }
                                        .testTag("receipt_category_chip_$catChipTag")
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CategoryBadgeGlyph(
                                            category = cat,
                                            tint = if (isSelected) Color.White else chipAccent,
                                            iconSize = if (isSelected) 22.dp else 20.dp,
                                            customEmoji = chipEmoji,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }

                            val activeCategories = if (type == "INCOME") incomeCategories else spendCategories
                            val divisionTag = if (type == "INCOME") {
                                "receipt_category_division_income"
                            } else {
                                "receipt_category_division_spend"
                            }
                            if (activeCategories.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag(divisionTag),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isEditMode) {
                                        val orderedEditCategories = remember(activeCategories, initialCategory) {
                                            val initialNorm = ExpenseCategories.normalizeCategory(initialCategory)
                                            val match = activeCategories.firstOrNull {
                                                it.equals(initialNorm, ignoreCase = true)
                                            }
                                            if (match != null) {
                                                listOf(match) + activeCategories.filterNot {
                                                    it.equals(match, ignoreCase = true)
                                                }
                                            } else {
                                                activeCategories
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            orderedEditCategories.forEach { cat ->
                                                renderCategoryNode(cat)
                                            }
                                        }
                                    } else {
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            activeCategories.forEach { cat ->
                                                renderCategoryNode(cat)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        val renderExcludeToggleCard: @Composable () -> Unit = {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isExcludedFromExpense) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        performHaptic()
                                        isExcludedFromExpense = !isExcludedFromExpense
                                    }
                                    .testTag("transaction_exclude_expense_row")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoneyOff,
                                            contentDescription = null,
                                            tint = if (isExcludedFromExpense) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isIncomeType) {
                                                    "Exclude from Income"
                                                } else {
                                                    "Exclude from Expenses"
                                                },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isExcludedFromExpense) {
                                                    MaterialTheme.colorScheme.onSecondaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                            Text(
                                                text = if (isIncomeType) {
                                                    "Ignore in monthly income & trends"
                                                } else {
                                                    "Ignore in monthly spend, budgets & trends"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isExcludedFromExpense) {
                                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isExcludedFromExpense,
                                        onCheckedChange = {
                                            performHaptic()
                                            isExcludedFromExpense = it
                                        },
                                        modifier = Modifier.testTag("transaction_exclude_expense_switch")
                                    )
                                }
                            }
                        }

                        val renderScopeAndToolsRow: @Composable () -> Unit = {
                            if (hasHousehold || onLinkDuplicate != null || (showDeleteButton && onDelete != null)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (hasHousehold) {
                                        val stampColor = if (isShared) HouseholdBadgeIndigo else PersonalBadgeTeal
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = stampColor.copy(alpha = 0.14f),
                                            modifier = Modifier
                                                .heightIn(min = 40.dp)
                                                .clip(RoundedCornerShape(50))
                                                .border(1.5.dp, stampColor, RoundedCornerShape(50))
                                                .clickable {
                                                    performHaptic()
                                                    isShared = !isShared
                                                }
                                                .testTag("transaction_shared_switch")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 8.dp
                                                ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isShared) Icons.Default.Groups else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = stampColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isShared) "HOUSEHOLD" else "PERSONAL",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = stampColor,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    if (onLinkDuplicate != null || (showDeleteButton && onDelete != null)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (onLinkDuplicate != null) {
                                                CircularTonalIconButton(
                                                    icon = Icons.Default.Link,
                                                    contentDescription = "Link Duplicate SMS / Transaction",
                                                    onClick = onLinkDuplicate,
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.testTag("edit_dialog_link_duplicate_button")
                                                )
                                            }
                                            if (showDeleteButton && onDelete != null) {
                                                CircularTonalIconButton(
                                                    icon = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete Receipt",
                                                    onClick = onDelete,
                                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.testTag("edit_dialog_delete_button")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        val renderTypeSegmentedRow: @Composable () -> Unit = {
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                val isExp = type == "EXPENSE"
                                SegmentedButton(
                                    selected = isExp,
                                    onClick = {
                                        performHaptic()
                                        type = "EXPENSE"
                                        if (ExpenseCategories.isIncomeCategory(category)) {
                                            category = spendCategories.firstOrNull() ?: "Groceries"
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                    icon = {
                                        SegmentedButtonDefaults.Icon(active = isExp) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("receipt_type_chip_expense")
                                ) {
                                    Text("Expense", fontWeight = if (isExp) FontWeight.Bold else FontWeight.Medium)
                                }

                                val isInc = type == "INCOME"
                                SegmentedButton(
                                    selected = isInc,
                                    onClick = {
                                        performHaptic()
                                        type = "INCOME"
                                        if (!ExpenseCategories.isIncomeCategory(category)) {
                                            category = incomeCategories.firstOrNull() ?: "Salary & Income"
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                    icon = {
                                        SegmentedButtonDefaults.Icon(active = isInc) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("receipt_type_chip_income")
                                ) {
                                    Text("Income", fontWeight = if (isInc) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        }

                        val renderPaymentAccountDateBlock: @Composable () -> Unit = {
                            // Payment Method Row (Tonal Preset Chips)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("UPI", "CARD", "BANK", "CASH", "WALLET").forEach { mode ->
                                    val selectedMode = paymentMode.equals(mode, ignoreCase = true)
                                    FilterChip(
                                        selected = selectedMode,
                                        onClick = {
                                            performHaptic()
                                            paymentMode = mode
                                        },
                                        leadingIcon = if (selectedMode) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        } else {
                                            null
                                        },
                                        label = { Text(mode, style = MaterialTheme.typography.labelSmall) },
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Account / Card Popup Selector
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (accountMissingError) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        performHaptic()
                                        showAccountPickerDialog = true
                                    }
                                    .testTag("tx_account_picker_button")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = when (FinancialAccount.normalizeAccountType(selectedAccountObj?.accountType ?: "")) {
                                                "CREDIT_CARD", "DEBIT_CARD" -> Icons.Default.CreditCard
                                                "WALLET", "CASH" -> Icons.Default.AccountBalanceWallet
                                                else -> Icons.Default.AccountBalance
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Account / Card",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (accountName.isNotBlank()) {
                                                    accountName
                                                } else if (selectableAccounts.isNotEmpty()) {
                                                    selectableAccounts.first().name
                                                } else {
                                                    "Tap to select or add account"
                                                },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (accountName.isNotBlank() || selectableAccounts.isNotEmpty()) {
                                                    MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.testTag("tx_selected_account_label")
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Choose Account",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (accountMissingError) {
                                Text(
                                    text = "Please choose an existing account or add a new account.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, start = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Exact Date & Timestamp Selector Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = { showDatePickerModal = true },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tx_date_picker_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatExactDate(txDateMillis),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showTimePickerModal = true },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tx_time_picker_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatExactTime(txDateMillis),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        val renderRawSmsBox: @Composable () -> Unit = {
                            if (allSmsRecords.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("edit_dialog_raw_sms_box"),
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (allSmsRecords.size > 1) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Link,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
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
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = rec.sender.ifBlank { "SMS" },
                                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Text(
                                                    text = formatFullTimestamp(
                                                        if (rec.timestamp > 0L) rec.timestamp else txDateMillis
                                                    ),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = rec.body,
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = ReceiptMonoFontFamily),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isEditMode) {
                            // Simplified Edit Transaction View:
                            // 1) Main Hero Banner showing ONLY the editable amount
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("edit_tx_amount_hero_banner")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    TextField(
                                        value = amountText,
                                        onValueChange = { amountText = sanitizeDecimalInput(it) },
                                        prefix = {
                                            Text(
                                                text = currencySymbol,
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontFamily = ReceiptMonoFontFamily,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = if (isIncomeType) {
                                                    BudgetSafeGreen
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                text = "0",
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    fontFamily = ReceiptMonoFontFamily,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    textAlign = TextAlign.Center
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                                            fontFamily = ReceiptMonoFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isIncomeType) {
                                                BudgetSafeGreen
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            textAlign = TextAlign.Center
                                        ),
                                        isError = amountError != null,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            errorContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            errorIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag(amountTestTag)
                                    )
                                    if (amountError != null) {
                                        Text(
                                            text = amountError,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // 2) Category Selector
                            Spacer(modifier = Modifier.height(14.dp))
                            renderCategorySection()

                            // 3) Exclude from Expense / Income Toggle
                            Spacer(modifier = Modifier.height(14.dp))
                            renderExcludeToggleCard()

                            // 4) Collapsible "Show More" button housing all secondary edit fields
                            Spacer(modifier = Modifier.height(12.dp))
                            FilledTonalButton(
                                onClick = {
                                    performHaptic()
                                    showMoreEditDetails = !showMoreEditDetails
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("edit_tx_show_more_button")
                            ) {
                                Text(
                                    text = if (showMoreEditDetails) "Show Less" else "Show More",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (showMoreEditDetails) {
                                        Icons.Default.ExpandLess
                                    } else {
                                        Icons.Default.ExpandMore
                                    },
                                    contentDescription = if (showMoreEditDetails) "Collapse details" else "Expand details",
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = showMoreEditDetails) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .testTag("edit_tx_more_details_section"),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
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
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag(titleTestTag)
                                    )

                                    renderTypeSegmentedRow()

                                    renderScopeAndToolsRow()

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        renderPaymentAccountDateBlock()
                                    }

                                    OutlinedTextField(
                                        value = tagsInput,
                                        onValueChange = { tagsInput = it.take(120) },
                                        label = { Text("#Tags") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("add_tx_tags_input")
                                    )

                                    OutlinedTextField(
                                        value = notes,
                                        onValueChange = { notes = it.take(500) },
                                        label = { Text("Note (${notes.length}/500)") },
                                        maxLines = 3,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    renderRawSmsBox()
                                }
                            }
                        } else {
                            // Full Add New Transaction Flow
                            if (hasHousehold) {
                                Spacer(modifier = Modifier.height(8.dp))
                                renderScopeAndToolsRow()
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            renderTypeSegmentedRow()

                            if (allSmsRecords.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                renderRawSmsBox()
                            }

                            ReceiptDashedDividerLine()

                            // Big Receipt Total & Merchant Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { amountText = sanitizeDecimalInput(it) },
                                    label = { Text("Total ($currencySymbol)") },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = ReceiptMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    isError = amountError != null,
                                    supportingText = amountError?.let { err -> { Text(err) } },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
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
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(0.52f)
                                        .testTag(titleTestTag)
                                )
                            }

                            ReceiptDashedDividerLine()

                            renderCategorySection()

                            ReceiptDashedDividerLine()

                            renderPaymentAccountDateBlock()

                            Spacer(modifier = Modifier.height(8.dp))
                            renderExcludeToggleCard()

                            // Progressive Disclosure: Expandable More Details
                            TextButton(
                                onClick = { showExtraDetails = !showExtraDetails },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (showExtraDetails) "Less Details" else "More Details",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (showExtraDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = showExtraDetails) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = tagsInput,
                                        onValueChange = { tagsInput = it.take(120) },
                                        label = { Text("#Tags") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("add_tx_tags_input")
                                    )
                                    OutlinedTextField(
                                        value = notes,
                                        onValueChange = { notes = it.take(500) },
                                        label = { Text("Note (${notes.length}/500)") },
                                        maxLines = 3,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = submitReceipt,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "INCOME") BudgetSafeGreen else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("${saveTestTag}_bottom")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showDeleteButton) "Save Changes" else "Save Transaction",
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

    if (showAccountPickerDialog) {
        Dialog(onDismissRequest = { showAccountPickerDialog = false }) {
            AnimatedModalSheetContainer {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("tx_account_picker_dialog")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        M3ModalHeroHeader(
                            icon = Icons.Default.AccountBalance,
                            title = "Choose Account / Card",
                            subtitle = "Select an existing account or add a new one"
                        )

                        ReceiptDashedDividerLine()

                        if (selectableAccounts.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No accounts added yet. Tap 'Add New Account' below to create one and select it.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            selectableAccounts.forEach { acc ->
                                val isChosen = acc.name.equals(accountName.trim(), ignoreCase = true)
                                val safeAccTag = acc.name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                                val accTypeNorm = FinancialAccount.normalizeAccountType(acc.accountType)
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isChosen) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainer
                                    },
                                    border = if (isChosen) {
                                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable {
                                            performHaptic()
                                            accountName = acc.name
                                            showAccountPickerDialog = false
                                        }
                                        .testTag("tx_picker_account_option_$safeAccTag")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = when (accTypeNorm) {
                                                    "CREDIT_CARD", "DEBIT_CARD" -> Icons.Default.CreditCard
                                                    "WALLET", "CASH" -> Icons.Default.AccountBalanceWallet
                                                    else -> Icons.Default.AccountBalance
                                                },
                                                contentDescription = null,
                                                tint = if (isChosen) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = acc.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isChosen) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                Text(
                                                    text = accTypeNorm.replace('_', ' '),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isChosen) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    }
                                                )
                                            }
                                        }
                                        if (isChosen) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                performHaptic()
                                showAccountPickerDialog = false
                                showAddNewAccountFromPicker = true
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tx_picker_add_new_account_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add New Account", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showAccountPickerDialog = false },
                                modifier = Modifier.testTag("tx_picker_close_button")
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNewAccountFromPicker) {
        val initialTypeFromMode = when (paymentMode.uppercase()) {
            "CARD" -> "CREDIT_CARD"
            "WALLET" -> "WALLET"
            "CASH" -> "CASH"
            else -> "BANK"
        }
        AddAccountDialog(
            currencySymbol = currencySymbol,
            accounts = selectableAccounts,
            initialAccountType = initialTypeFromMode,
            onDismiss = { showAddNewAccountFromPicker = false },
            onSaveWithBillingCycle = { name, bankName, accountType, lastFour, balance, creditLimit, linkedBankId, linkedBankName, spendLimit, billingCycleDay ->
                val formattedDisplayName = if (lastFour.isNotBlank() && !name.contains(lastFour)) {
                    "$name ••$lastFour"
                } else {
                    name
                }
                val newAcc = FinancialAccount(
                    id = "temp_${System.currentTimeMillis()}",
                    name = formattedDisplayName,
                    bankName = bankName,
                    accountType = accountType,
                    lastFourDigits = lastFour,
                    balance = balance,
                    creditLimit = creditLimit,
                    linkedBankAccountId = linkedBankId,
                    linkedBankAccountName = linkedBankName,
                    monthlySpendLimit = spendLimit,
                    billingCycleDay = billingCycleDay
                )
                createdAccountsInSession = createdAccountsInSession + newAcc
                accountName = formattedDisplayName
                onCreateAccount?.invoke(
                    name,
                    bankName,
                    accountType,
                    lastFour,
                    balance,
                    creditLimit,
                    linkedBankId,
                    linkedBankName,
                    spendLimit,
                    billingCycleDay
                )
                showAddNewAccountFromPicker = false
            }
        )
    }

    if (showDatePickerModal) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = localMillisToUtcPickerDateMillis(txDateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerModal = false },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.testTag("app_date_picker_dialog"),
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
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("date_picker_confirm_button")
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerModal = false },
                    modifier = Modifier.testTag("date_picker_dismiss_button")
                ) {
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
        AppTimePickerDialog(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            title = "Transaction Time",
            is24Hour = false,
            onDismiss = { showTimePickerModal = false },
            onConfirmTime = { hour, minute ->
                txDateMillis = mergeSelectedTimePreservingDate(
                    hourOfDay = hour,
                    minute = minute,
                    currentTimestampMillis = txDateMillis
                )
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
    onCreateAccount: (
        (
            name: String,
            bankName: String,
            accountType: String,
            lastFour: String,
            balance: Double,
            creditLimit: Double,
            linkedBankAccountId: String,
            linkedBankAccountName: String,
            monthlySpendLimit: Double,
            billingCycleDay: Int
        ) -> Unit
    )? = null,
    onSaveExcluded: (Boolean) -> Unit = {},
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
        initialAccountName = accounts.firstOrNull()?.name ?: "",
        initialTagsInput = "",
        initialNotes = "",
        initialIsShared = hasHousehold && defaultShared,
        initialIsExcludedFromExpense = false,
        initialDateMillis = System.currentTimeMillis(),
        rawSmsRecord = null,
        hasHousehold = hasHousehold,
        accounts = accounts,
        availableCategories = availableCategories,
        currencySymbol = currencySymbol,
        showDeleteButton = false,
        amountTestTag = "add_tx_amount_input",
        titleTestTag = "add_tx_title_input",
        saveTestTag = "save_transaction_button",
        onCreateAccount = onCreateAccount,
        onDismiss = onDismiss,
        onSaveExcluded = onSaveExcluded,
        onSave = onSave
    )
}

@Composable
fun EditTransactionDialog(
    transaction: ExpenseTransaction,
    hasHousehold: Boolean,
    accounts: List<FinancialAccount> = emptyList(),
    availableCategories: List<String>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onLinkDuplicate: (() -> Unit)? = null,
    onCreateAccount: (
        (
            name: String,
            bankName: String,
            accountType: String,
            lastFour: String,
            balance: Double,
            creditLimit: Double,
            linkedBankAccountId: String,
            linkedBankAccountName: String,
            monthlySpendLimit: Double,
            billingCycleDay: Int
        ) -> Unit
    )? = null,
    onSaveExcluded: (Boolean) -> Unit = {},
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
        initialNotes = SmsInboxReader.stripSyntheticAutoParsedNote(transaction.notes),
        initialIsShared = transaction.visibility == "SHARED" && hasHousehold,
        initialIsExcludedFromExpense = transaction.isExcludedFromExpense,
        initialDateMillis = transaction.transactionDateMillis,
        rawSmsRecord = rawSmsRecord,
        linkedSmsRecords = linkedSmsRecords,
        hasHousehold = hasHousehold,
        accounts = accounts,
        availableCategories = availableCategories,
        currencySymbol = currencySymbol,
        showDeleteButton = true,
        amountTestTag = "edit_tx_amount_input",
        titleTestTag = "edit_tx_title_input",
        saveTestTag = "save_edited_transaction_button",
        onDelete = onDelete,
        onLinkDuplicate = onLinkDuplicate,
        onCreateAccount = onCreateAccount,
        onDismiss = onDismiss,
        onSaveExcluded = onSaveExcluded,
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

/**
 * Interactive 360° Circular Scroll Wheel Category Editor (`QuickCategoryPickerDialog`):
 * Triggered when the user long-presses a transaction's category icon.
 * Allows spinning/dragging around the circular dial (or tapping any radial node / step button)
 * to edit the transaction's category with tactile haptics.
 */
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
    val performHaptic = rememberAppHapticPerformer()
    val density = LocalDensity.current

    val isIncomeTx = transaction.type.equals("INCOME", ignoreCase = true)
    val categories = remember(availableCategories, transaction.category, isIncomeTx) {
        val raw = (availableCategories + listOf(transaction.category))
            .map { ExpenseCategories.normalizeCategory(it) }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        val filtered = if (isIncomeTx) {
            raw.filter { ExpenseCategories.isIncomeCategory(it) }
        } else {
            raw.filter { !ExpenseCategories.isIncomeCategory(it) }
        }
        filtered.ifEmpty {
            if (isIncomeTx) ExpenseCategories.INCOME_CATEGORIES else ExpenseCategories.EXPENSE_ONLY
        }
    }

    val initialIdx = remember(categories, transaction.category) {
        categories.indexOfFirst { it.equals(transaction.category, ignoreCase = true) }.coerceAtLeast(0)
    }

    val count = categories.size.coerceAtLeast(1)
    val stepAngleDeg = 360f / count.toFloat()

    // Wheel rotation angle in degrees; when rotationAngleDeg == -idx * stepAngleDeg, category[idx] sits at 12 o'clock (-90 deg)
    var wheelRotationDeg by remember(transaction.id, count) {
        mutableFloatStateOf(-initialIdx * stepAngleDeg)
    }
    var selectedIdx by remember(transaction.id, count) {
        mutableIntStateOf(initialIdx)
    }
    var customCategory by remember { mutableStateOf("") }

    val selectedCat = categories.getOrElse(selectedIdx.coerceIn(0, categories.lastIndex)) {
        transaction.category
    }

    val animatedWheelRotationDeg by animateFloatAsState(
        targetValue = wheelRotationDeg,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "circular_category_wheel_rotation"
    )

    fun normalizePositiveMod(value: Int, mod: Int): Int {
        val r = value % mod
        return if (r < 0) r + mod else r
    }

    fun updateSelectionFromAngle(newAngleDeg: Float) {
        val rawSlot = kotlin.math.round(-newAngleDeg / stepAngleDeg).toInt()
        val idx = normalizePositiveMod(rawSlot, count)
        if (idx != selectedIdx) {
            selectedIdx = idx
            performHaptic()
        }
    }

    fun stepWheelBy(deltaSlots: Int) {
        val nextIdx = normalizePositiveMod(selectedIdx + deltaSlots, count)
        selectedIdx = nextIdx
        wheelRotationDeg -= deltaSlots * stepAngleDeg
        customCategory = ""
        performHaptic()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedModalSheetContainer(
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("circular_category_wheel_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    M3ModalHeroHeader(
                        icon = categoryIcon(selectedCat),
                        title = transaction.title,
                        subtitle = "Spin the circular wheel or tap an icon to change category",
                        iconTint = categoryAccentColor(selectedCat)
                    )

                    ReceiptDashedDividerLine()

                    // Step Counter-Clockwise / Active Slot Pill / Step Clockwise Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularTonalIconButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Category on Wheel",
                            onClick = { stepWheelBy(-1) },
                            modifier = Modifier.testTag("wheel_step_prev_button")
                        )

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = categoryAccentColor(selectedCat).copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = null,
                                    tint = categoryAccentColor(selectedCat),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${selectedIdx + 1} / $count • $selectedCat",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = categoryAccentColor(selectedCat)
                                )
                            }
                        }

                        CircularTonalIconButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Category on Wheel",
                            onClick = { stepWheelBy(1) },
                            modifier = Modifier.testTag("wheel_step_next_button")
                        )
                    }

                    // 360° Interactive Circular Scroll Wheel
                    val wheelSizeDp = 284.dp
                    val orbitRadiusDp = 104.dp
                    val nodeSizeDp = 46.dp
                    var wheelSizePx by remember { mutableFloatStateOf(1f) }

                    Box(
                        modifier = Modifier
                            .size(wheelSizeDp)
                            .onSizeChanged { wheelSizePx = it.width.toFloat().coerceAtLeast(1f) }
                            .pointerInput(count) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val center = wheelSizePx / 2f
                                        val prevPos = change.previousPosition
                                        val currPos = change.position
                                        val prevDx = prevPos.x - center
                                        val prevDy = prevPos.y - center
                                        val currDx = currPos.x - center
                                        val currDy = currPos.y - center
                                        val distSq = currDx * currDx + currDy * currDy
                                        val deltaDeg = if (distSq > 400f) {
                                            val prevAngle = Math.toDegrees(kotlin.math.atan2(prevDy.toDouble(), prevDx.toDouble())).toFloat()
                                            val currAngle = Math.toDegrees(kotlin.math.atan2(currDy.toDouble(), currDx.toDouble())).toFloat()
                                            var diff = currAngle - prevAngle
                                            while (diff > 180f) diff -= 360f
                                            while (diff < -180f) diff += 360f
                                            diff
                                        } else {
                                            // Fallback tangential conversion near center
                                            (dragAmount.x - dragAmount.y) * 0.45f
                                        }
                                        val nextRot = wheelRotationDeg + deltaDeg
                                        wheelRotationDeg = nextRot
                                        customCategory = ""
                                        updateSelectionFromAngle(nextRot)
                                    },
                                    onDragEnd = {
                                        // Snap cleanly to the nearest category slot
                                        val snappedSlot = kotlin.math.round(-wheelRotationDeg / stepAngleDeg).toInt()
                                        wheelRotationDeg = -snappedSlot * stepAngleDeg
                                        selectedIdx = normalizePositiveMod(snappedSlot, count)
                                    }
                                )
                            }
                            .testTag("circular_category_wheel"),
                        contentAlignment = Alignment.Center
                    ) {
                        val activeAccent = categoryAccentColor(selectedCat)
                        val trackColor = MaterialTheme.colorScheme.outlineVariant
                        val surfaceLowest = MaterialTheme.colorScheme.surfaceContainerLowest

                        // Wheel Dial Track, Tick Marks & 12-o'clock Active Selector Pointer
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val orbitRadiusPx = with(density) { orbitRadiusDp.toPx() }
                            val outerRingRadiusPx = orbitRadiusPx + with(density) { 24.dp.toPx() }

                            drawCircle(
                                color = surfaceLowest,
                                radius = outerRingRadiusPx,
                                center = Offset(cx, cy)
                            )

                            drawCircle(
                                color = trackColor.copy(alpha = 0.45f),
                                radius = orbitRadiusPx,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Radial tick marks around the dial
                            val tickCount = count * 2
                            for (t in 0 until tickCount) {
                                val tickAngleRad = Math.toRadians((animatedWheelRotationDeg + t * (360f / tickCount) - 90f).toDouble())
                                val isMajor = t % 2 == 0
                                val innerR = outerRingRadiusPx - (if (isMajor) 10.dp.toPx() else 5.dp.toPx())
                                val start = Offset(
                                    x = cx + (innerR * kotlin.math.cos(tickAngleRad)).toFloat(),
                                    y = cy + (innerR * kotlin.math.sin(tickAngleRad)).toFloat()
                                )
                                val end = Offset(
                                    x = cx + (outerRingRadiusPx * kotlin.math.cos(tickAngleRad)).toFloat(),
                                    y = cy + (outerRingRadiusPx * kotlin.math.sin(tickAngleRad)).toFloat()
                                )
                                drawLine(
                                    color = if (isMajor) activeAccent.copy(alpha = 0.55f) else trackColor.copy(alpha = 0.35f),
                                    start = start,
                                    end = end,
                                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // 12 o'clock highlight halo for the active category slot
                            val topSlotCenter = Offset(cx, cy - orbitRadiusPx)
                            drawCircle(
                                color = activeAccent.copy(alpha = 0.20f),
                                radius = 29.dp.toPx(),
                                center = topSlotCenter
                            )
                        }

                        // Radial Category Nodes positioned around the 360° wheel
                        val orbitRadiusPx = with(density) { orbitRadiusDp.toPx() }
                        categories.forEachIndexed { index, cat ->
                            val angleDeg = animatedWheelRotationDeg + (index * stepAngleDeg) - 90f
                            val angleRad = Math.toRadians(angleDeg.toDouble())
                            val offsetX = (orbitRadiusPx * kotlin.math.cos(angleRad)).toInt()
                            val offsetY = (orbitRadiusPx * kotlin.math.sin(angleRad)).toInt()
                            val isNodeSelected = index == selectedIdx && customCategory.isBlank()
                            val nodeAccent = categoryAccentColor(cat)
                            val catTag = cat.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                            val nodeScale by animateFloatAsState(
                                targetValue = if (isNodeSelected) 1.18f else 0.92f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "wheel_node_scale_$index"
                            )

                            Surface(
                                shape = CircleShape,
                                color = if (isNodeSelected) nodeAccent else MaterialTheme.colorScheme.surfaceContainerHighest,
                                shadowElevation = if (isNodeSelected) 6.dp else 1.dp,
                                modifier = Modifier
                                    .offset { IntOffset(offsetX, offsetY) }
                                    .size(nodeSizeDp)
                                    .graphicsLayer {
                                        scaleX = nodeScale
                                        scaleY = nodeScale
                                    }
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isNodeSelected) 2.5.dp else 1.dp,
                                        color = if (isNodeSelected) Color.White else nodeAccent.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        performHaptic()
                                        customCategory = ""
                                        if (selectedIdx == index) {
                                            onSelectCategory(cat)
                                        } else {
                                            selectedIdx = index
                                            wheelRotationDeg = -index * stepAngleDeg
                                        }
                                    }
                                    .testTag("wheel_category_node_$catTag")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = categoryIcon(cat),
                                        contentDescription = cat,
                                        tint = if (isNodeSelected) Color.White else nodeAccent,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }
                        }

                        // Center Hub of the Circular Wheel: Displays Active Category & Apply Action
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 8.dp,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(118.dp)
                                .clip(CircleShape)
                                .border(2.dp, activeAccent.copy(alpha = 0.55f), CircleShape)
                                .clickable {
                                    performHaptic()
                                    val target = customCategory.trim().takeIf {
                                        it.isNotBlank() && validateCategoryName(it) == null
                                    } ?: selectedCat
                                    onSelectCategory(target)
                                }
                                .testTag("circular_wheel_center_hub")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                SquircleIconBadge(
                                    icon = categoryIcon(selectedCat),
                                    tint = activeAccent,
                                    containerColor = activeAccent.copy(alpha = 0.16f),
                                    size = 36.dp,
                                    iconSize = 19.dp,
                                    contentDescription = selectedCat
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedCat,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.testTag("circular_wheel_active_category_label")
                                )
                                Text(
                                    text = "Tap to Apply",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = activeAccent
                                )
                            }
                        }
                    }

                    // Primary Apply Selected Wheel Category CTA Button
                    Button(
                        onClick = {
                            performHaptic()
                            val target = customCategory.trim().takeIf {
                                it.isNotBlank() && validateCategoryName(it) == null
                            } ?: selectedCat
                            onSelectCategory(target)
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = categoryAccentColor(selectedCat)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("circular_wheel_apply_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Set Category to ${customCategory.trim().ifBlank { selectedCat }}",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val customCatErr = if (customCategory.isNotEmpty()) validateCategoryName(customCategory) else null
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it.take(40) },
                        label = { Text("Or Type Custom Category") },
                        isError = customCatErr != null,
                        supportingText = customCatErr?.let { err -> { Text(err) } },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wheel_custom_category_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
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
                                        performHaptic()
                                        onSelectCategory(customCategory.trim())
                                    }
                                },
                                shape = RoundedCornerShape(50)
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
        AnimatedModalSheetContainer {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    M3ModalHeroHeader(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = "Choose your paper & ink mode"
                    )

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
    val performHaptic = rememberAppHapticPerformer()
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
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    M3ModalHeroHeader(
                        icon = categoryIcon(category),
                        title = if ((initialProgress?.limit ?: 0.0) > 0.0) {
                            "Edit Category Budget"
                        } else {
                            "Set Category Budget"
                        },
                        iconTint = categoryAccentColor(category)
                    )

                    ReceiptDashedDividerLine()

                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoryOptions.forEach { cat ->
                            FilterChip(
                                selected = category.equals(cat, ignoreCase = true) && customCategory.isBlank(),
                                onClick = {
                                    performHaptic()
                                    category = cat
                                    customCategory = ""
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = categoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(50),
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_budget_custom_name_input")
                    )

                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = sanitizeDecimalInput(it) },
                        label = { Text("Monthly Limit ($currencySymbol)") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = ReceiptMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        isError = limitError != null,
                        supportingText = limitError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
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
                        shape = RoundedCornerShape(16.dp),
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
                            M3CheckmarkSwitch(checked = isShared, onCheckedChange = { isShared = it })
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
                                    performHaptic()
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    attemptedSave = true
                                    val lErr = validatePositiveAmount(limitText)
                                    val cErr = if (customCategory.isNotBlank()) validateCategoryName(customCategory) else null
                                    val pct = alertPctText.toIntOrNull()
                                    val pErr = if (pct == null || pct !in 10..100) "Invalid" else null
                                    if (lErr == null && cErr == null && pErr == null) {
                                        performHaptic()
                                        val limit = limitText.toDoubleOrNull() ?: 0.0
                                        val targetCat = customCategory.trim().ifBlank { category }
                                        onSave(targetCat, limit, pct ?: 80, isShared)
                                    }
                                },
                                shape = RoundedCornerShape(50),
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
fun AddCustomCategoryDialog(
    hasHousehold: Boolean,
    defaultShared: Boolean,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (categoryName: String, emoji: String, optionalMonthlyLimit: Double?, alertThresholdPercent: Int, isShared: Boolean) -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    var categoryName by remember { mutableStateOf("") }
    var emojiInput by remember { mutableStateOf("✨") }
    var optionalLimitText by remember { mutableStateOf("") }
    var isShared by remember { mutableStateOf(hasHousehold && defaultShared) }
    var attemptedSave by remember { mutableStateOf(false) }

    val quickPickEmojis = remember {
        listOf("✨", "🐾", "🏋️", "📚", "☕", "🎮", "🎁", "✈️", "🚗", "🐶", "💡", "🎵", "🧴", "🧋", "🏡", "🎓")
    }

    val categoryNameError = if (attemptedSave || categoryName.isNotEmpty()) {
        validateCategoryName(categoryName)
    } else {
        null
    }
    val optionalLimitError = if (optionalLimitText.isNotBlank()) {
        validateNonNegativeAmount(optionalLimitText)
    } else {
        null
    }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedModalSheetContainer {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding)
                        .verticalScroll(rememberScrollState())
                        .testTag("add_custom_category_dialog"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val accent = categoryAccentColor(categoryName.ifBlank { "Custom" })
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SquircleIconBadge(
                            icon = categoryIcon(categoryName.ifBlank { "Custom" }),
                            tint = accent,
                            containerColor = accent.copy(alpha = 0.16f),
                            size = 50.dp,
                            iconSize = 24.dp,
                            emoji = emojiInput.trim().takeIf { it.isNotBlank() },
                            contentDescription = categoryName.ifBlank { "Custom Category" }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "New Custom Category",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pick an emoji icon & optional monthly budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    ReceiptDashedDividerLine()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = emojiInput,
                            onValueChange = { raw ->
                                emojiInput = LocalAccountSessionManager.extractFirstEmojiCluster(raw)
                            },
                            label = { Text("Emoji") },
                            placeholder = { Text("✨") },
                            textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .width(84.dp)
                                .testTag("custom_category_emoji_input")
                        )
                        OutlinedTextField(
                            value = categoryName,
                            onValueChange = { categoryName = it.take(40) },
                            label = { Text("Category Name") },
                            placeholder = { Text("e.g. Pet Care, Books, Fitness") },
                            isError = categoryNameError != null,
                            supportingText = categoryNameError?.let { err -> { Text(err) } },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_category_name_input")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        quickPickEmojis.forEachIndexed { idx, presetEmoji ->
                            val isChosen = emojiInput == presetEmoji
                            Surface(
                                shape = CircleShape,
                                color = if (isChosen) accent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = if (isChosen) BorderStroke(1.5.dp, accent) else null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        performHaptic()
                                        emojiInput = presetEmoji
                                    }
                                    .testTag("custom_category_emoji_preset_$idx")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = presetEmoji,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = optionalLimitText,
                        onValueChange = { optionalLimitText = sanitizeDecimalInput(it) },
                        label = { Text("Monthly Budget ($currencySymbol, Optional)") },
                        placeholder = { Text("Leave blank for unassigned") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = ReceiptMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        isError = optionalLimitError != null,
                        supportingText = optionalLimitError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_category_optional_budget_input")
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
                            M3CheckmarkSwitch(
                                checked = isShared,
                                onCheckedChange = { isShared = it },
                                modifier = Modifier.testTag("custom_category_shared_switch")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("cancel_custom_category_button")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                attemptedSave = true
                                val nameErr = validateCategoryName(categoryName)
                                val limitErr = if (optionalLimitText.isNotBlank()) {
                                    validateNonNegativeAmount(optionalLimitText)
                                } else {
                                    null
                                }
                                if (nameErr == null && limitErr == null) {
                                    performHaptic()
                                    val parsedLimit = optionalLimitText.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
                                    onSave(
                                        categoryName.trim(),
                                        emojiInput.trim().ifBlank { "✨" },
                                        parsedLimit,
                                        80,
                                        hasHousehold && isShared
                                    )
                                }
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("save_custom_category_button")
                        ) {
                            Text("Add Category")
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
    val performHaptic = rememberAppHapticPerformer()
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
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    M3ModalHeroHeader(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Monthly Budgets",
                        subtitle = "Configure overall monthly spending limits"
                    )

                    ReceiptDashedDividerLine()

                    if (householdBudget != null) {
                        OutlinedTextField(
                            value = householdText,
                            onValueChange = { householdText = sanitizeDecimalInput(it) },
                            label = { Text("Household Monthly Limit ($currency)") },
                            placeholder = { Text("Leave blank for unassigned") },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = ReceiptMonoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            isError = householdError != null,
                            supportingText = householdError?.let { err -> { Text(err) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
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
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = ReceiptMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        isError = personalError != null,
                        supportingText = personalError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overall_currency_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                attemptedSave = true
                                val hErr = if (householdBudget != null) validateNonNegativeAmount(householdText) else null
                                val pErr = validateNonNegativeAmount(personalText)
                                val cErr = validateCurrencySymbol(currency)
                                if (hErr == null && pErr == null && cErr == null) {
                                    performHaptic()
                                    val pLimit = personalText.trim().toDoubleOrNull() ?: 0.0
                                    val hLimit = if (householdBudget != null) {
                                        householdText.trim().toDoubleOrNull() ?: 0.0
                                    } else {
                                        null
                                    }
                                    onSave(pLimit, hLimit, currency.trim().ifBlank { "₹" })
                                }
                            },
                            shape = RoundedCornerShape(50),
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
    accounts: List<com.example.data.model.FinancialAccount> = emptyList(),
    initialAccountType: String = "BANK",
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        bankName: String,
        accountType: String,
        lastFour: String,
        balance: Double,
        creditLimit: Double,
        linkedBankAccountId: String,
        linkedBankAccountName: String,
        monthlySpendLimit: Double
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onSaveWithPeriod: (
        (
            name: String,
            bankName: String,
            accountType: String,
            lastFour: String,
            balance: Double,
            creditLimit: Double,
            linkedBankAccountId: String,
            linkedBankAccountName: String,
            monthlySpendLimit: Double,
            spendLimitPeriod: String,
            spendLimitStartMillis: Long,
            spendLimitEndMillis: Long
        ) -> Unit
    )? = null,
    onSaveWithBillingCycle: (
        (
            name: String,
            bankName: String,
            accountType: String,
            lastFour: String,
            balance: Double,
            creditLimit: Double,
            linkedBankAccountId: String,
            linkedBankAccountName: String,
            monthlySpendLimit: Double,
            billingCycleDay: Int
        ) -> Unit
    )? = null
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val bankAccounts = remember(accounts) {
        accounts.filter {
            FinancialAccount.normalizeAccountType(it.accountType) == "BANK"
        }
    }
    var accountType by remember(initialAccountType) {
        mutableStateOf(FinancialAccount.normalizeAccountType(initialAccountType))
    }
    var bankName by remember { mutableStateOf("") }
    var selectedLinkedBankId by remember(bankAccounts) { mutableStateOf(bankAccounts.firstOrNull()?.id.orEmpty()) }
    var lastFour by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var creditLimitText by remember { mutableStateOf("") }
    var spendLimitText by remember { mutableStateOf("") }
    var billingCycleDayText by remember { mutableStateOf("1") }
    var attemptedSave by remember { mutableStateOf(false) }

    val selectedLinkedBank = remember(bankAccounts, selectedLinkedBankId) {
        bankAccounts.firstOrNull { it.id == selectedLinkedBankId } ?: bankAccounts.firstOrNull()
    }

    val bankNameError = if (attemptedSave || bankName.isNotEmpty()) {
        if (bankName.trim().length < 2) "Enter a valid bank, card, or wallet name (min 2 chars)." else null
    } else {
        null
    }
    val requiresDigits = accountType == "BANK" || accountType == "CREDIT_CARD" || accountType == "DEBIT_CARD"
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
    val spendLimitError = if (spendLimitText.isNotEmpty()) {
        validateNonNegativeAmount(spendLimitText, maxAllowed = 100_000_000.0)
    } else {
        null
    }
    val linkedBankError = if (accountType == "DEBIT_CARD" && selectedLinkedBank == null) {
        "Add a Bank account first to link a Debit Card."
    } else {
        null
    }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedModalSheetContainer {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    M3ModalHeroHeader(
                        icon = Icons.Default.AccountBalance,
                        title = "Add Account",
                        subtitle = "Track wallet, bank account, linked debit card, or credit card"
                    )

                    ReceiptDashedDividerLine()

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "WALLET" to "Wallet",
                            "BANK" to "Bank",
                            "DEBIT_CARD" to "Debit Card",
                            "CREDIT_CARD" to "Credit Card",
                            "CASH" to "Cash"
                        ).forEach { (type, label) ->
                            val selected = accountType == type
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    performHaptic()
                                    accountType = type
                                    if (type == "DEBIT_CARD" && selectedLinkedBankId.isBlank() && bankAccounts.isNotEmpty()) {
                                        val firstBank = bankAccounts.first()
                                        selectedLinkedBankId = firstBank.id
                                        if (bankName.isBlank()) bankName = firstBank.bankName
                                    }
                                },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else {
                                    null
                                },
                                label = { Text(label) },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("account_type_chip_${type.lowercase()}")
                            )
                        }
                    }

                    if (accountType == "DEBIT_CARD") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Link to Existing Bank Account",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (bankAccounts.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    bankAccounts.forEach { bankAcc ->
                                        val isLinked = selectedLinkedBank?.id == bankAcc.id
                                        val chipLabel = if (bankAcc.lastFourDigits.isNotBlank()) {
                                            "${bankAcc.name} ••${bankAcc.lastFourDigits}"
                                        } else {
                                            bankAcc.name
                                        }
                                        FilterChip(
                                            selected = isLinked,
                                            onClick = {
                                                performHaptic()
                                                selectedLinkedBankId = bankAcc.id
                                                if (bankName.isBlank() || bankAccounts.any { it.bankName.equals(bankName, ignoreCase = true) }) {
                                                    bankName = bankAcc.bankName
                                                }
                                            },
                                            leadingIcon = if (isLinked) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            } else {
                                                null
                                            },
                                            label = { Text(chipLabel) },
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier.testTag("link_bank_chip_${bankAcc.id}")
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("debit_card_requires_bank_notice")
                                ) {
                                    Text(
                                        text = "Add a Bank account first. Debit cards can only be linked to an existing Bank account.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it.take(60) },
                        label = {
                            Text(
                                when (accountType) {
                                    "WALLET" -> "Wallet Provider Name"
                                    "DEBIT_CARD" -> "Card Issuer / Bank Name"
                                    "CREDIT_CARD" -> "Card Bank / Issuer Name"
                                    else -> "Bank / Account Name"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (accountType) {
                                    "WALLET" -> "e.g. Paytm Wallet"
                                    "DEBIT_CARD" -> "e.g. HDFC Millennia Debit"
                                    "CREDIT_CARD" -> "e.g. Axis Neo"
                                    else -> "e.g. HDFC Bank"
                                }
                            )
                        },
                        isError = bankNameError != null,
                        supportingText = bankNameError?.let { err -> { Text(err) } },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_account_name_input")
                    )
                    OutlinedTextField(
                        value = lastFour,
                        onValueChange = { lastFour = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text(if (requiresDigits) "Last 4 Digits" else "Last 4 Digits (Optional)") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = ReceiptMonoFontFamily),
                        isError = lastFourError != null,
                        supportingText = lastFourError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_account_last_four_input")
                    )
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = sanitizeDecimalInput(it, maxIntegerDigits = 10) },
                        label = { Text("Current Balance ($currencySymbol)") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = ReceiptMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        isError = balanceError != null,
                        supportingText = balanceError?.let { err -> { Text(err) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_account_balance_input")
                    )
                    if (accountType == "CREDIT_CARD") {
                        OutlinedTextField(
                            value = creditLimitText,
                            onValueChange = { creditLimitText = sanitizeDecimalInput(it, maxIntegerDigits = 9) },
                            label = { Text("Credit Limit ($currencySymbol)") },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = ReceiptMonoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            isError = creditLimitError != null,
                            supportingText = creditLimitError?.let { err -> { Text(err) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_account_credit_limit_input")
                        )
                    }
                    val parsedCycleDay = billingCycleDayText.trim().toIntOrNull()?.coerceIn(1, 31) ?: 1
                    val cycleRangePreview = remember(accountType, parsedCycleDay) {
                        if (accountType == "CREDIT_CARD") {
                            com.example.ui.viewmodel.AccountLimitPeriodUtils.formatBillingCycleRange(
                                billingCycleDay = parsedCycleDay,
                                cycleOffset = 0,
                                includeYear = false
                            )
                        } else {
                            ""
                        }
                    }
                    OutlinedTextField(
                        value = spendLimitText,
                        onValueChange = { spendLimitText = sanitizeDecimalInput(it, maxIntegerDigits = 9) },
                        label = {
                            Text(
                                if (accountType == "CREDIT_CARD") {
                                    "Billing Cycle Spend Limit ($currencySymbol, Optional)"
                                } else {
                                    "Spend Limit ($currencySymbol, Optional)"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                if (accountType == "CREDIT_CARD") {
                                    "Blank = uses Credit Limit (0 = no limit)"
                                } else {
                                    "Leave blank for no spend limit"
                                }
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = ReceiptMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        isError = spendLimitError != null,
                        supportingText = spendLimitError?.let { err -> { Text(err) } } ?: if (accountType == "CREDIT_CARD") {
                            {
                                Text(
                                    text = "Linked to billing cycle: Day $parsedCycleDay ($cycleRangePreview)",
                                    modifier = Modifier.testTag("add_account_limit_cycle_link")
                                )
                            }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_account_spend_limit_input")
                    )

                    if (accountType == "CREDIT_CARD") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_account_billing_cycle_section"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_account_cycle_limit_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = "Billing Cycle Linked to Limit",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Spend limit resets monthly on Day $parsedCycleDay ($cycleRangePreview)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            com.example.ui.components.BillingCycleCalendarGrid(
                                selectedDay = parsedCycleDay,
                                onSelectDay = { day ->
                                    billingCycleDayText = day.toString()
                                },
                                tagPrefix = "billing_cycle_calendar"
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
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
                                val spLimErr = if (spendLimitText.isNotEmpty()) {
                                    validateNonNegativeAmount(spendLimitText, maxAllowed = 100_000_000.0)
                                } else {
                                    null
                                }
                                val lnkErr = if (accountType == "DEBIT_CARD" && selectedLinkedBank == null) {
                                    "Invalid"
                                } else {
                                    null
                                }

                                if (nameErr == null && digitsErr == null && balErr == null &&
                                    limErr == null && spLimErr == null && lnkErr == null
                                ) {
                                    performHaptic()
                                    val cleanBank = bankName.trim()
                                    val suffix = if (lastFour.isNotBlank()) " ••$lastFour" else ""
                                    val displayName = when (accountType) {
                                        "CREDIT_CARD" -> "$cleanBank Credit Card$suffix"
                                        "DEBIT_CARD" -> if (cleanBank.contains("debit", ignoreCase = true)) {
                                            "$cleanBank$suffix"
                                        } else {
                                            "$cleanBank Debit Card$suffix"
                                        }
                                        else -> "$cleanBank$suffix"
                                    }
                                    val balVal = balanceText.trim().toDoubleOrNull() ?: 0.0
                                    val credVal = if (accountType == "CREDIT_CARD") creditLimitText.trim().toDoubleOrNull() ?: 0.0 else 0.0
                                    val lnkId = if (accountType == "DEBIT_CARD") selectedLinkedBank?.id.orEmpty() else ""
                                    val lnkName = if (accountType == "DEBIT_CARD") selectedLinkedBank?.name.orEmpty() else ""
                                    val spendLimVal = spendLimitText.trim().toDoubleOrNull()
                                        ?: if (accountType == "CREDIT_CARD" && credVal > 0.0) credVal else 0.0
                                    val cycleDayVal = if (accountType == "CREDIT_CARD") {
                                        (billingCycleDayText.trim().toIntOrNull() ?: 1).coerceIn(1, 31)
                                    } else {
                                        1
                                    }
                                    when {
                                        onSaveWithBillingCycle != null -> {
                                            onSaveWithBillingCycle(
                                                displayName,
                                                cleanBank,
                                                accountType,
                                                lastFour,
                                                balVal,
                                                credVal,
                                                lnkId,
                                                lnkName,
                                                spendLimVal,
                                                cycleDayVal
                                            )
                                        }
                                        onSaveWithPeriod != null -> {
                                            onSaveWithPeriod(
                                                displayName,
                                                cleanBank,
                                                accountType,
                                                lastFour,
                                                balVal,
                                                credVal,
                                                lnkId,
                                                lnkName,
                                                spendLimVal,
                                                AccountLimitPeriod.MONTHLY.name,
                                                0L,
                                                0L
                                            )
                                        }
                                        else -> {
                                            onSave(
                                                displayName,
                                                cleanBank,
                                                accountType,
                                                lastFour,
                                                balVal,
                                                credVal,
                                                lnkId,
                                                lnkName,
                                                spendLimVal
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = linkedBankError == null,
                            shape = RoundedCornerShape(50),
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
 * - Places compact M3 Expressive controls/telemetry at the top and expands the downward-scanning
 *   "Transaction Being Read" animation (`sms_transaction_reading_animation`) ONLY while actively scanning.
 */
@Composable
fun FirstTimeSmsSyncScreen(
    syncState: SmsSyncProgressState,
    onStartSyncClick: () -> Unit,
    onContinueToAppClick: () -> Unit,
    onSkipClick: () -> Unit,
    currencySymbol: String = "₹",
    selectedPeriodOption: SmsSyncPeriodOption = SmsSyncPeriodOption.ALL_TIME,
    customDateRange: Pair<Long, Long>? = null,
    onSelectPeriodOption: (SmsSyncPeriodOption) -> Unit = {},
    onSelectCustomDateRange: (Long, Long) -> Unit = { _, _ -> },
    permissionWarningMessage: String? = null,
    onOpenAppSettingsClick: () -> Unit = {},
    isFromSettings: Boolean = false,
    onSyncAnotherPeriodClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val dimens = LocalAppDimens.current
    val density = LocalDensity.current
    val performHaptic = rememberAppHapticPerformer()
    val hasStartedOrFinished = syncState.isSyncing || syncState.isCompleted
    var topControlsHeightPx by remember { mutableIntStateOf(0) }
    var showCustomRangePicker by remember { mutableStateOf(false) }
    var backPressBlockedWarning by remember { mutableStateOf(false) }
    val initialIgnoringBatteryOpt = remember(context) {
        runCatching {
            val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) == true
        }.getOrDefault(false)
    }
    var backgroundSyncAllowed by remember { mutableStateOf(initialIgnoringBatteryOpt) }

    androidx.activity.compose.BackHandler(enabled = syncState.isSyncing) {
        performHaptic()
        backPressBlockedWarning = true
    }

    if (showCustomRangePicker) {
        CustomDateRangePickerModal(
            initialStartMillis = customDateRange?.first,
            initialEndMillis = customDateRange?.second,
            onDismiss = { showCustomRangePicker = false },
            onApplyRange = { startMillis, endMillis ->
                onSelectCustomDateRange(startMillis, endMillis)
                showCustomRangePicker = false
            }
        )
    }

    com.example.ui.components.PredictiveBackSurface(
        enabled = isFromSettings && !syncState.isSyncing,
        onBack = {
            performHaptic()
            onSkipClick()
        },
        modifier = Modifier.fillMaxSize()
    ) {
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

            val contentScrollState = rememberScrollState()
            LaunchedEffect(hasStartedOrFinished) {
                if (hasStartedOrFinished) {
                    contentScrollState.scrollTo(0)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SquircleIconBadge(
                        icon = if (syncState.isCompleted) Icons.Default.Check else Icons.Default.Sync,
                        tint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        size = 54.dp,
                        iconSize = 26.dp
                    )

                    Text(
                        text = when {
                            syncState.isCompleted -> "SMS Sync Complete"
                            syncState.isSyncing -> "Syncing Your SMS Inbox..."
                            else -> "Sync Bank & UPI SMS"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    // Compact status & batch progress pill (zero thread counts exposed in UI)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 6.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "SMS Receipt Scanner",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (hasStartedOrFinished) {
                                    "Batch ${syncState.processedBatchesCount}/${syncState.totalBatchesCount.coerceAtLeast(1)}  •  ${selectedPeriodOption.label}"
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

                    if (!permissionWarningMessage.isNullOrBlank() && !syncState.isSyncing) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sms_permission_warning_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = permissionWarningMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            performHaptic()
                                            onOpenAppSettingsClick()
                                        },
                                        modifier = Modifier.testTag("open_sms_app_settings_button")
                                    ) {
                                        Text("Open App Settings")
                                    }
                                }
                            }
                        }
                    }

                    if (!hasStartedOrFinished) {
                        ReceiptTicketCard(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimens.cardPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "On-Device Thermal Receipt Indexing",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Automatically converts Bank, Card, and UPI SMS alerts into clean paper receipts with merchant names, categories, and balances.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                ReceiptDashedDividerLine()

                                // Material 3 Expressive SMS Fetch Period Slider
                                SmsSyncPeriodExpressiveSlider(
                                    selectedPeriod = selectedPeriodOption,
                                    customRange = customDateRange,
                                    onSelectPeriod = onSelectPeriodOption,
                                    onOpenCustomRangePicker = { showCustomRangePicker = true },
                                    onUpdateCustomRange = onSelectCustomDateRange,
                                    containerTag = "sms_sync_period_selector",
                                    selectedLabelTag = "sms_sync_selected_period_label",
                                    sliderTag = "sms_period_slider",
                                    chipTagPrefix = "sms_period_chip"
                                )
                            }
                        }
                    }

                    if (hasStartedOrFinished) {
                        ReceiptTicketCard(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = dimens.cardPadding,
                                        vertical = 12.dp
                                    ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (syncState.isSyncing) Icons.Default.Message else Icons.Default.Check,
                                            contentDescription = if (syncState.isSyncing) "Reading SMS Messages" else "Completed",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (syncState.isSyncing) "Reading SMS Messages" else "Completed",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    val isRobolectric = remember { Build.FINGERPRINT.equals("robolectric", ignoreCase = true) }
                                    val animatedProgress by animateFloatAsState(
                                        targetValue = syncState.progressFraction.coerceIn(0f, 1f),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "sms_sync_smooth_progress_fraction"
                                    )
                                    val displayFraction = if (isRobolectric) {
                                        syncState.progressFraction.coerceIn(0f, 1f)
                                    } else {
                                        animatedProgress
                                    }
                                    val pct = (displayFraction * 100f).roundToInt().coerceIn(0, 100)
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.testTag("sms_sync_percentage_text")
                                    )
                                }

                                val isRobolectricBar = remember { Build.FINGERPRINT.equals("robolectric", ignoreCase = true) }
                                val smoothBarProgress by animateFloatAsState(
                                    targetValue = syncState.progressFraction.coerceIn(0f, 1f),
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "sms_sync_smooth_progress_bar"
                                )
                                LinearProgressIndicator(
                                    progress = { if (isRobolectricBar) syncState.progressFraction else smoothBarProgress },
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .testTag("sms_sync_progress_bar")
                                )

                                if (syncState.statusMessage.isNotBlank()) {
                                    Text(
                                        text = syncState.statusMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("sms_sync_status_message")
                                    )
                                }

                                ReceiptDashedDividerLine()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "Read",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${syncState.readSmsCount}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.testTag("sms_sync_read_count")
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Pending",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${syncState.remainingSmsCount}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.testTag("sms_sync_remaining_count")
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Total",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${syncState.totalSmsCount}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.testTag("sms_sync_total_count")
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 5.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "${syncState.savedTransactionsCount}/${syncState.matchedTransactionsCount}",
                                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = ReceiptMonoFontFamily),
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

                    // Stay-in-App / Allow Background Sync Guidance Notice
                    if (!syncState.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (backPressBlockedWarning) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sms_sync_stay_open_notice")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (backgroundSyncAllowed) Icons.Default.Check else Icons.Default.Sync,
                                            contentDescription = "Sync Background Guidance",
                                            tint = if (backPressBlockedWarning) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (backPressBlockedWarning) {
                                                "Do Not Go Back or Minimize While Scanning"
                                            } else if (backgroundSyncAllowed) {
                                                "Background Sync Enabled • Keep App Active"
                                            } else {
                                                "Please Do Not Go Back or Minimize"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (backPressBlockedWarning) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            }
                                        )
                                    }
                                    FilterChip(
                                        selected = backgroundSyncAllowed,
                                        onClick = {
                                            performHaptic()
                                            backgroundSyncAllowed = true
                                            backPressBlockedWarning = false
                                            runCatching {
                                                val intent = android.content.Intent(
                                                    android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                                                ).apply {
                                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = if (backgroundSyncAllowed) "Background Allowed" else "Allow Background",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.testTag("sms_sync_allow_background_button")
                                    )
                                }
                                Text(
                                    text = if (backgroundSyncAllowed) {
                                        "Background sync permission requested. If your device restricts background apps, please stay on this screen and do not go back or minimize until scanning finishes."
                                    } else {
                                        "Allow OneFamily to run in the background, or stay on this screen and do not press Back or minimize while SMS scanning is in progress."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (backPressBlockedWarning) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.88f)
                                    }
                                )
                            }
                        }
                    }

                    if (!hasStartedOrFinished) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    performHaptic()
                                    onStartSyncClick()
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("start_first_time_sms_sync_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Start SMS Sync Now",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start SMS Sync Now",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            TextButton(
                                onClick = {
                                    performHaptic()
                                    onSkipClick()
                                },
                                modifier = Modifier.testTag("skip_first_time_sms_sync_button")
                            ) {
                                Text(if (isFromSettings) "Back" else "Skip for Now")
                            }
                        }
                    } else if (syncState.isCompleted && !syncState.isSyncing) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    performHaptic()
                                    onContinueToAppClick()
                                },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("finish_sms_sync_continue_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = if (isFromSettings) "Back to Settings" else "Continue to Home",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isFromSettings) "Back to Settings" else "Continue to Home",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isFromSettings && onSyncAnotherPeriodClick != null) {
                                FilledTonalButton(
                                    onClick = {
                                        performHaptic()
                                        onSyncAnotherPeriodClick()
                                    },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("sms_sync_another_period_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sync Another Period",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else if (syncState.isSyncing) {
                        TextButton(
                            onClick = {
                                performHaptic()
                                onSkipClick()
                            },
                            modifier = Modifier.testTag("skip_during_sms_sync_button")
                        ) {
                            Text(if (isFromSettings) "Cancel" else "Skip & Enter App")
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
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
            stiffness = Spring.StiffnessLow
        ),
        label = "sms_scan_beam_deterministic_phase"
    )

    val animatedScanPhase = if (!isRobolectric && !syncState.isCompleted) {
        val infiniteTransition = rememberInfiniteTransition(label = "sms_downward_scanner_transition")
        val sweepingDownPhase by infiniteTransition.animateFloat(
            initialValue = 0.08f,
            targetValue = 0.92f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = if (syncState.isSyncing) 3800 else 4800,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
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
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = primaryColor.copy(alpha = 0.32f),
                shape = RoundedCornerShape(28.dp)
            )
            .testTag("sms_transaction_reading_animation")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimens.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Transaction Being Read Ticker Pill (at top of scanner card for immediate visibility)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sms_live_transaction_ticker")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val catAccent = categoryAccentColor(activeCategory)
                        SquircleIconBadge(
                            icon = categoryIcon(activeCategory),
                            tint = catAccent,
                            containerColor = catAccent.copy(alpha = 0.16f),
                            size = 40.dp,
                            iconSize = 20.dp,
                            contentDescription = activeCategory
                        )
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
                                text = "$activeBank  •  $activeCategory",
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
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = ReceiptMonoFontFamily),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = if (syncState.isCompleted) Icons.Default.Check else Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    syncState.recentReadTransactions.take(4).forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 5.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = categoryIcon(item.category),
                                    contentDescription = item.category,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${item.merchant.take(12)} • ${formatCurrency(item.amount, currencySymbol)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = ReceiptMonoFontFamily),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Large Full-Space Downward Laser & SMS Receipt Scanner Viewport
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

                    val cardW = (w * 0.80f).coerceAtMost(380.dp.toPx())
                    val cardH = (h * 0.86f).coerceAtLeast(80.dp.toPx())
                    val cardLeft = centerX - cardW / 2f
                    val cardTop = centerY - cardH / 2f
                    val cardRight = cardLeft + cardW
                    val cardBottom = cardTop + cardH
                    val cornerRad = 18.dp.toPx()

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

                    val bracketOffset = 8.dp.toPx()
                    val bracketLen = (minDim * 0.09f).coerceIn(14.dp.toPx(), 28.dp.toPx())
                    val bracketStroke = 3.dp.toPx()
                    val bLeft = (cardLeft - bracketOffset).coerceAtLeast(2.dp.toPx())
                    val bRight = (cardRight + bracketOffset).coerceAtMost(w - 2.dp.toPx())
                    val bTop = (cardTop - bracketOffset).coerceAtLeast(2.dp.toPx())
                    val bBottom = (cardBottom + bracketOffset).coerceAtMost(h - 2.dp.toPx())

                    drawLine(primaryColor, Offset(bLeft, bTop), Offset(bLeft + bracketLen, bTop), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bLeft, bTop), Offset(bLeft, bTop + bracketLen), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bRight, bTop), Offset(bRight - bracketLen, bTop), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bRight, bTop), Offset(bRight, bTop + bracketLen), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bLeft, bBottom), Offset(bLeft + bracketLen, bBottom), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bLeft, bBottom), Offset(bLeft, bBottom - bracketLen), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bRight, bBottom), Offset(bRight - bracketLen, bBottom), bracketStroke, StrokeCap.Round)
                    drawLine(primaryColor, Offset(bRight, bBottom), Offset(bRight, bBottom - bracketLen), bracketStroke, StrokeCap.Round)

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

                Box(
                    modifier = Modifier
                        .size(62.dp)
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
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private val DUPLICATE_LINK_STOP_WORDS = setOf(
    "unknown", "expense", "income", "transaction", "transfer", "payment", "payments",
    "upi", "bank", "card", "wallet", "cash", "pay", "online", "store", "shop", "mart",
    "india", "private", "limited", "pvt", "ltd", "bill", "recharge", "merchant", "pos"
)

private fun normalizeMerchantForDuplicateLink(rawTitle: String): String =
    rawTitle.trim()
        .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase(Locale.US)

internal fun isSameMerchantForDuplicateLink(
    firstTitle: String,
    secondTitle: String
): Boolean {
    val normA = normalizeMerchantForDuplicateLink(firstTitle)
    val normB = normalizeMerchantForDuplicateLink(secondTitle)
    if (normA.isBlank() || normB.isBlank()) return false
    if (normA in DUPLICATE_LINK_STOP_WORDS || normB in DUPLICATE_LINK_STOP_WORDS) return false
    if (normA == normB) return true
    if (normA.length >= 3 && normB.startsWith("$normA ")) return true
    if (normB.length >= 3 && normA.startsWith("$normB ")) return true
    val firstTokenA = normA.substringBefore(' ')
    val firstTokenB = normB.substringBefore(' ')
    return firstTokenA.length >= 4 &&
        firstTokenA == firstTokenB &&
        firstTokenA !in DUPLICATE_LINK_STOP_WORDS
}

/**
 * Ranks duplicate/refund candidate transactions for [sourceTransaction] in strict priority:
 * 1. Latest transaction from the same merchant first.
 * 2. Transactions of that same denomination/amount from the same merchant (newest first).
 * 3. All remaining recent transactions ordered by newest date/time first.
 */
internal fun rankDuplicateOrRefundCandidates(
    sourceTransaction: ExpenseTransaction,
    candidates: List<ExpenseTransaction>
): List<ExpenseTransaction> {
    val eligible = candidates.filter { it.id != sourceTransaction.id }
    if (eligible.isEmpty()) return emptyList()

    val sameMerchantList = eligible.filter {
        isSameMerchantForDuplicateLink(it.title, sourceTransaction.title)
    }
    val latestSameMerchantTx = sameMerchantList.maxWithOrNull(
        compareBy<ExpenseTransaction> { it.transactionDateMillis }
            .thenBy { kotlin.math.abs(it.amount - sourceTransaction.amount) < 0.01 }
            .thenBy { -kotlin.math.abs(it.amount - sourceTransaction.amount) }
    )
    val latestSameMerchantId = latestSameMerchantTx?.id

    return eligible.sortedWith(
        compareBy<ExpenseTransaction> { tx ->
            val isSameMerchant = isSameMerchantForDuplicateLink(tx.title, sourceTransaction.title)
            val isSameAmount = kotlin.math.abs(tx.amount - sourceTransaction.amount) < 0.01
            when {
                tx.id == latestSameMerchantId -> 0
                isSameMerchant && isSameAmount -> 1
                else -> 2
            }
        }.thenByDescending { it.transactionDateMillis }
            .thenBy { kotlin.math.abs(it.amount - sourceTransaction.amount) }
    )
}

/**
 * Link Duplicate / Refund SMS & Transaction Dialog (`LinkDuplicateTransactionDialog`):
 * - Displays BOTH full untruncated Original SMS and Selected Target SMS on top in monospace receipt font.
 * - Prioritizes: (1) Latest same-merchant tx, (2) Same denomination amount from same merchant, (3) All recent txs.
 * - Candidate rows below show ONLY Merchant, DateTime, Account, and Amount in a connected M3 card stack.
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
    val performHaptic = rememberAppHapticPerformer()
    var searchQuery by remember { mutableStateOf("") }
    var showAllRecent by remember { mutableStateOf(false) }

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
        rankDuplicateOrRefundCandidates(
            sourceTransaction = sourceTransaction,
            candidates = filtered
        )
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            ModalSheetDragHandle()
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                SquircleIconBadge(
                                    icon = Icons.Default.Link,
                                    tint = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Link Duplicate / Refund",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Same merchant latest, matching amount, then recent",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            CircularTonalIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Close",
                                onClick = onDismiss
                            )
                        }

                        ReceiptDashedDividerLine()

                        // Top Raw SMS Verification Panel: Full Original SMS + Full Selected Target SMS
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .testTag("link_duplicate_sms_verification_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Message,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
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
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("link_duplicate_original_sms_box")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val primaryOrigSms = sourceSmsRecords.firstOrNull()
                                        val origHeaderMeta = if (primaryOrigSms != null && primaryOrigSms.sender.isNotBlank()) {
                                            "Original SMS • ${primaryOrigSms.sender} (${formatExactDateTime(primaryOrigSms.timestamp)})"
                                        } else {
                                            "Original Transaction • ${formatExactDateTime(sourceTransaction.transactionDateMillis)}"
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
                                            SmsInboxReader.stripSyntheticAutoParsedNote(sourceTransaction.notes)
                                                .ifBlank { "Manual transaction (no raw SMS attached)" }
                                        }
                                        Text(
                                            text = fullOriginalSmsText,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = ReceiptMonoFontFamily),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // 2. Full Untruncated Selected Target SMS Box
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
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("link_duplicate_selected_sms_box")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (activeTarget != null) {
                                                val primarySelSms = activeTargetSms.firstOrNull()
                                                val selHeaderMeta = if (primarySelSms != null && primarySelSms.sender.isNotBlank()) {
                                                    "Selected SMS • ${primarySelSms.sender} (${formatExactDateTime(primarySelSms.timestamp)})"
                                                } else {
                                                    "Selected Transaction • ${formatExactDateTime(activeTarget.transactionDateMillis)}"
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
                                                    SmsInboxReader.stripSyntheticAutoParsedNote(activeTarget.notes)
                                                        .ifBlank { "Manual transaction (no raw SMS attached)" }
                                                }
                                                Text(
                                                    text = fullSelectedSmsText,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = ReceiptMonoFontFamily),
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
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("link_source_summary_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
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
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
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
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("link_duplicate_search_input")
                        )

                        if (candidates.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No other transactions found to link with.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Select Target Transaction:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )

                            val visibleCandidates = if (showAllRecent || searchQuery.isNotBlank()) {
                                candidates.take(60)
                            } else {
                                candidates.take(20)
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                visibleCandidates.forEachIndexed { idx, candidate ->
                                    val isSelected = candidate.id == selectedTargetId
                                    val stackShape = groupedCardShape(index = idx, count = visibleCandidates.size, outerCorner = 20.dp, innerCorner = 6.dp)

                                    Surface(
                                        shape = stackShape,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(stackShape)
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = stackShape
                                            )
                                            .clickable {
                                                performHaptic()
                                                selectedTargetId = candidate.id
                                            }
                                            .testTag("link_candidate_row_${candidate.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = ReceiptMonoFontFamily),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            if (!showAllRecent && searchQuery.isBlank() && candidates.size > visibleCandidates.size) {
                                OutlinedButton(
                                    onClick = {
                                        performHaptic()
                                        showAllRecent = true
                                    },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("link_show_all_recent_button")
                                ) {
                                    Text("Show All Recent (${candidates.size})")
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    M3CheckmarkSwitch(
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val target = selectedTarget ?: return@Button
                                    performHaptic()
                                    if (keepTargetAsPrimary) {
                                        onConfirmLink(target, sourceTransaction)
                                    } else {
                                        onConfirmLink(sourceTransaction, target)
                                    }
                                },
                                enabled = selectedTarget != null,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("confirm_link_duplicate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
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
 * - Inspired by Screenshot 1 centered M3 28dp modal dialog with top hero icon, centered title,
 *   tonal preset chips with leading icons, summary card, and right-aligned stadium actions.
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
    val performHaptic = rememberAppHapticPerformer()
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
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimens.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        M3ModalHeroHeader(
                            icon = Icons.Default.Download,
                            title = "Export Transactions (CSV)",
                            subtitle = "Select financial year, calendar year, or date range"
                        )

                        ReceiptDashedDividerLine()

                        Text(
                            text = "Period / Date Range",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            exportPresets.forEach { preset ->
                                val isSel = selectedPreset == preset
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        performHaptic()
                                        if (preset == DateRangePreset.CUSTOM) {
                                            selectedPreset = DateRangePreset.CUSTOM
                                            showCustomPicker = true
                                        } else {
                                            selectedPreset = preset
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when {
                                                preset == DateRangePreset.CUSTOM -> Icons.Default.CalendarMonth
                                                isSel -> Icons.Default.Check
                                                else -> Icons.Default.Tune
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
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
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier.testTag("export_preset_chip_${preset.name.lowercase()}")
                                )
                            }
                        }

                        if (uiState.householdFeatureEnabled && uiState.household != null) {
                            Text(
                                text = "Transactions Scope",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CsvExportScope.entries.forEach { scopeOption ->
                                    FilterChip(
                                        selected = exportScope == scopeOption,
                                        onClick = {
                                            performHaptic()
                                            exportScope = scopeOption
                                        },
                                        label = { Text(scopeOption.label, style = MaterialTheme.typography.labelSmall) },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.testTag("export_scope_chip_${scopeOption.name.lowercase()}")
                                    )
                                }
                            }
                        }

                        // Live Export Summary Card
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = ReceiptMonoFontFamily),
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
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Income: ${formatCurrency(totalIncome, currency)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = ReceiptMonoFontFamily),
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        performHaptic()
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share")
                                }

                                Button(
                                    onClick = {
                                        performHaptic()
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
                                        modifier = Modifier.size(16.dp)
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

/**
 * Prompts the user after updating a merchant's category to optionally apply that category
 * to older transactions from the same merchant, with individual tick/untick checkboxes.
 */
@Composable
fun MerchantCategoryBatchUpdateDialog(
    prompt: MerchantCategoryUpdatePrompt,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (selectedIds: Set<String>) -> Unit
) {
    val dimens = LocalAppDimens.current
    val performHaptic = rememberAppHapticPerformer()
    val customEmojis = rememberCustomCategoryEmojis()
    var selectedIds by remember(prompt) {
        mutableStateOf(prompt.matchingTransactions.map { it.id }.toSet())
    }
    val allSelected = selectedIds.size == prompt.matchingTransactions.size && prompt.matchingTransactions.isNotEmpty()
    val targetAccent = categoryAccentColor(prompt.newCategory)

    Dialog(onDismissRequest = onDismiss) {
        AnimatedModalSheetContainer {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("merchant_batch_category_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    M3ModalHeroHeader(
                        icon = Icons.Default.DoneAll,
                        title = "Update Older Receipts?",
                        subtitle = "Move older \"${prompt.merchantTitle}\" transactions to ${prompt.newCategory}",
                        iconTint = targetAccent
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${selectedIds.size} of ${prompt.matchingTransactions.size} selected",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                                    .testTag("merchant_batch_selected_count")
                            )
                        }

                        TextButton(
                            onClick = {
                                performHaptic()
                                selectedIds = if (allSelected) {
                                    emptySet()
                                } else {
                                    prompt.matchingTransactions.map { it.id }.toSet()
                                }
                            },
                            modifier = Modifier.testTag("merchant_batch_toggle_all_button")
                        ) {
                            Text(if (allSelected) "Clear All" else "Select All")
                        }
                    }

                    ReceiptDashedDividerLine()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .testTag("merchant_batch_tx_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = prompt.matchingTransactions,
                            key = { "${it.visibility}_${it.id}" }
                        ) { tx ->
                            val isChecked = tx.id in selectedIds
                            val oldCategory = ExpenseCategories.normalizeCategory(tx.category)
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isChecked) {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                border = BorderStroke(
                                    width = if (isChecked) 1.dp else 0.5.dp,
                                    color = if (isChecked) {
                                        targetAccent.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        performHaptic()
                                        selectedIds = if (isChecked) {
                                            selectedIds - tx.id
                                        } else {
                                            selectedIds + tx.id
                                        }
                                    }
                                    .testTag("merchant_batch_row_${tx.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            performHaptic()
                                            selectedIds = if (checked) {
                                                selectedIds + tx.id
                                            } else {
                                                selectedIds - tx.id
                                            }
                                        },
                                        modifier = Modifier.testTag("merchant_batch_checkbox_${tx.id}")
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(categoryAccentColor(oldCategory).copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CategoryBadgeGlyph(
                                            category = oldCategory,
                                            tint = categoryAccentColor(oldCategory),
                                            iconSize = 17.dp,
                                            customEmoji = customEmojis[oldCategory.lowercase()],
                                            contentDescription = null
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = tx.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${formatExactDate(tx.transactionDateMillis)} • $oldCategory → ${prompt.newCategory}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = formatCurrency(tx.amount, currencySymbol),
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = ReceiptMonoFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                performHaptic()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("merchant_batch_skip_button")
                        ) {
                            Text("Only This One")
                        }
                        Button(
                            onClick = {
                                performHaptic()
                                onConfirm(selectedIds)
                            },
                            enabled = selectedIds.isNotEmpty(),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("merchant_batch_confirm_button")
                        ) {
                            Text(
                                text = "Update (${selectedIds.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

