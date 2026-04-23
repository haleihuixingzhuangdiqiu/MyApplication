package com.example.myapplication.mall

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import coil.load
import coil.imageLoader
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.mvvm.BaseUiActivity
import com.example.myapplication.mall.adapter.MallCatalogRowModel
import com.example.myapplication.mall.databinding.ActivityMallItemDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RoutePaths.MALL_ITEM_DETAIL)
@AndroidEntryPoint
class MallItemDetailActivity : BaseUiActivity() {

    private lateinit var binding: ActivityMallItemDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMallItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindMaterialToolbar(binding.toolbar) { supportFinishAfterTransition() }

        val tn = intent.getStringExtra(EXTRA_TRANSITION_NAME).orEmpty()
        if (tn.isNotEmpty()) {
            ViewCompat.setTransitionName(binding.hero, tn)
        }

        supportActionBar?.title = intent.getStringExtra(EXTRA_PAGE_TITLE).orEmpty()

        val coverUrl = intent.getStringExtra(EXTRA_COVER_URL).orEmpty()
        binding.hero.load(coverUrl, binding.hero.context.imageLoader) {
            crossfade(false)
        }
        binding.textTitle.text = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        binding.textSubtitle.text = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
        binding.textPrice.text = intent.getStringExtra(EXTRA_PRICE).orEmpty()
    }

    companion object {
        const val EXTRA_TRANSITION_NAME = "extra_transition_name"
        const val EXTRA_COVER_URL = "extra_cover_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_PAGE_TITLE = "extra_page_title"

        fun createIntent(context: Context, model: MallCatalogRowModel, heroTransitionName: String): Intent =
            Intent(context, MallItemDetailActivity::class.java).apply {
                putExtra(EXTRA_TRANSITION_NAME, heroTransitionName)
                putExtra(EXTRA_COVER_URL, model.coverUrl)
                putExtra(EXTRA_TITLE, model.title)
                putExtra(EXTRA_SUBTITLE, model.subtitle)
                putExtra(EXTRA_PRICE, model.priceLabel)
                putExtra(EXTRA_PAGE_TITLE, context.getString(R.string.mall_item_detail_title))
            }

        fun startWithHero(
            activity: FragmentActivity,
            model: MallCatalogRowModel,
            heroView: ImageView,
        ) {
            val tn = ViewCompat.getTransitionName(heroView)
                ?: "mall_cover_${model.postId}".also { ViewCompat.setTransitionName(heroView, it) }
            val opts = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, heroView, tn).toBundle()
            activity.startActivity(createIntent(activity, model, tn), opts)
        }
    }
}
