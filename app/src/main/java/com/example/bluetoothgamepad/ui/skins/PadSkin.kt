package com.example.bluetoothgamepad.ui.skins

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.bluetoothgamepad.domain.ProfileKind
import com.example.bluetoothgamepad.ui.rememberDownsampledImage
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Visual style of the controller surface. Controls read it from [LocalPadSkin] so the drawing code
 * can stay free of plumbing; the body/colour layer is drawn once by `GamepadScreen`.
 */
enum class PadSkin {
    /** Xbox-like: rounded shell, grips, glossy controls. Also used by the Android profile. */
    MODERN,
    /** NES-like: boxy shell, flat matte controls. */
    RETRO8,
    /** Mega Drive-like: rounded shell without grips, glossy controls. */
    SEGA6;

    /** Glossy 3D shading for buttons and stick caps; the retro skin stays flat. */
    val gloss: Boolean get() = this != RETRO8
    val roundBody: Boolean get() = this != RETRO8
    val grips: Boolean get() = this == MODERN

    companion object {
        fun forProfile(kind: ProfileKind): PadSkin = when (kind) {
            ProfileKind.EIGHT_BIT -> RETRO8
            ProfileKind.SEGA -> SEGA6
            else -> MODERN
        }
    }
}

val LocalPadSkin = staticCompositionLocalOf { PadSkin.MODERN }

/**
 * Colours of the controller shell. Derived from the user's control colour so the existing
 * appearance settings keep driving the look; only the image is exclusive to the pad skin.
 */
fun padBodyColor(controlColor: Color, dark: Boolean): Color =
    if (dark) lerp(lerp(controlColor, Color.Black, .68f), Color(0xFF14121A), .40f)
    else lerp(controlColor, Color.White, .72f)

@Composable
fun PadBody(
    skin: PadSkin,
    bodyColor: Color,
    outlineColor: Color,
    imageUri: String?,
    modifier: Modifier = Modifier
) {
    val window = LocalWindowInfo.current.containerSize
    val image = rememberDownsampledImage(imageUri, window.width, window.height)
    Canvas(modifier.fillMaxSize()) { drawPadBody(skin, bodyColor, outlineColor, image) }
}

fun DrawScope.drawPadBody(skin: PadSkin, bodyColor: Color, outlineColor: Color, image: ImageBitmap?) {
    val body = padBodyPath(skin, size)
    val strokeWidth = size.minDimension * .006f

    if (image != null) {
        clipPath(body) { drawCenterCropped(image, size) }
        // Keep the controls readable over a photo.
        drawPath(body, Color.Black.copy(alpha = .42f))
    } else {
        drawPath(body, bodyColor)
        if (skin.roundBody) {
            clipPath(body) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = .10f), Color.Transparent),
                        startY = 0f, endY = size.height * .55f
                    )
                )
            }
        }
    }
    drawPath(body, outlineColor, style = Stroke(width = strokeWidth))
}

/**
 * Shortens the main shell just enough for the grips to read as a controller silhouette.
 *
 * The controls are laid out with a fixed padding, so this value cannot be large: if the main shell
 * is shorter than the control area the bottom row spills into the grips and looks cut off. Keep
 * `PadBody`'s visible body taller than the padded control area.
 */
private const val GRIP_DROP_FRACTION = .07f

/** Rounded shell plus optional bottom grips, combined into a single outline. */
fun padBodyPath(skin: PadSkin, size: Size): Path {
    val inset = size.minDimension * .02f
    val radius = if (skin.roundBody) size.minDimension * .14f else size.minDimension * .035f
    // Grips only read as a controller silhouette in landscape.
    val grips = skin.grips && size.width > size.height
    val drop = if (grips) size.height * GRIP_DROP_FRACTION else 0f
    val base = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                Rect(inset, inset, size.width - inset, size.height - inset - drop),
                CornerRadius(radius, radius)
            )
        )
    }
    if (!grips) return base
    val gripRadius = min(size.width * .16f, size.height * .30f)
    val centerY = size.height - inset - gripRadius
    var result = base
    listOf(inset + gripRadius, size.width - inset - gripRadius).forEach { centerX ->
        val grip = Path().apply {
            addOval(Rect(centerX - gripRadius, centerY - gripRadius, centerX + gripRadius, centerY + gripRadius))
        }
        val merged = Path()
        merged.op(result, grip, PathOperation.Union)
        result = merged
    }
    return result
}

fun DrawScope.drawCenterCropped(image: ImageBitmap, size: Size) {
    if (image.width <= 0 || image.height <= 0) return
    val scale = max(size.width / image.width, size.height / image.height)
    val width = image.width * scale
    val height = image.height * scale
    val left = (size.width - width) / 2f
    val top = (size.height - height) / 2f
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
        dstSize = IntSize(width.roundToInt(), height.roundToInt())
    )
}

// ---------------------------------------------------------------- shared drawing helpers

fun lighten(color: Color, amount: Float): Color = lerp(color, Color.White, amount)
fun darken(color: Color, amount: Float): Color = lerp(color, Color.Black, amount)

/**
 * Shaded ball used for face buttons, Sega buttons and stick caps. No highlight ring: a ring drawn
 * over the ball reads as a separate circle and was mistaken for a socket behind the button.
 */
fun DrawScope.drawBall(color: Color, radius: Float, center: Offset, gloss: Boolean) {
    if (!gloss) {
        drawCircle(color, radius, center)
        return
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(lighten(color, .45f), color, darken(color, .55f)),
            center = center + Offset(-radius * .34f, -radius * .40f),
            radius = radius * 1.65f
        ),
        radius = radius,
        center = center
    )
}

/** Rounded plus used for every D-pad; the two bars are merged so the outline has no seams. */
fun plusPath(size: Size, thickness: Float, corner: Float): Path {
    val horizontal = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                Rect(0f, size.height * (1f - thickness) / 2f, size.width, size.height * (1f + thickness) / 2f),
                CornerRadius(corner, corner)
            )
        )
    }
    val vertical = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                Rect(size.width * (1f - thickness) / 2f, 0f, size.width * (1f + thickness) / 2f, size.height),
                CornerRadius(corner, corner)
            )
        )
    }
    val merged = Path()
    merged.op(horizontal, vertical, PathOperation.Union)
    return merged
}
