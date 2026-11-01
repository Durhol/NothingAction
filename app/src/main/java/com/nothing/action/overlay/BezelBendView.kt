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

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt()
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#333333")
    }

    private val path = Path()
    private var currentDepth = 0f
    private var isHoldingLongPress = false

    private val density: Float = context.resources.displayMetrics.density
    private var isRightSide = true
    private var configuredButtonLengthDp = 95f
    private var configuredButtonWidthDp = 24f
    private var configuredCornerRadiusDp = 12f
    private var configuredStrokeEnabled = false

    var onAnimationFinished: (() -> Unit)? = null

    init {
        setBackgroundColor(Color.TRANSPARENT)
        elevation = 0f
    }

    override fun hasOverlappingRendering(): Boolean = false

    fun updateCalibration(
        side: String,
        buttonLengthDp: Float,
        buttonWidthDp: Float,
        cornerRadiusDp: Float = 12f,
        fillColorHex: String = "#000000",
        strokeEnabled: Boolean = false,
        strokeWidthDp: Float = 1.5f,
        strokeColorHex: String = "#333333"
    ) {
        this.isRightSide = side.equals("RIGHT", ignoreCase = true)
        this.configuredButtonLengthDp = buttonLengthDp.coerceIn(40f, 320f)
        this.configuredButtonWidthDp = buttonWidthDp.coerceIn(8f, 75f)
        this.configuredCornerRadiusDp = cornerRadiusDp.coerceIn(2f, 36f)
        this.configuredStrokeEnabled = strokeEnabled

        try {
            val colorInt = Color.parseColor(fillColorHex)
            fillPaint.color = colorInt
        } catch (_: Exception) {
            fillPaint.color = 0xFF000000.toInt()
        }

        if (strokeEnabled) {
            try {
                strokePaint.color = Color.parseColor(strokeColorHex)
                strokePaint.strokeWidth = strokeWidthDp.coerceIn(0.5f, 6f) * density
            } catch (_: Exception) {
                strokePaint.color = Color.parseColor("#333333")
                strokePaint.strokeWidth = 1.5f * density
            }
        }

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

        val r = (configuredCornerRadiusDp * density).coerceAtMost(currentDepth * 0.9f)

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

        // Draw solid fill
        canvas.drawPath(path, fillPaint)

        // Draw optional stroke outline
        if (configuredStrokeEnabled) {
            canvas.drawPath(path, strokePaint)
        }
    }
}
