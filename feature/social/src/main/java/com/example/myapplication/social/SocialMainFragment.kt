package com.example.myapplication.social

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.common.MainFragmentArgs
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.framework.disableItemChangeAnimations
import com.example.myapplication.framework.publishToolbarTitle
import com.example.myapplication.framework.showStandaloneToolbarBackIfHost
import com.example.myapplication.framework.adapter.setItemsWithStableItemDiff
import com.example.myapplication.social.adapter.SocialFeedRowModel
import com.example.myapplication.social.adapter.SocialFeedRowView
import com.example.myapplication.social.databinding.FragmentSocialBinding
import com.tory.module_adapter.base.NormalModuleAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SocialMainFragment : SocialBindingVmFragment<FragmentSocialBinding, SocialViewModel>() {

    override fun onResume() {
        super.onResume()
        viewModel.applyPendingFollowAfterLogin()
    }

    override val layoutId: Int = R.layout.fragment_social

    override val viewModel: SocialViewModel by viewModels()

    private val listAdapter = NormalModuleAdapter(calDiff = true)

    override fun onVmBound(view: View, savedInstanceState: Bundle?) {
        val highlightColor = ContextCompat.getColor(requireContext(), R.color.social_search_highlight)
        listAdapter.register {
            SocialFeedRowView(
                context = it.context,
                attrs = null,
                searchHighlightColor = highlightColor,
                onToggleFollow = { entryId -> viewModel.toggleFollow(entryId) },
                onOpenDetail = { model, cover ->
                    SocialFeedDetailActivity.startWithHero(requireActivity(), model, cover)
                },
            )
        }
        binding.feedList.layoutManager = listAdapter.getGridLayoutManager(requireContext())
        binding.feedList.adapter = listAdapter
        binding.feedList.disableItemChangeAnimations()

        binding.inputSearch.doAfterTextChanged { editable ->
            viewModel.setSearchQuery(editable?.toString().orEmpty())
        }

        showStandaloneToolbarBackIfHost()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.screenTitle.collect { title ->
                        publishToolbarTitle(title)
                    }
                }
                launch {
                    viewModel.statusHint.collect { binding.textHint.text = it }
                }
                launch {
                    viewModel.rows.collect { rows ->
                        @Suppress("UNCHECKED_CAST")
                        listAdapter.setItemsWithStableItemDiff(
                            newItems = rows as List<Any>,
                            stableItemId = { item -> (item as SocialFeedRowModel).entryId },
                        )
                    }
                }
                launch {
                    viewModel.loading.collect { show -> binding.swipe.isRefreshing = show }
                }
                launch {
                    viewModel.feedChromeVisible.collect { show ->
                        binding.storiesBar.visibility = if (show) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.requireLogin.collect {
                        ARouter.getInstance().build(RoutePaths.LOGIN).navigation(requireActivity())
                    }
                }
            }
        }
        binding.swipe.setOnRefreshListener { viewModel.refreshFeed() }
    }

    companion object {

        fun newEmbeddedInMain(): SocialMainFragment =
            SocialMainFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(MainFragmentArgs.EMBEDDED_IN_MAIN, true)
                }
            }
    }
}
