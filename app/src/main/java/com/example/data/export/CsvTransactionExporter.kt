package com.example.data.export

import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.ui.viewmodel.DateRangeFilterUtils
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.YearMonthPeriod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CsvExportScope(val label: String) {
    CURRENT_VIEW("Current Scope"),
    ALL_TRANSACTIONS("Family + Personal")
}

object CsvTransactionExporter {

    private val CSV_COLUMNS = listOf(
        "Date",
        "Time",
        "Merchant",
        "Category",
        "Type",
        "Amount",
        "Currency",
        "Payment Mode",
        "Account",
        "Scope",
        "Member",
        "Reference",
        "Tags"
    )

    fun filterTransactionsForExport(
        transactions: List<ExpenseTransaction>,
        preset: DateRangePreset,
        selectedPeriod: YearMonthPeriod? = YearMonthPeriod.current(),
        customRange: Pair<Long, Long>? = null,
        referenceMillis: Long = System.currentTimeMillis()
    ): List<ExpenseTransaction> {
        return DateRangeFilterUtils.filterTransactionsByPreset(
            transactions = transactions,
            preset = preset,
            selectedPeriod = selectedPeriod,
            customRange = customRange,
            referenceMillis = referenceMillis
        ).sortedByDescending { it.transactionDateMillis }
    }

    fun buildTransactionsCsv(
        transactions: List<ExpenseTransaction>,
        currencySymbol: String = "₹"
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        val sb = StringBuilder()

        sb.append(CSV_COLUMNS.joinToString(",") { escapeCsvField(it) }).append("\n")

        transactions
            .sortedByDescending { it.transactionDateMillis }
            .forEach { tx ->
                val txDate = Date(tx.transactionDateMillis)
                val normalizedCategory = ExpenseCategories.normalizeCategory(tx.category)
                val cleanAmount = if (tx.amount % 1.0 == 0.0) {
                    tx.amount.toLong().toString()
                } else {
                    String.format(Locale.US, "%.2f", tx.amount)
                }
                val scopeLabel = if (tx.visibility.equals("SHARED", ignoreCase = true) && tx.householdId.isNotBlank()) {
                    "Household"
                } else {
                    "Personal"
                }
                val row = listOf(
                    dateFormat.format(txDate),
                    timeFormat.format(txDate),
                    tx.title.ifBlank { normalizedCategory },
                    normalizedCategory,
                    tx.type,
                    cleanAmount,
                    currencySymbol,
                    tx.paymentMode,
                    tx.accountName,
                    scopeLabel,
                    tx.userName.ifBlank { "Me" },
                    tx.referenceNumber,
                    tx.tags.filterNot { it == "sms-auto" || it == "mock-sample" }.joinToString("; ")
                )
                sb.append(row.joinToString(",") { escapeCsvField(it) }).append("\n")
            }

        return sb.toString()
    }

    fun suggestCsvFileName(
        preset: DateRangePreset,
        selectedPeriod: YearMonthPeriod? = YearMonthPeriod.current(),
        customRange: Pair<Long, Long>? = null,
        referenceMillis: Long = System.currentTimeMillis()
    ): String {
        val label = DateRangeFilterUtils.formatPeriodLabel(
            preset = preset,
            selectedPeriod = selectedPeriod,
            customRange = customRange,
            referenceMillis = referenceMillis
        )
        val slug = label
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "all_time" }
        return "onefamily_transactions_$slug.csv"
    }

    internal fun escapeCsvField(value: String): String {
        val cleaned = value.replace("\r\n", " ").replace("\n", " ").replace("\r", " ").trim()
        val needsQuotes = cleaned.contains(',') || cleaned.contains('"')
        return if (needsQuotes) {
            "\"${cleaned.replace("\"", "\"\"")}\""
        } else {
            cleaned
        }
    }
}
