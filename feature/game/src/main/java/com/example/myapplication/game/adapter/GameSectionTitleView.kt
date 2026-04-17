package com.example.myapplication.game.adapter

import android.content.Context
import android.util.AttributeSet
import com.example.myapplication.game.databinding.ItemGameSectionTitleBinding
import com.tory.module_adapter.views.AbsModuleView

class GameSectionTitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbsModuleView<GameSectionTitleModel>(context, attrs) {

    private val binding: ItemGameSectionTitleBinding by lazy(LazyThreadSafetyMode.NONE) {
        ItemGameSectionTitleBinding.bind(getChildAt(0))
    }

    override fun getLayoutId(): Int = com.example.myapplication.game.R.layout.item_game_section_title

    override fun onChanged(model: GameSectionTitleModel) {
        super.onChanged(model)
        binding.title.text = model.title
        binding.subtitle.text = model.subtitle.orEmpty()
        binding.subtitle.visibility = if (model.subtitle.isNullOrBlank()) android.view.View.GONE else android.view.View.VISIBLE
    }
}
