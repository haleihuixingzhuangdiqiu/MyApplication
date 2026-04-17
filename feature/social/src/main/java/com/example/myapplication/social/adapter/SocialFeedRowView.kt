package com.example.myapplication.social.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import coil.load
import coil.imageLoader
import com.example.myapplication.social.R
import com.example.myapplication.social.SearchHighlight
import com.example.myapplication.social.databinding.ItemSocialFeedRowBinding
import com.tory.module_adapter.views.AbsModuleView

class SocialFeedRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val searchHighlightColor: Int,
    private val onToggleFollow: (Int) -> Unit,
    private val onOpenDetail: (SocialFeedRowModel, ImageView) -> Unit,
) : AbsModuleView<SocialFeedRowModel>(context, attrs) {

    private val binding: ItemSocialFeedRowBinding by lazy(LazyThreadSafetyMode.NONE) {
        val rootChild = getChildAt(0)
        checkNotNull(DataBindingUtil.bind<ItemSocialFeedRowBinding>(rootChild)) {
            "item_social_feed_row DataBinding 绑定失败"
        }
    }

    private var lastCoverUrl: String? = null

    override fun getLayoutId(): Int = R.layout.item_social_feed_row

    override fun onChanged(model: SocialFeedRowModel) {
        super.onChanged(model)
        binding.item = model
        binding.executePendingBindings()

        binding.title.text = SearchHighlight.build(model.title, model.searchQuery, searchHighlightColor)
        binding.anchorLine.text = SearchHighlight.build(model.anchorLine, model.searchQuery, searchHighlightColor)

        if (lastCoverUrl != model.coverUrl) {
            lastCoverUrl = model.coverUrl
            binding.cover.load(model.coverUrl, context.imageLoader) {
                crossfade(false)
            }
        }

        ViewCompat.setTransitionName(binding.cover, "social_cover_${model.entryId}")

        binding.actionFollow.text = context.getString(
            if (model.isFollowed) R.string.social_row_unfollow else R.string.social_row_follow,
        )
        binding.actionFollow.setOnClickListener { onToggleFollow(model.entryId) }

        binding.root.setOnClickListener { onOpenDetail(model, binding.cover) }
    }
}
