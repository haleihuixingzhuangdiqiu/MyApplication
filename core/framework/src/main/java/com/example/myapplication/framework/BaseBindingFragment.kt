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

/**
 * 仅 DataBinding + 生命周期的 Fragment 基类；无 ViewModel 时使用。
 * 需要 Hilt 时在子类标注 [@dagger.hilt.android.AndroidEntryPoint]。
 *
 * [enablePageOverlay] 为 true 时，在业务布局外包一层 [android.widget.FrameLayout]，用于全屏遮罩（见 [BaseBindingActivity] 说明）。
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
