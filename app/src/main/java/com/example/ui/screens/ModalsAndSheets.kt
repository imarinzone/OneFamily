package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseCategories
import com.example.data.model.FinancialAccount
import com.example.data.sms.SmsCandidate
import com.example.ui.components.formatCurrency
import com.example.ui.viewmodel.CategorySpendProgress
import com.ritesh.parser.core.bank.BankParserFactory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    hasHousehold: Boolean,
    defaultShared: Boolean,
    accounts: List<FinancialAccount>,
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
        isShared: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var category by remember { mutableStateOf("Groceries") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var accountName by remember {
        mutableStateOf(accounts.firstOrNull()?.name ?: "HDFC Bank ••4821")
    }
    var tagsInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isShared by remember { mutableStateOf(hasHousehold && defaultShared) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (type == "EXPENSE") "Log Expense" else "Log Income",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Expense vs Income + Shared vs Personal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                // Privacy Selector (Shared Household vs Personal)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isShared) Icons.Default.Groups else Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isShared) "Shared Household Expense" else "Private Personal Expense",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = if (isShared) "Synced with linked family members" else "Visible only to you",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isShared,
                            onCheckedChange = { isShared = it },
                            enabled = hasHousehold,
                            modifier = Modifier.testTag("transaction_shared_switch")
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_amount_input")
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Merchant / Payee (e.g. BigBasket, Swiggy)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_title_input")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseCategories.ALL.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = "Payment Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("UPI", "CARD", "BANK", "WALLET", "CASH").forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = {
                                paymentMode = mode
                                if (mode == "CASH") accountName = "Cash Wallet"
                            },
                            label = { Text(mode) }
                        )
                    }
                }

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account / Card Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Custom #tags (comma or space separated)") },
                    placeholder = { Text("groceries, weekend, family") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_tags_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(
                            title.ifBlank { category },
                            amt,
                            type,
                            category,
                            accountName,
                            paymentMode,
                            tagsInput,
                            notes,
                            isShared
                        )
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CashiroSmsSyncDialog(
    candidates: List<SmsCandidate>,
    hasHousehold: Boolean,
    currencySymbol: String,
    onScanInbox: () -> Unit,
    onLoadSampleBatch: () -> Unit,
    onParseCustomSms: (sender: String, body: String, defaultShared: Boolean) -> Unit,
    onImportCandidate: (SmsCandidate, asShared: Boolean) -> Unit,
    onImportAll: (forceShared: Boolean?) -> Unit,
    onDismissCandidate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customSender by remember { mutableStateOf("AD-HDFCBK-S") }
    var customBody by remember {
        mutableStateOf("Sent Rs.1,250.00\nFrom HDFC Bank A/C *4821\nTo SWIGGY INSTAMART\nOn 05/11/26\nRef 631099887766\nAvl Bal Rs.58,400.00")
    }
    var customDefaultShared by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cashiro Bank SMS Parser",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Powered by com.ritesh.parser.core.bank (HDFC, SBI, ICICI, Axis, Kotak, IDFC, OneCard, Amex & UPI).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onScanInbox,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Scan Device SMS", style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = onLoadSampleBatch,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("load_sample_sms_batch_button")
                    ) {
                        Text("Load 6 Bank SMS", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Live SMS Paste / Tester Box
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Test / Paste Any Bank SMS Alert",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = customSender,
                            onValueChange = { customSender = it },
                            label = { Text("DLT Sender ID (e.g. AD-HDFCBK-S, VM-SBIINB)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customBody,
                            onValueChange = { customBody = it },
                            label = { Text("SMS Message Body") },
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_sms_body_input")
                        )
                        Button(
                            onClick = {
                                onParseCustomSms(customSender, customBody, customDefaultShared)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parse_custom_sms_button")
                        ) {
                            Text("Parse with BankParserFactory")
                        }
                    }
                }

                if (candidates.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parsed Transactions (${candidates.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { onImportAll(null) },
                            modifier = Modifier.testTag("import_all_sms_button")
                        ) {
                            Text("Import All")
                        }
                    }

                    candidates.forEach { cand ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cand.parsed.merchant ?: cand.suggestedCategory,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${cand.suggestedAccountName} • ${cand.suggestedCategory} (${cand.parsed.type.name})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        cand.parsed.balance?.let { bal ->
                                            Text(
                                                text = "Extracted Bal: ${formatCurrency(bal.toDouble(), currencySymbol)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Text(
                                        text = formatCurrency(cand.parsed.amount.toDouble(), currencySymbol),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (hasHousehold) {
                                        Button(
                                            onClick = { onImportCandidate(cand, true) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Shared", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { onImportCandidate(cand, false) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Personal", style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(
                                        onClick = { onDismissCandidate(cand.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Skip", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
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
    var limitText by remember {
        mutableStateOf(
            if ((initialProgress?.limit ?: 0.0) > 0) initialProgress!!.limit.toInt().toString() else "12000"
        )
    }
    var alertPctText by remember {
        mutableStateOf((initialProgress?.alertThresholdPercent ?: 80).toString())
    }
    var isShared by remember { mutableStateOf(hasHousehold && defaultShared) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Category Monthly Budget") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseCategories.EXPENSE_ONLY.forEach { cat ->
                        FilterChip(
                            selected = category.equals(cat, ignoreCase = true),
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Monthly Limit ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = alertPctText,
                    onValueChange = { alertPctText = it },
                    label = { Text("Alert Threshold % (e.g. 80)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasHousehold) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Apply to Shared Family Household",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(checked = isShared, onCheckedChange = { isShared = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    val pct = alertPctText.toIntOrNull() ?: 80
                    if (limit > 0) {
                        onSave(category, limit, pct, isShared)
                    }
                }
            ) {
                Text("Save Limit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditOverallBudgetDialog(
    personalBudget: Double,
    householdBudget: Double?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (personalLimit: Double, householdLimit: Double?, currency: String) -> Unit
) {
    var personalText by remember { mutableStateOf(personalBudget.toInt().toString()) }
    var householdText by remember { mutableStateOf(householdBudget?.toInt()?.toString().orEmpty()) }
    var currency by remember { mutableStateOf(currencySymbol) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Budget Limits") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = personalText,
                    onValueChange = { personalText = it },
                    label = { Text("My Personal Monthly Budget") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (householdBudget != null) {
                    OutlinedTextField(
                        value = householdText,
                        onValueChange = { householdText = it },
                        label = { Text("Shared Household Monthly Budget") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it.take(4) },
                    label = { Text("Currency Symbol (₹, $, €, £)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pLimit = personalText.toDoubleOrNull() ?: personalBudget
                    val hLimit = householdText.toDoubleOrNull()
                    onSave(pLimit, hLimit, currency.ifBlank { "₹" })
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bank Account / Card") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("BANK", "CREDIT_CARD", "UPI", "WALLET", "CASH").forEach { type ->
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
                    label = { Text("Bank / Issuer Name (e.g. HDFC Bank, SBI, ICICI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastFour,
                    onValueChange = { lastFour = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Last 4 Digits (for SMS matching)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Current Balance ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (accountType == "CREDIT_CARD") {
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = it },
                        label = { Text("Available Credit Limit ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
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
                Text("Save Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
