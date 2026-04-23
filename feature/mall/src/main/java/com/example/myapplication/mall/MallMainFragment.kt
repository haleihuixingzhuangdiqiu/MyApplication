package com.example.myapplication.mall

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
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
import com.example.myapplication.mall.adapter.MallCatalogRowModel
import com.example.myapplication.mall.adapter.MallCatalogRowView
import com.example.myapplication.mall.databinding.FragmentMallBinding
import com.tory.module_adapter.base.NormalModuleAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MallMainFragment : MallBindingVmFragment<FragmentMallBinding, MallViewModel>() {

    override fun onResume() {
        super.onResume()
        viewModel.applyPendingCartAfterLogin()
    }

    override val layoutId: Int = R.layout.fragment_mall

    override val viewModel: MallViewModel by viewModels()

    private val listAdapter = NormalModuleAdapter(calDiff = true)

    override fun onVmBound(view: View, savedInstanceState: Bundle?) {
        listAdapter.register {
            MallCatalogRowView(
                context = it.context,
                onToggleCart = { postId -> viewModel.toggleCart(postId) },
                onOpenDetail = { model, cover ->
                    MallItemDetailActivity.startWithHero(requireActivity(), model, cover)
                },
            )
        }
        binding.itemList.layoutManager = listAdapter.getGridLayoutManager(requireContext())
        binding.itemList.adapter = listAdapter
        binding.itemList.disableItemChangeAnimations()

        showStandaloneToolbarBackIfHost()

        binding.btnMallLogin.setOnClickListener {
            ARouter.getInstance().build(RoutePaths.LOGIN).navigation(requireActivity())
        }
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
                            stableItemId = { item -> (item as MallCatalogRowModel).postId },
                        )
                    }
                }
                launch {
                    viewModel.loading.collect { show -> binding.swipe.isRefreshing = show }
                }
                launch {
                    viewModel.needsLoginGate.collect { need ->
                        binding.loginBanner.isVisible = need
                    }
                }
                launch {
                    viewModel.requireLogin.collect {
                        ARouter.getInstance().build(RoutePaths.LOGIN).navigation(requireActivity())
                    }
                }
            }
        }
        binding.swipe.setOnRefreshListener { viewModel.refreshCatalog() }
    }

    companion object {

        fun newEmbeddedInMain(): MallMainFragment =
            MallMainFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(MainFragmentArgs.EMBEDDED_IN_MAIN, true)
                }
            }
    }
}
