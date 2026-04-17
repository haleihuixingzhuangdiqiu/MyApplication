package com.example.myapplication.framework

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding

/**
 * 带 [BaseViewModel] 的 DataBinding Fragment；通过 [viewModelBrId] + [ViewDataBinding.setVariable] 绑定，无反射。
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
