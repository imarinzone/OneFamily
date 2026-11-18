package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseTransaction
import com.example.ui.theme.categoryAccentColor
import com.example.ui.theme.rememberAppHapticPerformer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private fun positiveMod(value: Int, mod: Int): Int {
    if (mod <= 0) return 0
    val r = value % mod
    return if (r < 0) r + mod else r
}

@Stable
class CategoryWheelController(
    private val pxPerSlot: Float,
    private val performHaptic: () -> Unit,
    private val onCommit: (ExpenseTransaction, String) -> Unit
) {
    var isActive by mutableStateOf(false)
        private set

    var activeTransaction by mutableStateOf<ExpenseTransaction?>(null)
        private set

    var anchorCenterInRoot by mutableStateOf(Offset.Zero)
        private set

    var categories by mutableStateOf<List<String>>(ExpenseCategories.ALL)
        private set

    var customEmojis by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var wheelSlotOffset by mutableFloatStateOf(0f)
        private set

    var selectedIndex by mutableIntStateOf(0)
        private set

    var hasMovedDuringHold by mutableStateOf(false)
        private set

    private var totalDragDistancePx = 0f

    val selectedCategory: String
        get() = categories.getOrElse(selectedIndex.coerceIn(0, categories.lastIndex.coerceAtLeast(0))) {
            activeTransaction?.category ?: "Unknown"
        }

    fun start(
        transaction: ExpenseTransaction,
        anchorInRoot: Offset,
        availableCategories: List<String>,
        emojiMap: Map<String, String>
    ) {
        val isIncomeTx = transaction.type.equals("INCOME", ignoreCase = true) ||
            ExpenseCategories.isIncomeCategory(transaction.category)
        val resolved = (availableCategories + listOf(transaction.category))
            .map { ExpenseCategories.normalizeCategory(it) }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filter { cat ->
                if (isIncomeTx) {
                    ExpenseCategories.isIncomeCategory(cat)
                } else {
                    !ExpenseCategories.isIncomeCategory(cat)
                }
            }
            .ifEmpty {
                if (isIncomeTx) ExpenseCategories.INCOME_CATEGORIES else ExpenseCategories.EXPENSE_ONLY
            }

        val normCurrent = ExpenseCategories.normalizeCategory(transaction.category)
        val initialIdx = resolved.indexOfFirst {
            it.equals(normCurrent, ignoreCase = true)
        }.coerceAtLeast(0)

        activeTransaction = transaction
        anchorCenterInRoot = anchorInRoot
        categories = resolved
        customEmojis = emojiMap
        selectedIndex = initialIdx
        wheelSlotOffset = initialIdx.toFloat()
        totalDragDistancePx = 0f
        hasMovedDuringHold = false
        isActive = true
    }

    fun updateDrag(delta: Offset) {
        if (!isActive || categories.isEmpty()) return
        totalDragDistancePx += hypotDist(delta.x, delta.y)
        if (totalDragDistancePx > 8f) {
            hasMovedDuringHold = true
        }
        // Single-hand thumb drag: sliding thumb up/down (or diagonal) rotates the half-circle wheel
        val effectiveDelta = (-delta.y) + (delta.x * 0.15f)
        wheelSlotOffset += effectiveDelta / pxPerSlot.coerceAtLeast(12f)
        val count = categories.size
        val nextIdx = positiveMod(wheelSlotOffset.roundToInt(), count)
        if (nextIdx != selectedIndex) {
            selectedIndex = nextIdx
            performHaptic()
        }
    }

    fun commitAndClose() {
        val tx = activeTransaction
        val chosen = selectedCategory
        val shouldCommit = isActive && tx != null && (hasMovedDuringHold || !chosen.equals(tx.category, ignoreCase = true))
        isActive = false
        activeTransaction = null
        hasMovedDuringHold = false
        if (shouldCommit) {
            performHaptic()
            onCommit(tx!!, chosen)
        }
    }

    fun dismiss() {
        isActive = false
        activeTransaction = null
        hasMovedDuringHold = false
    }

    private fun hypotDist(dx: Float, dy: Float): Float =
        kotlin.math.sqrt(dx * dx + dy * dy)
}

val LocalCategoryWheelController = compositionLocalOf<CategoryWheelController?> { null }

/**
 * Root gesture-tracking overlay that renders:
 * 1. A faded dark scrim over the entire screen while active.
 * 2. A compact (~184dp) half-circle category scroll wheel anchored directly on top of the
 *    pressed category icon, with its left side smoothly faded out (`BlendMode.DstIn`).
 * Selection commits immediately when the user lifts their finger — a single long-press-and-slide
 * gesture with zero second click needed.
 */
@Composable
fun CategoryWheelOverlayHost(
    availableCategories: List<String>,
    onCommitCategory: (ExpenseTransaction, String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val performHaptic = rememberAppHapticPerformer()
    val density = LocalDensity.current
    val customEmojis = rememberCustomCategoryEmojis()
    val currentCategories by rememberUpdatedState(availableCategories)
    val currentCommit by rememberUpdatedState(onCommitCategory)

    val pxPerSlot = with(density) { 36.dp.toPx() }
    val controller = remember(pxPerSlot) {
        CategoryWheelController(
            pxPerSlot = pxPerSlot,
            performHaptic = performHaptic,
            onCommit = { tx, cat -> currentCommit(tx, cat) }
        )
    }

    var hostSize by remember { mutableStateOf(IntSize.Zero) }

    CompositionLocalProvider(
        LocalCategoryWheelController provides controller
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { hostSize = it.size }
        ) {
            content()

            // Full-screen faded background while wheel is active
            AnimatedVisibility(
                visible = controller.isActive,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(140)),
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(50f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.56f))
                        .testTag("category_wheel_scrim")
                )
            }

            if (controller.isActive) {
                CompactHalfCircleCategoryWheel(
                    controller = controller,
                    customEmojis = customEmojis,
                    hostSize = hostSize,
                    modifier = Modifier.zIndex(51f)
                )
            }
        }
    }
}

@Composable
private fun CompactHalfCircleCategoryWheel(
    controller: CategoryWheelController,
    customEmojis: Map<String, String>,
    hostSize: IntSize,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val wheelDiameterDp = 188.dp
    val orbitRadiusDp = 64.dp
    val nodeSizeDp = 38.dp

    val wheelSizePx = with(density) { wheelDiameterDp.toPx() }
    val halfWheelPx = wheelSizePx / 2f
    val orbitRadiusPx = with(density) { orbitRadiusDp.toPx() }
    val nodeSizePx = with(density) { nodeSizeDp.toPx() }
    val topSafePx = with(density) { 86.dp.toPx() }
    val bottomSafePx = with(density) { 48.dp.toPx() }
    val edgePadPx = with(density) { 10.dp.toPx() }

    val rawAnchor = controller.anchorCenterInRoot
    val maxW = hostSize.width.toFloat().coerceAtLeast(wheelSizePx)
    val maxH = hostSize.height.toFloat().coerceAtLeast(wheelSizePx)

    val centerX = rawAnchor.x.coerceIn(
        minimumValue = (halfWheelPx * 0.34f).coerceAtLeast(edgePadPx),
        maximumValue = (maxW - halfWheelPx - edgePadPx).coerceAtLeast(edgePadPx)
    )
    val centerY = rawAnchor.y.coerceIn(
        minimumValue = (halfWheelPx + topSafePx).coerceAtMost(maxH / 2f),
        maximumValue = (maxH - halfWheelPx - bottomSafePx).coerceAtLeast(halfWheelPx + edgePadPx)
    )

    val topLeftX = (centerX - halfWheelPx).roundToInt()
    val topLeftY = (centerY - halfWheelPx).roundToInt()

    val selectedCat = controller.selectedCategory
    val activeAccent = categoryAccentColor(selectedCat)
    val activeEmoji = customEmojis[selectedCat.trim().lowercase()]
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val wheelBgColor = MaterialTheme.colorScheme.surfaceContainerHigh

    val animatedSlotOffset by animateFloatAsState(
        targetValue = controller.wheelSlotOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "half_circle_wheel_slot_offset"
    )

    val stepAngleDeg = 34f
    val count = controller.categories.size.coerceAtLeast(1)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Active Category Label Pill positioned neatly above the wheel for crisp one-hand visibility
        val pillOffsetX = (centerX - with(density) { 12.dp.toPx() })
            .coerceIn(edgePadPx, (maxW - with(density) { 176.dp.toPx() }).coerceAtLeast(edgePadPx))
            .roundToInt()
        val pillOffsetY = (centerY - halfWheelPx - with(density) { 46.dp.toPx() })
            .coerceAtLeast(with(density) { 28.dp.toPx() })
            .roundToInt()

        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 8.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .offset { IntOffset(pillOffsetX, pillOffsetY) }
                .border(1.5.dp, activeAccent.copy(alpha = 0.7f), RoundedCornerShape(50))
                .testTag("category_wheel_active_label")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadgeGlyph(
                    category = selectedCat,
                    tint = activeAccent,
                    iconSize = 16.dp,
                    customEmoji = activeEmoji
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedCat,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Compact Left-Faded Half-Circle Scroll Wheel Anchored Directly on Selected Category
        Box(
            modifier = Modifier
                .offset { IntOffset(topLeftX, topLeftY) }
                .size(wheelDiameterDp)
                .testTag("circular_category_wheel")
        ) {
            // Half-circle ring + ticks with left edge faded out via DstIn alpha gradient
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0.0f to Color.Transparent,
                                0.24f to Color.Transparent,
                                0.44f to Color.Black.copy(alpha = 0.55f),
                                0.66f to Color.Black,
                                1.0f to Color.Black
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outerRadius = orbitRadiusPx + (nodeSizePx * 0.55f)

                drawCircle(
                    color = wheelBgColor.copy(alpha = 0.94f),
                    radius = outerRadius,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    color = trackColor.copy(alpha = 0.50f),
                    radius = orbitRadiusPx,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx())
                )

                val tickCount = 24
                val phaseDeg = -(animatedSlotOffset * stepAngleDeg)
                for (i in 0 until tickCount) {
                    val deg = phaseDeg + i * (360f / tickCount)
                    val rad = Math.toRadians(deg.toDouble())
                    val isMajor = i % 2 == 0
                    val r0 = outerRadius - if (isMajor) 8.dp.toPx() else 4.dp.toPx()
                    drawLine(
                        color = if (isMajor) activeAccent.copy(alpha = 0.55f) else trackColor.copy(alpha = 0.35f),
                        start = Offset(cx + (r0 * cos(rad)).toFloat(), cy + (r0 * sin(rad)).toFloat()),
                        end = Offset(cx + (outerRadius * cos(rad)).toFloat(), cy + (outerRadius * sin(rad)).toFloat()),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 3-o'clock active selector halo on right-hand side for thumb alignment
                drawCircle(
                    color = activeAccent.copy(alpha = 0.26f),
                    radius = (nodeSizePx * 0.64f),
                    center = Offset(cx + orbitRadiusPx, cy)
                )
            }

            // Center anchor dot directly over the category icon
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(activeAccent.copy(alpha = 0.28f))
                    .border(1.5.dp, activeAccent.copy(alpha = 0.8f), CircleShape)
            )

            // Visible Half-Circle Nodes (with smooth left-side alpha fade)
            HalfCircleWheelNodes(
                categories = controller.categories,
                selectedIndex = controller.selectedIndex,
                animatedSlotOffset = animatedSlotOffset,
                halfWheelPx = halfWheelPx,
                orbitRadiusPx = orbitRadiusPx,
                nodeSizePx = nodeSizePx,
                nodeSizeDp = nodeSizeDp,
                stepAngleDeg = stepAngleDeg,
                customEmojis = customEmojis,
                onNodeTap = null
            )
        }
    }
}

@Composable
private fun HalfCircleWheelNodes(
    categories: List<String>,
    selectedIndex: Int,
    animatedSlotOffset: Float,
    halfWheelPx: Float,
    orbitRadiusPx: Float,
    nodeSizePx: Float,
    nodeSizeDp: androidx.compose.ui.unit.Dp,
    stepAngleDeg: Float,
    customEmojis: Map<String, String>,
    onNodeTap: ((String) -> Unit)?
) {
    val count = categories.size.coerceAtLeast(1)
    categories.forEachIndexed { index, cat ->
        var diff = (index.toFloat() - animatedSlotOffset) % count.toFloat()
        if (diff > count / 2f) diff -= count
        if (diff < -count / 2f) diff += count

        val angleDeg = diff * stepAngleDeg
        val absAngle = abs(angleDeg)
        if (absAngle <= 112f) {
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val nodeCenterX = halfWheelPx + (orbitRadiusPx * cos(angleRad)).toFloat()
            val nodeCenterY = halfWheelPx + (orbitRadiusPx * sin(angleRad)).toFloat()
            val nodeTopLeftX = (nodeCenterX - nodeSizePx / 2f).roundToInt()
            val nodeTopLeftY = (nodeCenterY - nodeSizePx / 2f).roundToInt()

            // Left-part fade: items approaching/past the left semicircle fade smoothly to 0 alpha
            val horizontalBias = ((cos(angleRad).toFloat() + 0.15f) / 0.55f).coerceIn(0f, 1f)
            val angularAlpha = if (absAngle <= 72f) {
                1f
            } else {
                (1f - ((absAngle - 72f) / 40f)).coerceIn(0f, 1f)
            }
            val nodeAlpha = (horizontalBias * angularAlpha).coerceIn(0f, 1f)

            if (nodeAlpha > 0.02f) {
                val isSelected = index == selectedIndex
                val nodeAccent = categoryAccentColor(cat)
                val nodeEmoji = customEmojis[cat.trim().lowercase()]
                val catTag = cat.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                val nodeScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.22f else 0.90f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "half_wheel_node_scale_$index"
                )

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) nodeAccent else MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = if (isSelected) 6.dp else 1.dp,
                    modifier = Modifier
                        .offset { IntOffset(nodeTopLeftX, nodeTopLeftY) }
                        .size(nodeSizeDp)
                        .alpha(nodeAlpha)
                        .graphicsLayer {
                            scaleX = nodeScale
                            scaleY = nodeScale
                        }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else nodeAccent.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                        .then(
                            if (onNodeTap != null) {
                                Modifier.clickable { onNodeTap(cat) }
                            } else {
                                Modifier
                            }
                        )
                        .testTag("wheel_category_node_$catTag")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CategoryBadgeGlyph(
                            category = cat,
                            tint = if (isSelected) Color.White else nodeAccent,
                            iconSize = 18.dp,
                            customEmoji = nodeEmoji
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standalone interactive compact half-circle scroll wheel overlay launched from a notification
 * action button so users can spin or tap a category in one hand without opening the full app.
 */
@Composable
fun NotificationQuickCategoryWheelOverlay(
    transactionTitle: String,
    amountDisplay: String,
    initialCategory: String,
    availableCategories: List<String>,
    onSelectCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val performHaptic = rememberAppHapticPerformer()
    val density = LocalDensity.current
    val customEmojis = rememberCustomCategoryEmojis()

    val isIncomeTx = remember(initialCategory) {
        ExpenseCategories.isIncomeCategory(initialCategory)
    }
    val categories = remember(availableCategories, initialCategory, isIncomeTx) {
        (availableCategories + listOf(initialCategory))
            .map { ExpenseCategories.normalizeCategory(it) }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filter { cat ->
                if (isIncomeTx) {
                    ExpenseCategories.isIncomeCategory(cat)
                } else {
                    !ExpenseCategories.isIncomeCategory(cat)
                }
            }
            .ifEmpty {
                if (isIncomeTx) ExpenseCategories.INCOME_CATEGORIES else ExpenseCategories.EXPENSE_ONLY
            }
    }
    val initialIndex = remember(categories, initialCategory) {
        val norm = ExpenseCategories.normalizeCategory(initialCategory)
        categories.indexOfFirst { it.equals(norm, ignoreCase = true) }.coerceAtLeast(0)
    }

    var selectedIndex by remember(initialIndex) { mutableIntStateOf(initialIndex) }
    var wheelSlotOffset by remember(initialIndex) { mutableFloatStateOf(initialIndex.toFloat()) }
    var totalDragPx by remember { mutableFloatStateOf(0f) }

    val animatedSlotOffset by animateFloatAsState(
        targetValue = wheelSlotOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "notif_half_circle_wheel_slot_offset"
    )

    val pxPerSlot = with(density) { 36.dp.toPx() }
    val wheelDiameterDp = 188.dp
    val orbitRadiusDp = 64.dp
    val nodeSizeDp = 38.dp
    val wheelSizePx = with(density) { wheelDiameterDp.toPx() }
    val halfWheelPx = wheelSizePx / 2f
    val orbitRadiusPx = with(density) { orbitRadiusDp.toPx() }
    val nodeSizePx = with(density) { nodeSizeDp.toPx() }
    val stepAngleDeg = 34f
    val count = categories.size.coerceAtLeast(1)

    val selectedCat = categories.getOrElse(selectedIndex.coerceIn(0, categories.lastIndex)) { "Unknown" }
    val activeAccent = categoryAccentColor(selectedCat)
    val activeEmoji = customEmojis[selectedCat.trim().lowercase()]
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val wheelBgColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            }
            .testTag("notification_wheel_overlay")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { /* Consume taps inside card/wheel area */ })
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = listOf(amountDisplay, transactionTitle).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Assign Category" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Spin wheel or tap any icon to assign",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .border(1.5.dp, activeAccent.copy(alpha = 0.75f), RoundedCornerShape(50))
                    .clickable {
                        performHaptic()
                        onSelectCategory(selectedCat)
                    }
                    .testTag("category_wheel_active_label")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadgeGlyph(
                        category = selectedCat,
                        tint = activeAccent,
                        iconSize = 18.dp,
                        customEmoji = activeEmoji
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set: $selectedCat",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(wheelDiameterDp)
                    .pointerInput(categories) {
                        detectVerticalDragGestures(
                            onDragStart = { totalDragPx = 0f },
                            onDragEnd = {
                                if (totalDragPx > 32f) {
                                    performHaptic()
                                    onSelectCategory(categories.getOrElse(selectedIndex) { selectedCat })
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDragPx += abs(dragAmount)
                                wheelSlotOffset += (-dragAmount) / pxPerSlot.coerceAtLeast(12f)
                                val nextIdx = positiveMod(wheelSlotOffset.roundToInt(), count)
                                if (nextIdx != selectedIndex) {
                                    selectedIndex = nextIdx
                                    performHaptic()
                                }
                            }
                        )
                    }
                    .testTag("circular_category_wheel")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0.0f to Color.Transparent,
                                    0.24f to Color.Transparent,
                                    0.44f to Color.Black.copy(alpha = 0.55f),
                                    0.66f to Color.Black,
                                    1.0f to Color.Black
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val outerRadius = orbitRadiusPx + (nodeSizePx * 0.55f)

                    drawCircle(
                        color = wheelBgColor.copy(alpha = 0.94f),
                        radius = outerRadius,
                        center = Offset(cx, cy)
                    )

                    drawCircle(
                        color = trackColor.copy(alpha = 0.50f),
                        radius = orbitRadiusPx,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    val tickCount = 24
                    val phaseDeg = -(animatedSlotOffset * stepAngleDeg)
                    for (i in 0 until tickCount) {
                        val deg = phaseDeg + i * (360f / tickCount)
                        val rad = Math.toRadians(deg.toDouble())
                        val isMajor = i % 2 == 0
                        val r0 = outerRadius - if (isMajor) 8.dp.toPx() else 4.dp.toPx()
                        drawLine(
                            color = if (isMajor) activeAccent.copy(alpha = 0.55f) else trackColor.copy(alpha = 0.35f),
                            start = Offset(cx + (r0 * cos(rad)).toFloat(), cy + (r0 * sin(rad)).toFloat()),
                            end = Offset(cx + (outerRadius * cos(rad)).toFloat(), cy + (outerRadius * sin(rad)).toFloat()),
                            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    drawCircle(
                        color = activeAccent.copy(alpha = 0.26f),
                        radius = (nodeSizePx * 0.64f),
                        center = Offset(cx + orbitRadiusPx, cy)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(activeAccent.copy(alpha = 0.28f))
                        .border(1.5.dp, activeAccent.copy(alpha = 0.8f), CircleShape)
                )

                HalfCircleWheelNodes(
                    categories = categories,
                    selectedIndex = selectedIndex,
                    animatedSlotOffset = animatedSlotOffset,
                    halfWheelPx = halfWheelPx,
                    orbitRadiusPx = orbitRadiusPx,
                    nodeSizePx = nodeSizePx,
                    nodeSizeDp = nodeSizeDp,
                    stepAngleDeg = stepAngleDeg,
                    customEmojis = customEmojis,
                    onNodeTap = { chosen ->
                        performHaptic()
                        onSelectCategory(chosen)
                    }
                )
            }
        }
    }
}

/**
 * Single-finger long-press-drag-release modifier attached to category icons:
 * - Short tap opens normal transaction edit (`onTap`).
 * - Horizontal/vertical drag before long-press yields to parent list scroll / card swipe.
 * - Holding for `longPressTimeout` opens the compact left-faded half-circle wheel directly over
 *   this category icon, continuously tracks the same held finger, and commits on finger release
 *   with zero second click required.
 */
fun Modifier.categoryWheelLongPressGesture(
    transaction: ExpenseTransaction,
    availableCategories: List<String>,
    onTap: () -> Unit,
    onFallbackQuickChange: () -> Unit = {}
): Modifier = composed {
    val controller = LocalCategoryWheelController.current
    val customEmojis = rememberCustomCategoryEmojis()
    val performHaptic = rememberAppHapticPerformer()

    val currentTx by rememberUpdatedState(transaction)
    val currentCategories by rememberUpdatedState(availableCategories)
    val currentEmojis by rememberUpdatedState(customEmojis)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentFallback by rememberUpdatedState(onFallbackQuickChange)

    var centerInRoot by remember { mutableStateOf(Offset.Zero) }

    this
        .onGloballyPositioned { coords ->
            val pos = coords.positionInRoot()
            centerInRoot = Offset(
                x = pos.x + coords.size.width / 2f,
                y = pos.y + coords.size.height / 2f
            )
        }
        .semantics {
            onClick(label = "Edit ${currentTx.title}") {
                currentOnTap()
                true
            }
            onLongClick(label = "Change category for ${currentTx.title}") {
                if (controller != null) {
                    performHaptic()
                    controller.start(
                        transaction = currentTx,
                        anchorInRoot = centerInRoot,
                        availableCategories = currentCategories,
                        emojiMap = currentEmojis
                    )
                } else {
                    currentFallback()
                }
                true
            }
        }
        .pointerInput(transaction.id, transaction.category) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val downPos = down.position
                val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                val touchSlop = viewConfiguration.touchSlop

                var abortedBeforeLongPress = false
                var liftedCleanlyAsTap = false

                val earlyFinish = withTimeoutOrNull(longPressTimeout) {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change == null || !change.pressed) {
                            liftedCleanlyAsTap = true
                            change?.consume()
                            return@withTimeoutOrNull true
                        }
                        if ((change.position - downPos).getDistance() > touchSlop) {
                            abortedBeforeLongPress = true
                            return@withTimeoutOrNull false
                        }
                    }
                    @Suppress("UNREACHABLE_CODE")
                    false
                }

                if (liftedCleanlyAsTap && earlyFinish == true) {
                    performHaptic()
                    currentOnTap()
                    return@awaitEachGesture
                }

                if (abortedBeforeLongPress || earlyFinish != null) {
                    return@awaitEachGesture
                }

                // Long-press timeout elapsed while finger stayed down -> open half-circle wheel
                down.consume()
                performHaptic()

                if (controller == null) {
                    currentFallback()
                    return@awaitEachGesture
                }

                controller.start(
                    transaction = currentTx,
                    anchorInRoot = centerInRoot,
                    availableCategories = currentCategories,
                    emojiMap = currentEmojis
                )

                var lastPos = downPos
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                    if (change == null || !change.pressed) {
                        change?.consume()
                        controller.commitAndClose()
                        break
                    }
                    val delta = change.position - lastPos
                    if (delta != Offset.Zero) {
                        change.consume()
                        lastPos = change.position
                        controller.updateDrag(delta)
                    }
                }
            }
        }
}
