package com.example.myapplication.ext

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText

fun EditText.isPasswordVisible(): Boolean =
    inputType.and(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) ==
        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

fun EditText.setPasswordVisible(isVisible: Boolean) {
    inputType = if (isVisible) {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    } else {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }
    setSelection(text.length)
}

inline fun EditText.addTextChanged(
    crossinline afterChanged: (Editable?) -> Unit = {},
    crossinline beforeChanged: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ -> },
    crossinline onChanged: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ -> },
) {
    val listener =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = afterChanged(s)

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                beforeChanged(s, start, count, after)

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) =
                onChanged(s, start, before, count)
        }
    addTextChangedListener(listener)
}

inline fun EditText.afterTextChanged(crossinline afterChanged: (Editable?) -> Unit = {}) {
    addTextChanged(afterChanged = afterChanged)
}

inline fun EditText.beforeTextChanged(
    crossinline beforeChanged: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ -> },
) {
    addTextChanged(beforeChanged = beforeChanged)
}

inline fun EditText.onTextChanged(
    crossinline onChanged: (CharSequence?, Int, Int, Int) -> Unit = { _, _, _, _ -> },
) {
    addTextChanged(onChanged = onChanged)
}

fun EditText.setTextWithDecimalLimit(text: String, decimalDigits: Int = 3) {
    setText(text.formatToDecimalLimit(decimalDigits))
    setSelection(this.text.length)
}

fun String.formatToDecimalLimit(decimalDigits: Int): String {
    if (isEmpty()) return this

    val dotIndex = indexOf('.')
    if (dotIndex == -1) return this

    val integerPart = substring(0, dotIndex)
    val decimalPart =
        if (length > dotIndex + 1) {
            substring(dotIndex + 1).let {
                if (it.length > decimalDigits) it.substring(0, decimalDigits) else it
            }
        } else {
            ""
        }

    return if (decimalPart.isEmpty()) integerPart else "$integerPart.$decimalPart"
}
