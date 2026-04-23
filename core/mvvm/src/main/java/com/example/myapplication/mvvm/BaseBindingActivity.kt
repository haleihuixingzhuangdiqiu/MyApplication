package com.example.myapplication.mvvm

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.mvvm.BaseViewModel

/**
 * **DataBinding 根布局 + 生命周期**的 Activity 基类；**无** `ViewModel` 绑定时用本类即可。
 *
 * ## 与 [BaseUiActivity] 的关系
 * 继承链：[BaseBindingActivity] → [BaseUiActivity]。本类在 [onCreate] 里用 [androidx.databinding.DataBindingUtil.inflate] 得到 [binding]，
 * 并调用 `setContentView(binding.root)`；因此**禁止**再直接 `setContentView(int)`（基类已重写为抛异常防误用）。
 *
 * ## 与「独立 Toolbar 壳」互斥
 * 不要同时覆写 [BaseUiActivity.standaloneShellLayoutId] / [BaseUiActivity.standaloneToolbarId] 与这里的 [layoutId]——两套都是根布局方案，二选一。
 *
 * ## 整页蒙层（与 [com.example.myapplication.mvvm.BaseViewModel.pageOverlay]）
 * - [enablePageOverlay] 为 `true`（默认）时，在 [android.R.id.content] 上叠一层 [PageOverlayHost]，与 [com.example.myapplication.mvvm.BaseViewModel] 里
 *   [showPageLoading] / [showPageEmpty] / [showPageError] / [showPageContent] 驱动状态同步。
 * - 重试：若设置了 [onPageOverlayRetry]，**只调**该 lambda，**不再**调 [com.example.myapplication.mvvm.BaseViewModel.onPageOverlayRetry]；
 *   若 lambda 为 `null`，则转发到 ViewModel 的 `onPageOverlayRetry`。
 *
 * ## 需要 ViewModel
 * 请继承 [BaseBindingVmActivity]：会多走 [bindBaseViewModel] / `bindBaseViewModelUi`，自动接 [userMessage] 与 `pageOverlay`。
 */
abstract class BaseBindingActivity<VB : ViewDataBinding> : BaseUiActivity() {

    protected lateinit var binding: VB
        private set

    @get:LayoutRes
    protected abstract val layoutId: Int

    /** 为 false 时不挂载全屏遮罩（仍可通过 [com.example.myapplication.mvvm.BaseViewModel.pageOverlay] 自行处理）。 */
    protected open val enablePageOverlay: Boolean = true

    /**
     * 错误遮罩显示「重试」且被点击时调用；若非 null，则不再调用 [com.example.myapplication.mvvm.BaseViewModel.onPageOverlayRetry]。
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
     * 绑定 [com.example.myapplication.mvvm.BaseViewModel] 的通用 UI：[com.example.myapplication.mvvm.BaseViewModel.userMessage] 与全屏遮罩等。
     * Fragment 请使用 [BaseBindingFragment.bindBaseViewModel]。
     */
    protected open fun bindBaseViewModel(vm: BaseViewModel) {
        bindBaseViewModel(vm, this, this)
    }

    protected open fun bindBaseViewModel(vm: BaseViewModel, lifecycleOwner: LifecycleOwner, context: Context) {
        bindBaseViewModelUi(lifecycleOwner, context, vm, pageOverlayHost)
    }
}
