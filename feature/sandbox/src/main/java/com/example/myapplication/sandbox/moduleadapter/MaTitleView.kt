package com.example.myapplication.sandbox.moduleadapter

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.AbsModuleView

@Keep
data class MaTitleModel(val text: String)

class MaTitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbsModuleView<MaTitleModel>(context, attrs) {

    private val textView = AppCompatTextView(context)

    init {
        val h = 12.dp(context)
        addView(textView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        textView.setPadding(16.dp(context), h, 16.dp(context), 8.dp(context))
        textView.textSize = 16f
        textView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        textView.typeface = Typeface.DEFAULT_BOLD
    }

    override fun onChanged(model: MaTitleModel) {
        super.onChanged(model)
        textView.text = model.text
    }
}
