package com.example.myapplication.ext

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener

fun <T : View> T.click(action: (T) -> Unit) {
    setOnClickListener { action(this) }
}

fun <T : View> T.singleClick(interval: Long = 500L, action: ((T) -> Unit)?) {
    setOnClickListener(SingleClickListener(interval, action))
}

class SingleClickListener<T : View>(
    private val interval: Long = 500L,
    private val clickFunc: ((T) -> Unit)?,
) : View.OnClickListener {
    private var lastClickTime = 0L

    @Suppress("UNCHECKED_CAST")
    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - lastClickTime > interval) {
            clickFunc?.invoke(v as T)
            lastClickTime = nowTime
        }
    }
}

fun View?.visible() {
    this?.visibility = View.VISIBLE
}

fun View?.gone() {
    this?.visibility = View.GONE
}

fun View?.invisible() {
    this?.visibility = View.INVISIBLE
}

fun visible(vararg views: View) {
    views.forEach { it.visible() }
}

fun gone(vararg views: View) {
    views.forEach { it.gone() }
}

fun invisible(vararg views: View) {
    views.forEach { it.invisible() }
}

fun View?.visibleAlphaAnimation(duration: Long = 500L) {
    this?.visible()
    this?.startAnimation(
        AlphaAnimation(0f, 1f).apply {
            this.duration = duration
            fillAfter = true
        },
    )
}

fun View?.goneAlphaAnimation(duration: Long = 500L) {
    this?.gone()
    this?.startAnimation(
        AlphaAnimation(1f, 0f).apply {
            this.duration = duration
            fillAfter = true
        },
    )
}

fun View?.invisibleAlphaAnimation(duration: Long = 500L) {
    this?.invisible()
    this?.startAnimation(
        AlphaAnimation(1f, 0f).apply {
            this.duration = duration
            fillAfter = true
        },
    )
}

fun View?.visibleTranslateAnimation(duration: Long = 200L) {
    this?.visible()
    this?.animation =
        TranslateAnimation(
            Animation.RELATIVE_TO_SELF,
            0.0f,
            Animation.RELATIVE_TO_SELF,
            0.0f,
            Animation.RELATIVE_TO_SELF,
            0.0f,
            Animation.RELATIVE_TO_SELF,
            1.0f,
        ).apply {
            repeatCount = 1
            this.duration = duration
        }
}

fun View.setRoundRectBg(
    color: Int = Color.WHITE,
    cornerRadiusDp: Float = 15f,
) {
    background = GradientDrawable().apply {
        setColor(color)
        cornerRadius = context.dpToPx(cornerRadiusDp)
    }
}

fun EditText.inputNum(maxCount: Int, numView: TextView) {
    addTextChangedListener {
        val text = it ?: ""
        numView.text = "${text.length}/$maxCount"
    }
}
