package com.example.myapplication.framework

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

/**
 * 仅 DataBinding + 生命周期的 Activity 基类；无 ViewModel 时使用。
 * 子类勿覆写 [BaseUiActivity] 的 `standaloneShellLayoutId` / `standaloneToolbarId`（由本类 inflate 根布局）。
 * 若使用 Hilt，在子类标注 [@dagger.hilt.android.AndroidEntryPoint]。
 * 需要 ViewModel 时继承 [BaseBindingVmActivity]。
 *
 * 页面级全屏遮罩（加载 / 空 / 错 + 可选重试）：
 * - [enablePageOverlay] 为 true 时挂到 [android.R.id.content] 最上层，与 [BaseViewModel.pageOverlay] 自动同步。
 * - 需要重试时：在 ViewModel 中 [BaseViewModel.onPageOverlayRetry]，或在本类设置 [onPageOverlayRetry] 回调（优先于 ViewModel）。
 */
abstract class BaseBindingActivity<VB : ViewDataBinding> : BaseUiActivity() {

    protected lateinit var binding: VB
        private set

    @get:LayoutRes
    protected abstract val layoutId: Int

    /** 为 false 时不挂载全屏遮罩（仍可通过 [BaseViewModel.pageOverlay] 自行处理）。 */
    protected open val enablePageOverlay: Boolean = true

    /**
     * 错误遮罩显示「重试」且被点击时调用；若非 null，则不再调用 [BaseViewModel.onPageOverlayRetry]。
     * 若既要回调又要 ViewModel 逻辑，请在 lambda 内自行调用 `viewModel.onPageOverlayRetry()` 等。
     */
    var onPageOverlayRetry: (() -> Unit)? = null

    private var pageOverlayHost: PageOverlayHost? = null

    override fun setContentView(layoutResID: Int) {
        throw UnsupportedOperationException("使用 DataBinding，勿直接调用 setContentView(int)")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 不可使用 DataBindingUtil.setContentView(activity, int)：其内部会调用 setContentView(int)，与上方防误用重写冲突
        binding = DataBindingUtil.inflate(layoutInflater, layoutId, null, false)
        setContentView(binding.root)
        if (enablePageOverlay) {
            pageOverlayHost = PageOverlayHost.attachToActivityContent(this) { handlePageOverlayRetry() }
        }
        binding.lifecycleOwner = this
    }

    protected open fun handlePageOverlayRetry() {
        onPageOverlayRetry?.invoke()
    }

    /**
     * 绑定 [BaseViewModel] 的通用 UI：Toast、[messageFlow]、全屏遮罩等。
     * Fragment 请使用 [BaseBindingFragment.bindBaseViewModel]。
     */
    protected open fun bindBaseViewModel(vm: BaseViewModel) {
        bindBaseViewModel(vm, this, this)
    }

    protected open fun bindBaseViewModel(vm: BaseViewModel, lifecycleOwner: LifecycleOwner, context: Context) {
        bindBaseViewModelUi(lifecycleOwner, context, vm, pageOverlayHost)
    }
}
