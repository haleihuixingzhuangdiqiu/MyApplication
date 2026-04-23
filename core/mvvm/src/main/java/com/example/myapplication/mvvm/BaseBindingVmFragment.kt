package com.example.myapplication.mvvm

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.example.myapplication.mvvm.BaseViewModel

/**
 * 带 [com.example.myapplication.mvvm.BaseViewModel] 的 DataBinding [Fragment]。
 *
 * 与 [BaseBindingVmActivity] 对称：在 [onViewCreated] 里 `setVariable`、[bindBaseViewModel]（内部 [bindBaseViewModelUi]），
 * 从而在本 Fragment 生命周期内收 [com.example.myapplication.mvvm.BaseViewModel.userMessage] 与 [com.example.myapplication.mvvm.BaseViewModel.pageOverlay]。
 * [viewModelBrId] 为各模块 `BR.xxx`；[handlePageOverlayRetry] 与 Activity 版相同，优先 [onPageOverlayRetry]，否则 [viewModel.onPageOverlayRetry]。
 */
abstract class BaseBindingVmFragment<VB : ViewDataBinding, VM : BaseViewModel> : BaseBindingFragment<VB>() {

    protected abstract val viewModel: VM

    protected abstract val viewModelBrId: Int

    protected open fun bindViewModelToBinding(vm: VM) {
        binding.setVariable(viewModelBrId, vm)
    }

    protected open fun onVmBound(view: View, savedInstanceState: Bundle?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModelToBinding(viewModel)
        bindBaseViewModel(viewModel)
        onVmBound(view, savedInstanceState)
    }

    override fun handlePageOverlayRetry() {
        val cb = onPageOverlayRetry
        if (cb != null) {
            cb()
        } else {
            viewModel.onPageOverlayRetry()
        }
    }
}
