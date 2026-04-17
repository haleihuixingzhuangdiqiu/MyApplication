package com.example.myapplication.social

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import coil.load
import coil.imageLoader
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.framework.BaseUiActivity
import com.example.myapplication.social.adapter.SocialFeedRowModel
import com.example.myapplication.social.databinding.ActivitySocialFeedDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RoutePaths.SOCIAL_FEED_DETAIL)
@AndroidEntryPoint
class SocialFeedDetailActivity : BaseUiActivity() {

    private lateinit var binding: ActivitySocialFeedDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialFeedDetailBinding.inflate(layoutInflater)
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
        binding.textAnchor.text = intent.getStringExtra(EXTRA_ANCHOR).orEmpty()
    }

    companion object {
        const val EXTRA_TRANSITION_NAME = "extra_transition_name"
        const val EXTRA_COVER_URL = "extra_cover_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ANCHOR = "extra_anchor"
        const val EXTRA_PAGE_TITLE = "extra_page_title"

        fun createIntent(context: Context, model: SocialFeedRowModel, heroTransitionName: String): Intent =
            Intent(context, SocialFeedDetailActivity::class.java).apply {
                putExtra(EXTRA_TRANSITION_NAME, heroTransitionName)
                putExtra(EXTRA_COVER_URL, model.coverUrl)
                putExtra(EXTRA_TITLE, model.title)
                putExtra(EXTRA_ANCHOR, model.anchorLine)
                putExtra(EXTRA_PAGE_TITLE, context.getString(R.string.social_detail_title))
            }

        fun startWithHero(
            activity: FragmentActivity,
            model: SocialFeedRowModel,
            heroView: View,
        ) {
            val tn = ViewCompat.getTransitionName(heroView)
                ?: "social_cover_${model.entryId}".also { ViewCompat.setTransitionName(heroView, it) }
            val opts = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, heroView, tn).toBundle()
            activity.startActivity(createIntent(activity, model, tn), opts)
        }
    }
}
