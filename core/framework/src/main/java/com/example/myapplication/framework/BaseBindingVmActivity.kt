package com.example.myapplication.framework

import android.os.Bundle
import androidx.databinding.ViewDataBinding

/**
 * 带 [BaseViewModel] 的 DataBinding Activity。
 *
 * 子类提供 [viewModel] 与 [viewModelBrId]（各模块 `BR.*`，与布局 `<variable name="vm" …>` 对应），
 * 使用 [ViewDataBinding.setVariable] 绑定，无反射。
 *
 * 各业务模块可再包一层「薄基类」固定 `viewModelBrId = BR.vm`（见 `:app`、`:feature:game` 等模块实现），
 * 具体页面只需 `override val viewModel by viewModels()`。
 *
 * [bindBaseViewModel] 内的 Flow 收集已在 `bindBaseViewModelUi` 中按
 * [androidx.lifecycle.Lifecycle.State.STARTED] 延迟。
 */
abstract class BaseBindingVmActivity<VB : ViewDataBinding, VM : BaseViewModel> : BaseBindingActivity<VB>() {

    protected abstract val viewModel: VM

    /** 布局 `<variable name="vm" …>` 时填本模块 DataBinding 生成的 `BR.vm`。 */
    protected abstract val viewModelBrId: Int

    protected open fun bindViewModelToBinding(vm: VM) {
        binding.setVariable(viewModelBrId, vm)
    }

    /**
     * 在布局绑定、[bindViewModelToBinding]、[bindBaseViewModel] 之后调用。
     * 较重 UI 可再包一层 `binding.root.post { … }`。
     */
    protected open fun onVmBound(savedInstanceState: Bundle?) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindViewModelToBinding(viewModel)
        bindBaseViewModel(viewModel, this, this)
        onVmBound(savedInstanceState)
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
