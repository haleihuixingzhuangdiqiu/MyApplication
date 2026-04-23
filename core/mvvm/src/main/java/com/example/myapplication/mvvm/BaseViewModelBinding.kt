package com.example.myapplication.mvvm

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.hjq.toast.Toaster
import com.example.myapplication.common.toast.showError
import com.example.myapplication.common.toast.showInfo

/**
 * 把 [com.example.myapplication.mvvm.BaseViewModel] 的两类输出接到界面：
 * - [com.example.myapplication.mvvm.BaseViewModel.pageOverlay] → [PageOverlayHost.render]（传 `null` 则仅不挂蒙层、由页面自行写 `when` 亦可）。
 * - [com.example.myapplication.mvvm.BaseViewModel.userMessage]：网络类 [UiUserMessage.ErrorMessage] → [showError]；业务/一般 [UiUserMessage.InfoMessage] → [showInfo]。
 *
 * 使用 [collectFlows]（内部为 `repeatOnLifecycle(STARTED)`），避免后台页面仍收 Toast、浪费电量；
 * 每个 `Flow` 在块内 [FlowCollectInRepeatScope.onValue]，并行收集、互不阻塞。
 *
 * **仅** [BaseBindingActivity.bindBaseViewModel] / [BaseBindingFragment.bindBaseViewModel] 会调用，业务侧勿直接调（`internal`）。
 */
internal fun bindBaseViewModelUi(
    lifecycleOwner: LifecycleOwner,
    context: Context,
    vm: BaseViewModel,
    pageOverlayHost: PageOverlayHost? = null,
) {
    lifecycleOwner.collectFlows {
        if (pageOverlayHost != null) {
            vm.pageOverlay.onValue { state -> pageOverlayHost.render(state) }
        }
        vm.userMessage.onValue { ev ->
            ev.getContentIfNotHandled()?.let { msg ->
                when (msg) {
                    is UiUserMessage.ErrorMessage -> {
                        if (Toaster.isInit()) {
                            msg.errorMessage.showError()
                        } else {
                            @Suppress("DEPRECATION")
                            android.widget.Toast.makeText(
                                context,
                                msg.errorMessage,
                                android.widget.Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                    is UiUserMessage.InfoMessage -> {
                        if (Toaster.isInit()) {
                            msg.text.showInfo()
                        } else {
                            @Suppress("DEPRECATION")
                            android.widget.Toast.makeText(
                                context,
                                msg.text,
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
