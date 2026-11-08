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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.PersonalBadgeTeal
import com.example.ui.theme.categoryAccentColor
import com.example.ui.viewmodel.CategorySpendProgress

@OptIn(ExperimentalLayoutApi::class)
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
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp)
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
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Action Row: Receipt Stamp + Category Emblem + Close/Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1-Tap Rubber Stamp Toggle for Household vs Personal
                        val stampColor = if (isShared) HouseholdBadgeIndigo else PersonalBadgeTeal
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = stampColor.copy(alpha = 0.14f),
                            modifier = Modifier
                                .border(1.5.dp, stampColor, RoundedCornerShape(8.dp))
                                .clickable(enabled = hasHousehold) { isShared = !isShared }
                                .testTag("transaction_shared_switch")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isShared) Icons.Default.Groups else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = stampColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isShared) "HOUSEHOLD" else "PERSONAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = stampColor
                                )
                            }
                        }

                        // Category Squircle Emblem (No text underneath)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(catAccent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon(category),
                                contentDescription = category,
                                tint = catAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (showDeleteButton && onDelete != null) {
                                IconButton(
                                    onClick = onDelete,
                                    modifier = Modifier
                                        .size(34.dp)
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
                                modifier = Modifier.size(34.dp)
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
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                    ReceiptDashedDividerLine()

                    // Big Receipt Total & Merchant Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Total ($currencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.45f)
                                .testTag(amountTestTag)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Merchant") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
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
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
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
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    ReceiptDashedDividerLine()

                    // Quick Date & Payment Method Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(onClick = { txDateMillis = System.currentTimeMillis() }) {
                            Text("Today", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(onClick = { txDateMillis = System.currentTimeMillis() - 86_400_000L }) {
                            Text("Yesterday", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(onClick = { txDateMillis -= 86_400_000L }) {
                            Text("-1 Day", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(onClick = { txDateMillis += 86_400_000L }) {
                            Text("+1 Day", style = MaterialTheme.typography.labelSmall)
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
                                value = accountName,
                                onValueChange = { accountName = it },
                                label = { Text("Account / Card") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = tagsInput,
                                onValueChange = { tagsInput = it },
                                label = { Text("#Tags") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_tx_tags_input")
                            )
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Note") },
                                maxLines = 2,
                                shape = RoundedCornerShape(14.dp),
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
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") BudgetSafeGreen else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag(saveTestTag)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                ReceiptDashedDividerLine()

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryIcon(cat),
                                    contentDescription = null,
                                    tint = if (isSel) Color.White else accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
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
                    shape = RoundedCornerShape(16.dp),
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
                        Text("Edit")
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
    Dialog(onDismissRequest = onDismiss) {
        ReceiptTicketCard(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Category Limit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ReceiptDashedDividerLine()

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Limit ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = personalText,
                    onValueChange = { personalText = it },
                    label = { Text("Personal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it.take(4) },
                    label = { Text("Currency") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                ReceiptDashedDividerLine()

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { lastFour = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Last 4 Digits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Balance ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
