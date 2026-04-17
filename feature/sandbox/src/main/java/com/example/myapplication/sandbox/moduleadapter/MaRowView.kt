package com.example.myapplication.sandbox.moduleadapter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatTextView
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.AbsModuleView

@Keep
data class MaRowModel(val label: String)

class MaRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbsModuleView<MaRowModel>(context, attrs) {

    private val textView = AppCompatTextView(context)

    init {
        addView(textView, LayoutParams.MATCH_PARENT, 48.dp(context))
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setPadding(16.dp(context), 0, 16.dp(context), 0)
        textView.setBackgroundColor(Color.parseColor("#E8EAF6"))
        textView.setTextColor(Color.parseColor("#283593"))
    }

    override fun onChanged(model: MaRowModel) {
        super.onChanged(model)
        textView.text = model.label
    }
}
