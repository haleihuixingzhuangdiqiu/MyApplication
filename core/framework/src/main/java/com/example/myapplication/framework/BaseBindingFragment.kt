package com.example.myapplication.framework

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.mvvm.BaseViewModel

/**
 * **DataBinding 根** + 生命周期的 `Fragment` 基类；**无** [com.example.myapplication.mvvm.BaseViewModel] 时用本类。
 *
 * ## 与 [BaseBindingActivity] 的差异
 * - `Activity` 可把蒙层直接加到 [android.R.id.content]；`Fragment` 没有等价单根，故在 [onCreateView] 里用 [android.widget.FrameLayout]
 *   包住业务 [binding.root]，再叠一层 [R.layout.framework_page_overlay] 和 [PageOverlayHost]。
 * - [enablePageOverlay] 为 `false` 时直接返回业务根视图，不包 [FrameLayout]，适合确定不需要整页态的子页。
 *
 * [binding] 在 [onDestroyView] 置 `null`，避免泄漏；[bindBaseViewModel] 使用 [getViewLifecycleOwner] 与 [requireContext] 收集流。
 * 要接 ViewModel 请用 [BaseBindingVmFragment]。
 */
abstract class BaseBindingFragment<VB : ViewDataBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    @get:LayoutRes
    protected abstract val layoutId: Int

    protected open val enablePageOverlay: Boolean = true

    var onPageOverlayRetry: (() -> Unit)? = null

    private var pageOverlayHost: PageOverlayHost? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        if (!enablePageOverlay) {
            return binding.root
        }
        // Fragment 无法像 Activity 那样直接挂到 android.R.id.content，
        // 因此这里在业务根布局外再包一层 FrameLayout，叠加统一遮罩层。
        val wrapper = FrameLayout(inflater.context)
        wrapper.addView(
            binding.root,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        val overlay = inflater.inflate(R.layout.framework_page_overlay, wrapper, false)
        wrapper.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        pageOverlayHost = PageOverlayHost(overlay) { handlePageOverlayRetry() }
        return wrapper
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onDestroyView() {
        pageOverlayHost = null
        super.onDestroyView()
        _binding = null
    }

    protected open fun handlePageOverlayRetry() {
        onPageOverlayRetry?.invoke()
    }

    protected open fun bindBaseViewModel(vm: BaseViewModel) {
        bindBaseViewModel(vm, viewLifecycleOwner, requireContext())
    }

    protected open fun bindBaseViewModel(vm: BaseViewModel, lifecycleOwner: LifecycleOwner, context: Context) {
        bindBaseViewModelUi(lifecycleOwner, context, vm, pageOverlayHost)
    }
}
