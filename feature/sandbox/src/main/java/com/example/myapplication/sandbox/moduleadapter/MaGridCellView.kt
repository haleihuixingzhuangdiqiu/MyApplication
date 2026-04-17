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
data class MaGridCellModel(val index: Int)

class MaGridCellView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbsModuleView<MaGridCellModel>(context, attrs) {

    private val textView = AppCompatTextView(context)

    init {
        val h = 56.dp(context)
        addView(textView, LayoutParams.MATCH_PARENT, h)
        textView.gravity = Gravity.CENTER
        textView.setBackgroundColor(Color.parseColor("#C8E6C9"))
        textView.setTextColor(Color.parseColor("#1B5E20"))
    }

    override fun onChanged(model: MaGridCellModel) {
        super.onChanged(model)
        textView.text = "#${model.index}"
    }
}
