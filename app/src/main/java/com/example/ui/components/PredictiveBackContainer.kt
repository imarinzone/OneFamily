package com.example.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Official Android / Material 3 Predictive Back interpolators:
 * - Standard Decelerate (`CubicBezierEasing(0f, 0f, 0f, 1f)`) for live gesture progress tracking.
 * - Emphasized Decelerate / Accelerate for commit screen transitions.
 */
val PredictiveBackStandardDecelerateEasing = CubicBezierEasing(0f, 0f, 0f, 1f)
val PredictiveBackEmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
val PredictiveBackEmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

/**
 * Official Android Predictive Back screen transition spec for [androidx.compose.animation.AnimatedContent]:
 * - On back (`isPop = true`):
 *   - Exiting screen scales down from 100% -> 90% and fades out by the 35% progress threshold (105ms of 300ms).
 *   - Entering (previous) screen settles from 108% -> 100% and fades in starting at the 35% threshold.
 * - On forward navigation (`isPop = false`):
 *   - Incoming screen scales cleanly from 92% -> 100% with fade-through.
 *   - Previous screen subtly scales up (100% -> 105%) and fades out.
 */
fun predictiveBackContentTransform(isPop: Boolean): ContentTransform {
    return if (isPop) {
        val enter = scaleIn(
            initialScale = 1.08f,
            animationSpec = tween(
                durationMillis = 300,
                easing = PredictiveBackEmphasizedDecelerateEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 195,
                delayMillis = 105
            )
        )
        val exit = scaleOut(
            targetScale = 0.90f,
            animationSpec = tween(
                durationMillis = 250,
                easing = PredictiveBackEmphasizedAccelerateEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 105
            )
        )
        (enter togetherWith exit).apply {
            // Keep the exiting foreground screen visually on top while shrinking & fading through.
            targetContentZIndex = 0f
        }
    } else {
        val enter = scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(
                durationMillis = 300,
                easing = PredictiveBackEmphasizedDecelerateEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 210,
                delayMillis = 70
            )
        )
        val exit = scaleOut(
            targetScale = 1.05f,
            animationSpec = tween(
                durationMillis = 250,
                easing = PredictiveBackEmphasizedAccelerateEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 100
            )
        )
        (enter togetherWith exit).apply {
            targetContentZIndex = 1f
        }
    }
}

/**
 * Wraps an in-app screen or sub-destination with live Android Predictive Back gesture preview
 * following official Android design specs:
 * - Intercepts back gesture progress via [PredictiveBackHandler] only when [enabled] is true
 *   (when false at root, system back-to-home predictive animation runs natively).
 * - Scales the surface smoothly down toward 90% with `STANDARD_DECELERATE` easing.
 * - Shifts horizontally away from the active swipe edge leaving an 8dp margin:
 *   `((width / 20) - 8dp)`.
 * - Shifts vertically with soft deceleration bounded within `((height / 20) - 8dp)`.
 * - Detaches edge-to-edge corners into 28dp Material rounded corners with subtle elevation.
 * - Smoothly springs back to edge-to-edge full scale if the user cancels the back gesture.
 */
@Composable
fun PredictiveBackSurface(
    enabled: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val resetScope = rememberCoroutineScope()
    val backProgress = remember { Animatable(0f) }
    val verticalOffsetPx = remember { Animatable(0f) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    var initialTouchY by remember { mutableFloatStateOf(Float.NaN) }

    LaunchedEffect(enabled) {
        if (!enabled) {
            backProgress.snapTo(0f)
            verticalOffsetPx.snapTo(0f)
            initialTouchY = Float.NaN
        }
    }

    PredictiveBackHandler(enabled = enabled) { progressFlow ->
        initialTouchY = Float.NaN
        try {
            progressFlow.collect { backEvent ->
                swipeEdge = backEvent.swipeEdge
                if (initialTouchY.isNaN()) {
                    initialTouchY = backEvent.touchY
                }
                val eased = PredictiveBackStandardDecelerateEasing.transform(
                    backEvent.progress.coerceIn(0f, 1f)
                )
                backProgress.snapTo(eased)
                verticalOffsetPx.snapTo(backEvent.touchY - initialTouchY)
            }
            onBack()
            resetScope.launch {
                backProgress.snapTo(0f)
                verticalOffsetPx.snapTo(0f)
            }
        } catch (e: CancellationException) {
            resetScope.launch {
                launch {
                    backProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
                launch {
                    verticalOffsetPx.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }
            throw e
        }
    }

    val progress = backProgress.value.coerceIn(0f, 1f)
    val scrimColor = if (progress > 0.001f) {
        MaterialTheme.colorScheme.scrim.copy(alpha = 0.14f * (1f - progress * 0.5f))
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (progress > 0.001f) {
                        val scale = 1f - 0.10f * progress
                        scaleX = scale
                        scaleY = scale

                        val marginPx = 8.dp.toPx()
                        val maxShiftX = ((size.width / 20f) - marginPx).coerceAtLeast(0f)
                        val direction = if (swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
                        translationX = direction * maxShiftX * progress

                        val maxShiftY = ((size.height / 20f) - marginPx).coerceAtLeast(0f)
                        val rawDeltaY = verticalOffsetPx.value * 0.25f
                        translationY = if (maxShiftY > 0f) {
                            rawDeltaY.coerceIn(-maxShiftY, maxShiftY)
                        } else {
                            0f
                        }

                        shape = RoundedCornerShape(28.dp * progress)
                        clip = true
                        shadowElevation = 18.dp.toPx() * progress
                    }
                }
        ) {
            content()
        }
    }
}
