package com.example.myapplication.mall.adapter

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import coil.load
import coil.imageLoader
import com.example.myapplication.mall.R
import com.example.myapplication.mall.databinding.ItemMallCatalogRowBinding
import com.tory.module_adapter.views.AbsModuleView

class MallCatalogRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val onToggleCart: (Int) -> Unit,
    private val onOpenDetail: (MallCatalogRowModel, ImageView) -> Unit,
) : AbsModuleView<MallCatalogRowModel>(context, attrs) {

    private var lastCoverUrl: String? = null

    private val binding: ItemMallCatalogRowBinding by lazy(LazyThreadSafetyMode.NONE) {
        val rootChild = getChildAt(0)
        checkNotNull(DataBindingUtil.bind<ItemMallCatalogRowBinding>(rootChild)) {
            "item_mall_catalog_row DataBinding 绑定失败"
        }
    }

    override fun getLayoutId(): Int = R.layout.item_mall_catalog_row

    override fun onChanged(model: MallCatalogRowModel) {
        super.onChanged(model)
        binding.item = model
        binding.executePendingBindings()
        if (lastCoverUrl != model.coverUrl) {
            lastCoverUrl = model.coverUrl
            binding.cover.load(model.coverUrl, context.imageLoader) { crossfade(false) }
        }
        ViewCompat.setTransitionName(binding.cover, "mall_cover_${model.postId}")
        binding.actionCart.text = context.getString(
            if (model.inCart) R.string.mall_row_remove_cart else R.string.mall_row_add_cart,
        )
        binding.actionCart.setOnClickListener { onToggleCart(model.postId) }
        binding.root.setOnClickListener { onOpenDetail(model, binding.cover) }
    }
}
