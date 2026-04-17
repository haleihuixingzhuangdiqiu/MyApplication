package com.example.myapplication

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.common.MainTabExtras
import com.example.myapplication.common.MainToolbarHost
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.game.GameMainFragment
import com.example.myapplication.mall.MallMainFragment
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.session.SessionRepository
import com.example.myapplication.social.SocialMainFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * 「单壳 + BottomNav + 多 Fragment」门户；由 [SplashActivity] 冷启动进入。
 *
 * Tab 切换采用 **add + show/hide + 固定 tag**。
 */
@AndroidEntryPoint
class MainActivity : AppBindingVmActivity<ActivityMainBinding, MainViewModel>(), MainToolbarHost {

    override val layoutId: Int = R.layout.activity_main

    /** 门户壳不用 Activity 级 [PageOverlayHost]：子 Fragment 自带遮罩。 */
    override val enablePageOverlay: Boolean = false

    override val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sessionRepository: SessionRepository

    override fun onVmBound(savedInstanceState: Bundle?) {
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.main_nav_profile && !sessionRepository.state.value.isLoggedIn) {
                ARouter.getInstance().build(RoutePaths.LOGIN).navigation(this)
                false
            } else {
                showMainTab(item.itemId)
                true
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateTo.collect { event ->
                    event.getContentIfNotHandled()?.let { path ->
                        ARouter.getInstance().build(path).navigation(this@MainActivity)
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            val target = extraToNavId(intent.getStringExtra(MainTabExtras.EXTRA_TAB))
                ?: R.id.main_nav_game
            binding.bottomNav.selectedItemId = target
            if (!hasAnyMainTabFragment()) {
                showMainTab(target)
            }
        }
    }

    override fun setMainToolbarTitle(title: CharSequence) {
        // 主壳无顶栏；独立壳仍走 [FeatureStandaloneToolbarHost]。
    }

    private fun hasAnyMainTabFragment(): Boolean =
        MAIN_TAB_TAGS.any { supportFragmentManager.findFragmentByTag(it) != null }

    private fun showMainTab(menuItemId: Int) {
        val fm = supportFragmentManager
        val tag = fragmentTagFor(menuItemId)
        val tx = fm.beginTransaction().setReorderingAllowed(true)
        for (t in MAIN_TAB_TAGS) {
            if (t == tag) continue
            fm.findFragmentByTag(t)?.let { hidden ->
                tx.hide(hidden)
                tx.setMaxLifecycle(hidden, Lifecycle.State.CREATED)
            }
        }
        var target: Fragment? = fm.findFragmentByTag(tag)
        if (target == null) {
            target = createMainTabFragment(menuItemId)
            tx.add(R.id.main_fragment_container, target, tag)
        } else {
            tx.show(target)
        }
        target?.let { visible ->
            tx.setMaxLifecycle(visible, Lifecycle.State.RESUMED)
        }
        tx.commitNow()
    }

    private fun fragmentTagFor(menuItemId: Int): String = when (menuItemId) {
        R.id.main_nav_game -> TAG_TAB_GAME
        R.id.main_nav_social -> TAG_TAB_SOCIAL
        R.id.main_nav_mall -> TAG_TAB_MALL
        R.id.main_nav_profile -> TAG_TAB_PROFILE
        else -> TAG_TAB_GAME
    }

    private fun createMainTabFragment(menuItemId: Int): Fragment = when (menuItemId) {
        R.id.main_nav_game -> GameMainFragment.newEmbeddedInMain()
        R.id.main_nav_social -> SocialMainFragment.newEmbeddedInMain()
        R.id.main_nav_mall -> MallMainFragment.newEmbeddedInMain()
        R.id.main_nav_profile -> ProfileFragment()
        else -> GameMainFragment.newEmbeddedInMain()
    }

    private companion object {
        const val TAG_TAB_GAME = "main_tab_game"
        const val TAG_TAB_SOCIAL = "main_tab_social"
        const val TAG_TAB_MALL = "main_tab_mall"
        const val TAG_TAB_PROFILE = "main_tab_profile"

        val MAIN_TAB_TAGS = arrayOf(TAG_TAB_GAME, TAG_TAB_SOCIAL, TAG_TAB_MALL, TAG_TAB_PROFILE)
    }

    private fun extraToNavId(extra: String?): Int? = when (extra) {
        MainTabExtras.TAB_GAME,
        MainTabExtras.TAB_HOME,
        -> R.id.main_nav_game
        MainTabExtras.TAB_SOCIAL,
        -> R.id.main_nav_social
        MainTabExtras.TAB_MALL,
        -> R.id.main_nav_mall
        MainTabExtras.TAB_PROFILE -> R.id.main_nav_profile
        else -> null
    }
}
