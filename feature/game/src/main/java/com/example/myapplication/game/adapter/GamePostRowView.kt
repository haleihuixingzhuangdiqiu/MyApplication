package com.example.myapplication.game.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import coil.load
import coil.imageLoader
import com.example.myapplication.game.R
import com.example.myapplication.game.databinding.ItemGamePostModuleBinding
import com.tory.module_adapter.views.AbsModuleView

/**
 * 使用 ModuleAdapter 推荐方式：
 * - 通过 [getLayoutId] 直接 inflate 布局文件
 * - 行内使用 DataBinding 绑定文案
 * - 图片在 onChanged 中走全局 Coil ImageLoader；URL 未变时不重复 load，避免与 Diff 局部刷新叠加闪动
 * - 列表封面 [android.widget.ImageView] 与详情页 Hero 共用 [androidx.core.view.ViewCompat.setTransitionName]
 */
class GamePostRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val onOpenDetail: (GamePostRowModel, ImageView) -> Unit,
) : AbsModuleView<GamePostRowModel>(context, attrs) {

    private var lastCoverUrl: String? = null

    private val binding: ItemGamePostModuleBinding by lazy(LazyThreadSafetyMode.NONE) {
        val content = getChildAt(0)
        checkNotNull(DataBindingUtil.bind<ItemGamePostModuleBinding>(content)) {
            "item_game_post_module.xml DataBinding 绑定失败"
        }
    }

    override fun getLayoutId(): Int = R.layout.item_game_post_module

    override fun onChanged(model: GamePostRowModel) {
        super.onChanged(model)
        binding.item = model
        binding.executePendingBindings()
        if (lastCoverUrl != model.coverImageUrl) {
            lastCoverUrl = model.coverImageUrl
            binding.cover.load(model.coverImageUrl, context.imageLoader) {
                crossfade(false)
            }
        }
        ViewCompat.setTransitionName(binding.cover, "game_cover_${model.postId}")
        binding.root.setOnClickListener { onOpenDetail(model, binding.cover) }
    }
}
