package com.example.myapplication.game

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
import com.example.myapplication.framework.BaseUiActivity
import com.example.myapplication.game.adapter.GamePostRowModel
import com.example.myapplication.game.databinding.ActivityGamePostDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RoutePaths.GAME_POST_DETAIL)
@AndroidEntryPoint
class GamePostDetailActivity : BaseUiActivity() {

    private lateinit var binding: ActivityGamePostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePostDetailBinding.inflate(layoutInflater)
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
        binding.textBody.text = intent.getStringExtra(EXTRA_BODY).orEmpty()
    }

    companion object {
        const val EXTRA_TRANSITION_NAME = "extra_transition_name"
        const val EXTRA_COVER_URL = "extra_cover_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_PAGE_TITLE = "extra_page_title"

        fun createIntent(context: Context, model: GamePostRowModel, heroTransitionName: String): Intent =
            Intent(context, GamePostDetailActivity::class.java).apply {
                putExtra(EXTRA_TRANSITION_NAME, heroTransitionName)
                putExtra(EXTRA_COVER_URL, model.coverImageUrl)
                putExtra(EXTRA_TITLE, model.title)
                putExtra(EXTRA_BODY, model.bodyPreview)
                putExtra(EXTRA_PAGE_TITLE, context.getString(R.string.game_post_detail_title))
            }

        fun startWithHero(
            activity: FragmentActivity,
            model: GamePostRowModel,
            heroView: ImageView,
        ) {
            val tn = ViewCompat.getTransitionName(heroView)
                ?: "game_cover_${model.postId}".also { ViewCompat.setTransitionName(heroView, it) }
            val opts = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, heroView, tn).toBundle()
            activity.startActivity(createIntent(activity, model, tn), opts)
        }
    }
}
