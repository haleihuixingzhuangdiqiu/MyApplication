package com.example.myapplication.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.common.MainFragmentArgs
import com.example.myapplication.framework.disableItemChangeAnimations
import com.example.myapplication.framework.publishToolbarTitle
import com.example.myapplication.framework.showStandaloneToolbarBackIfHost
import com.example.myapplication.framework.adapter.setItemsWithStableItemDiff
import com.example.myapplication.game.adapter.GameMetaHintModel
import com.example.myapplication.game.adapter.GameMetaHintView
import com.example.myapplication.game.adapter.GamePostRowModel
import com.example.myapplication.game.adapter.GamePostRowView
import com.example.myapplication.game.adapter.GameSectionTitleModel
import com.example.myapplication.game.adapter.GameSectionTitleView
import com.example.myapplication.game.databinding.FragmentGameBinding
import com.tory.module_adapter.base.ItemSpace
import com.tory.module_adapter.base.NormalModuleAdapter
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.ModuleGroupSectionModel
import com.tory.module_adapter.views.ModuleSpaceModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GameMainFragment : GameBindingVmFragment<FragmentGameBinding, GameViewModel>() {

    override val layoutId: Int = R.layout.fragment_game

    override val viewModel: GameViewModel by viewModels()

    private val postListAdapter = NormalModuleAdapter(calDiff = true)

    private var tabSyncFromVm = false

    override fun onVmBound(view: View, savedInstanceState: Bundle?) {
        setupTabs()
        setupBannerPager()
        registerPostListViews()
        binding.postList.layoutManager = postListAdapter.getGridLayoutManager(requireContext())
        binding.postList.adapter = postListAdapter
        binding.postList.disableItemChangeAnimations()

        showStandaloneToolbarBackIfHost()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.screenTitle.collect { title ->
                        publishToolbarTitle(title)
                    }
                }
                launch {
                    viewModel.selectedTabIndex.collect { index ->
                        val tab = binding.gameTabLayout.getTabAt(index) ?: return@collect
                        if (binding.gameTabLayout.selectedTabPosition != index) {
                            tabSyncFromVm = true
                            tab.select()
                            tabSyncFromVm = false
                        }
                    }
                }
                launch {
                    viewModel.feedRows.collect { rows ->
                        @Suppress("UNCHECKED_CAST")
                        postListAdapter.setItemsWithStableItemDiff(
                            newItems = rows as List<Any>,
                            stableItemId = { item -> feedStableId(item) },
                        )
                    }
                }
                launch {
                    viewModel.loading.collect { show -> binding.swipe.isRefreshing = show }
                }
                launch {
                    viewModel.banners.collect { list ->
                        binding.bannerPager.adapter = GameBannerPagerAdapter(list) { }
                        updateBannerIndex(binding.bannerPager.currentItem, list.size)
                    }
                }
            }
        }
        binding.swipe.setOnRefreshListener { viewModel.refreshPosts() }

        binding.gameTabLayout.addOnTabSelectedListener(
            object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                    if (tabSyncFromVm) return
                    val i = tab?.position ?: return
                    viewModel.selectTab(i)
                }

                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}

                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            },
        )
    }

    private fun setupTabs() {
        binding.gameTabLayout.removeAllTabs()
        val labels = resources.getStringArray(R.array.game_tab_labels)
        labels.forEach { label ->
            binding.gameTabLayout.addTab(binding.gameTabLayout.newTab().setText(label))
        }
    }

    private fun setupBannerPager() {
        binding.bannerPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val n = binding.bannerPager.adapter?.itemCount ?: 0
                    updateBannerIndex(position, n)
                }
            },
        )
    }

    private fun updateBannerIndex(position: Int, total: Int) {
        if (total <= 0) {
            binding.bannerPageIndex.text = ""
            return
        }
        binding.bannerPageIndex.text = getString(R.string.game_banner_index, position + 1, total)
    }

    private fun registerPostListViews() {
        postListAdapter.register { GameMetaHintView(it.context) }
        postListAdapter.register { GameSectionTitleView(it.context) }
        postListAdapter.register(
            gridSize = 2,
            itemSpace = ItemSpace(
                spaceH = 8.dp(requireContext()),
                spaceV = 8.dp(requireContext()),
                edgeH = 12.dp(requireContext()),
            ),
        ) {
            GamePostRowView(it.context) { model, cover ->
                GamePostDetailActivity.startWithHero(requireActivity(), model, cover)
            }
        }
    }

    private fun feedStableId(item: Any): Any = when (item) {
        is GameMetaHintModel -> "meta_hint"
        is GameSectionTitleModel -> "section:${item.sectionId}"
        is GamePostRowModel -> "post:${item.postId}"
        is ModuleSpaceModel -> "space:${item.tag}:${item.height}"
        is ModuleGroupSectionModel -> "group:${item.tag}"
        else -> item::class.java.name + "@" + item.hashCode()
    }

    companion object {

        fun newEmbeddedInMain(): GameMainFragment =
            GameMainFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(MainFragmentArgs.EMBEDDED_IN_MAIN, true)
                }
            }
    }
}
