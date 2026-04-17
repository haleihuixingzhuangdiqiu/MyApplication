package com.example.myapplication.framework

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * Activity / Fragment 共用的 [BaseViewModel] UI 绑定（Loading、Toast、Flow）。
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
                vm.errorMessage.collect { ev ->
                    ev.getContentIfNotHandled()?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            launch {
                vm.uiEvents.collect { event ->
                    when (event) {
                        is UiEvent.Toast ->
                            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            launch {
                vm.messageFlow.collect { msg ->
                    if (msg != null) {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
