package com.example.myapplication.framework

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.example.myapplication.mvvm.BaseViewModel

/**
 * 带 [com.example.myapplication.mvvm.BaseViewModel] 的 **DataBinding Activity**。
 *
 * ## 子类要提供什么
 * - [viewModel]：通过 `by viewModels()` / Hilt 等注入的页面级 ViewModel，必须继承 [com.example.myapplication.mvvm.BaseViewModel]。
 * - [viewModelBrId]：各 feature 模块 DataBinding 生成的 `BR.xxx`（如 `BR.vm`），与布局里 `<variable name="vm" type="YourViewModel" />` 一致；
 *   基类在 `onCreate` 里执行 `binding.setVariable(viewModelBrId, viewModel)`，**无反射**。
 *
 * ## 与「薄基类」模式
 * 各业务模块可再包一层只固定 `viewModelBrId` 的抽象类（如 `AppBindingVmActivity`、`GameBindingVmActivity`），页面只写 `override val viewModel`。
 *
 * ## UI 行为（与无 VM 的 [BaseBindingActivity] 多出来的部分）
 * 在 [onCreate] 中顺序为：`bindViewModelToBinding` → [bindBaseViewModel]（内部为 [bindBaseViewModelUi]）：
 * - 在 [androidx.lifecycle.Lifecycle.State.STARTED] 下收集 [com.example.myapplication.mvvm.BaseViewModel.userMessage] 弹 Toast、收集 [com.example.myapplication.mvvm.BaseViewModel.pageOverlay] 驱动 [PageOverlayHost]（若 [BaseBindingActivity.enablePageOverlay] 为 true）。
 * - [onVmBound] 在以上完成之后调用，可安全访问已在布局里绑好的 `vm`。
 *
 * 重试逻辑见 [BaseBindingActivity.onPageOverlayRetry] / [handlePageOverlayRetry] 说明；本类在 [handlePageOverlayRetry] 中优先调页面 lambda，否则 [viewModel.onPageOverlayRetry]。
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
