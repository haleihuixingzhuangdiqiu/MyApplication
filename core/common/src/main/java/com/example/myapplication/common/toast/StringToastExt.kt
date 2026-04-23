package com.example.myapplication.common.toast

import android.widget.Toast
import com.hjq.toast.ToastParams
import com.hjq.toast.Toaster
import com.hjq.toast.style.CustomToastStyle
import com.example.myapplication.common.R

/**
 * 基于 [getActivity/Toaster](https://github.com/getActivity/Toaster) 的扩展；须先 [AppToaster.init]。
 *
 * 默认全局样式为 [R.layout.common_toast_plain]；带图标版式为 success / error 专用布局。
 */
fun CharSequence.show() {
    Toaster.showShort(this)
}

fun CharSequence.showLong() {
    Toaster.showLong(this)
}

/**
 * 无左侧图标，纯文案（[R.layout.common_toast_plain]）。
 */
fun CharSequence.showInfo() {
    val p = ToastParams()
    p.text = this
    p.style = CustomToastStyle(R.layout.common_toast_plain)
    p.duration = Toast.LENGTH_SHORT
    Toaster.show(p)
}

fun CharSequence.showSuccess() {
    val p = ToastParams()
    p.text = this
    p.style = CustomToastStyle(R.layout.common_toast_success)
    p.duration = Toast.LENGTH_SHORT
    Toaster.show(p)
}

fun CharSequence.showError() {
    val p = ToastParams()
    p.text = this
    p.style = CustomToastStyle(R.layout.common_toast_error)
    p.duration = Toast.LENGTH_LONG
    Toaster.show(p)
}

fun CharSequence.delayShow(delayMillis: Long) {
    Toaster.delayedShow(this, delayMillis)
}

fun CharSequence.debugShow() {
    Toaster.debugShow(this)
}
