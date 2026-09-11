package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Standard feRey Wordmark component matching the brand typography.
 * Clean, bold, modern geometric sans-serif: "feRey".
 */
@Composable
fun FeReyWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 48.sp,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    val style = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        letterSpacing = (-0.5).sp,
        color = color
    )

    Row(
        modifier = modifier.testTag("ferey_wordmark"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = "f", style = style)
        Text(text = "e", style = style)
        Text(text = "R", style = style)
        Text(text = "e", style = style)
        Text(text = "y", style = style)
    }
}

/**
 * feRey Startup Animation
 * - Starts blank in current theme (pure white or pure black)
 * - R appears in the center
 * - 'f' and 'e' smoothly move toward 'R' from the left
 * - 'e' and 'y' smoothly move toward 'R' from the right
 * - Movement feels pulled toward the stable central 'R'
 * - Completes wordmark "feRey", holds briefly, transitions smoothly.
 */
@Composable
fun FeReyStartupAnimation(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground

    // Animatable values for letter positions & opacity
    val rAlpha = remember { Animatable(0f) }
    val leftOffset = remember { Animatable(-140f) } // starts far left
    val leftAlpha = remember { Animatable(0f) }
    val rightOffset = remember { Animatable(140f) } // starts far right
    val rightAlpha = remember { Animatable(0f) }

    val letterStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 54.sp,
        letterSpacing = (-0.5).sp,
        color = contentColor
    )

    LaunchedEffect(Unit) {
        // Step 1: Central R emerges stable in the center
        delay(120)
        rAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 250, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
        )

        // Step 2: Left and right letters animate inward pulled toward central R
        // Smooth easing: FastOutSlowIn / CubicBezier smooth pull
        val pullEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

        launch {
            leftOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 650, easing = pullEasing)
            )
        }
        launch {
            leftAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450)
            )
        }
        launch {
            rightOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 650, easing = pullEasing)
            )
        }
        launch {
            rightAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450)
            )
        }

        // Step 3: Hold the completed logo briefly (400ms)
        delay(750)

        // Step 4: Finish startup animation
        onAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("startup_animation_screen"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left letters: f, e
            Row(
                modifier = Modifier
                    .offset(x = leftOffset.value.dp)
                    .alpha(leftAlpha.value)
                    .testTag("startup_letters_left")
            ) {
                Text(text = "f", style = letterStyle)
                Text(text = "e", style = letterStyle)
            }

            // Central R: stable, anchored
            Text(
                text = "R",
                style = letterStyle,
                modifier = Modifier
                    .alpha(rAlpha.value)
                    .testTag("startup_letter_center_r")
            )

            // Right letters: e, y
            Row(
                modifier = Modifier
                    .offset(x = rightOffset.value.dp)
                    .alpha(rightAlpha.value)
                    .testTag("startup_letters_right")
            ) {
                Text(text = "e", style = letterStyle)
                Text(text = "y", style = letterStyle)
            }
        }
    }
}
