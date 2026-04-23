package com.example.myapplication.framework

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.mvvm.BaseViewModel
import kotlinx.coroutines.launch

/**
 * 把 [com.example.myapplication.mvvm.BaseViewModel] 的两类输出接到界面：
 * - [com.example.myapplication.mvvm.BaseViewModel.pageOverlay] → [PageOverlayHost.render]（传 `null` 则仅不挂蒙层、由页面自行写 `when` 亦可）。
 * - [com.example.myapplication.mvvm.BaseViewModel.userMessage] → 简短 [Toast]。
 *
 * 使用 `repeatOnLifecycle(Lifecycle.State.STARTED)`，避免后台页面仍收 Toast、浪费电量；
 * 每个 `Flow` 单独 `launch`，并行收集、互不阻塞。
 *
 * **仅** [BaseBindingActivity.bindBaseViewModel] / [BaseBindingFragment.bindBaseViewModel] 会调用，业务侧勿直接调（`internal`）。
 */
internal fun bindBaseViewModelUi(
    lifecycleOwner: LifecycleOwner,
    context: Context,
    vm: BaseViewModel,
    pageOverlayHost: PageOverlayHost? = null,
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (pageOverlayHost != null) {
                launch {
                    vm.pageOverlay.collect { state -> pageOverlayHost.render(state) }
                }
            }
            launch {
                vm.userMessage.collect { ev ->
                    ev.getContentIfNotHandled()?.let { text ->
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
