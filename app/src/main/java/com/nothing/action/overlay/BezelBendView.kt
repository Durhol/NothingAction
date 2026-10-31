package com.nothing.action.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.nothing.action.core.model.PressType

class BezelBendView(context: Context) : View(context) {

    // 100% solid, fully opaque pitch black - PorterDuff.Mode.SRC forces absolute zero transparency
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt() // Solid #000000 with 255 alpha
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }

    private val path = Path()
    private var currentDepth = 0f
    private var isHoldingLongPress = false

    private val density: Float = context.resources.displayMetrics.density
    private var isRightSide = true
    private var configuredButtonLengthDp = 95f
    private var configuredButtonWidthDp = 26f // Solid, prominent protrusion

    var onAnimationFinished: (() -> Unit)? = null

    init {
        // Explicitly clear any theme window background or elevation shadow
        setBackgroundColor(Color.TRANSPARENT)
        elevation = 0f
    }

    override fun hasOverlappingRendering(): Boolean = false

    fun updateCalibration(
        side: String,
        buttonLengthDp: Float,
        buttonWidthDp: Float
    ) {
        this.isRightSide = side.equals("RIGHT", ignoreCase = true)
        this.configuredButtonLengthDp = buttonLengthDp.coerceIn(50f, 220f)
        this.configuredButtonWidthDp = buttonWidthDp.coerceIn(12f, 60f)
        invalidate()
    }

    fun animatePress(type: PressType) {
        val peak = configuredButtonWidthDp * density

        when (type) {
            PressType.SINGLE -> {
                runIndent(from = 0f, to = peak, duration = 180L, interpolator = OvershootInterpolator(1.35f)) {
                    runIndent(from = peak, to = 0f, duration = 150L, interpolator = DecelerateInterpolator()) {
                        onAnimationFinished?.invoke()
                    }
                }
            }

            PressType.DOUBLE -> {
                runIndent(from = 0f, to = peak * 0.85f, duration = 110L) {
                    runIndent(from = peak * 0.85f, to = peak * 0.2f, duration = 70L) {
                        runIndent(from = peak * 0.2f, to = peak * 1.15f, duration = 130L, interpolator = OvershootInterpolator(1.4f)) {
                            runIndent(from = peak * 1.15f, to = 0f, duration = 150L) {
                                onAnimationFinished?.invoke()
                            }
                        }
                    }
                }
            }

            PressType.LONG -> {
                isHoldingLongPress = true
                runIndent(from = 0f, to = peak * 1.3f, duration = 240L, interpolator = OvershootInterpolator(1.1f))
            }
        }
    }

    fun releaseLongPress() {
        if (!isHoldingLongPress) return
        isHoldingLongPress = false
        runIndent(from = currentDepth, to = 0f, duration = 170L, interpolator = OvershootInterpolator(1.4f)) {
            onAnimationFinished?.invoke()
        }
    }

    private fun runIndent(
        from: Float,
        to: Float,
        duration: Long,
        interpolator: android.view.animation.Interpolator = DecelerateInterpolator(),
        onEnd: (() -> Unit)? = null
    ) {
        val animator = ValueAnimator.ofFloat(from, to).apply {
            this.duration = duration
            this.interpolator = interpolator
            addUpdateListener {
                currentDepth = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentDepth <= 0.2f) return

        path.reset()
        val w = width.toFloat()
        val h = height.toFloat()

        val buttonLengthPx = configuredButtonLengthDp * density
        val centerY = h / 2f
        val topY = centerY - (buttonLengthPx / 2f)
        val bottomY = centerY + (buttonLengthPx / 2f)

        // iOS 18 mechanical button bump geometry - completely solid, non-transparent
        val r = (10f * density).coerceAtMost(currentDepth * 0.8f)

        if (isRightSide) {
            val rightEdge = w
            val innerX = rightEdge - currentDepth

            path.moveTo(rightEdge, topY - r)
            path.cubicTo(
                rightEdge, topY,
                innerX, topY,
                innerX, topY + r
            )
            path.lineTo(innerX, bottomY - r)
            path.cubicTo(
                innerX, bottomY,
                rightEdge, bottomY,
                rightEdge, bottomY + r
            )
            path.lineTo(rightEdge, topY - r)
            path.close()
        } else {
            val leftEdge = 0f
            val innerX = leftEdge + currentDepth

            path.moveTo(leftEdge, topY - r)
            path.cubicTo(
                leftEdge, topY,
                innerX, topY,
                innerX, topY + r
            )
            path.lineTo(innerX, bottomY - r)
            path.cubicTo(
                innerX, bottomY,
                leftEdge, bottomY,
                leftEdge, bottomY + r
            )
            path.lineTo(leftEdge, topY - r)
            path.close()
        }

        // Draw with 100% solid opacity replacing destination buffer
        canvas.drawPath(path, fillPaint)
    }
}
