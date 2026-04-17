package com.example.myapplication.game.adapter

import android.content.Context
import android.util.AttributeSet
import com.example.myapplication.game.databinding.ItemGameMetaHintBinding
import com.tory.module_adapter.views.AbsModuleView

class GameMetaHintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbsModuleView<GameMetaHintModel>(context, attrs) {

    private val binding: ItemGameMetaHintBinding by lazy(LazyThreadSafetyMode.NONE) {
        ItemGameMetaHintBinding.bind(getChildAt(0))
    }

    override fun getLayoutId(): Int = com.example.myapplication.game.R.layout.item_game_meta_hint

    override fun onChanged(model: GameMetaHintModel) {
        super.onChanged(model)
        binding.hintText.text = model.text
    }
}
