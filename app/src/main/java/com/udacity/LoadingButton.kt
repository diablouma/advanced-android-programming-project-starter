package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var buttonWidth = 0
    private var sweepAngle = 360f

    private var btnBackgroundColor = 0
        get() = field
        set(value) {
            field = value
        }
    private var btnTextColor = 0
        get() = field
        set(value) {
            field = value
        }

    private val valueAnimator = ValueAnimator()

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Loading -> animateLoading()
        }
    }

    private fun animateLoading() {
        val animationForCircle = ValueAnimator.ofFloat(0f, sweepAngle)
        animationForCircle.repeatMode = ValueAnimator.RESTART
        animationForCircle.repeatCount = 1
        animationForCircle.addUpdateListener { valueAnimator ->
            val progress = valueAnimator.animatedValue as Float
            this.sweepAngle = progress
            this.invalidate()
        }

        val animationForLoadingBar = ValueAnimator.ofInt(0, widthSize)
        animationForLoadingBar.repeatMode = ValueAnimator.RESTART
        animationForLoadingBar.addUpdateListener { valueAnimator ->
            val progress = valueAnimator.animatedValue as Int
            this.buttonWidth = progress
            valueAnimator.interpolator = LinearInterpolator()
            this.invalidate()
        }

        val set = AnimatorSet()
        set.playTogether(animationForCircle, animationForLoadingBar)
        set.duration = 4000
        set.start()
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            btnBackgroundColor = getColor(R.styleable.LoadingButton_btnBackground, 0)
            btnTextColor = getColor(R.styleable.LoadingButton_btnTextColor, 0)
        }
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        invalidate()
        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = btnBackgroundColor
        canvas.drawRect(
            RectF(0f, 0f, context.resources.displayMetrics.widthPixels - 24f, 150f),
            paint
        )

        if (buttonState.equals(ButtonState.Loading)) {
            paint.color = ContextCompat.getColor(context, R.color.colorPrimaryDark);
            canvas.drawRect(
                RectF(0f, 0f, buttonWidth + 0f, 150f),
                paint
            )

            paint.color = ContextCompat.getColor(context, R.color.colorAccent);
            canvas.drawArc(800f, 20f, 880f, 100f, -360f, sweepAngle, true, paint)

            paint.color = btnTextColor
            canvas.drawText(resources.getString(R.string.button_loading), width / 2f, height / 2f, paint)
        } else {
            paint.color = btnTextColor
            canvas.drawText(
                resources.getString(R.string.button_download_initial_text),
                width / 2f,
                height / 2f,
                paint
            )
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}