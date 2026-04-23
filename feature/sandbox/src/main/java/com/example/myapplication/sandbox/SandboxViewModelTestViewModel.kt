package com.example.myapplication.sandbox

import com.example.myapplication.mvvm.BaseViewModel
import com.example.myapplication.mvvm.VmLoadingStyle
import com.example.myapplication.mvvm.launch
import com.example.myapplication.mvvm.launchInlineLoading
import com.example.myapplication.mvvm.launchPageLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SandboxViewModelTestViewModel : BaseViewModel() {

    private val _log = MutableStateFlow("—")
    val log: StateFlow<String> = _log.asStateFlow()

    /** NONE：不自动动 loading / pageOverlay，自行管或不管。 */
    fun demoNone() {
        launch(loadingStyle = VmLoadingStyle.NONE) {
            _log.value = "NONE：协程跑完"
        }
    }

    /** INLINE：行内 [loading] 自动开/关。 */
    fun demoInline() {
        launchInlineLoading {
            _log.value = "INLINE：请求中…"
            delay(500)
            _log.value = "INLINE：结束"
        }
    }

    /** PAGE：整页 Loading，在块内自己 [showPageContent]。 */
    fun demoPageToContent() {
        launchPageLoading {
            delay(500)
            showPageContent()
            _log.value = "PAGE → Content"
        }
    }

    fun demoPageToEmpty() {
        launchPageLoading {
            delay(500)
            showPageEmpty()
            _log.value = "PAGE → Empty"
        }
    }

    fun demoPageToError() {
        launchPageLoading {
            delay(500)
            showPageError("demo", allowRetry = true)
            _log.value = "PAGE → Error"
        }
    }

    fun demoToast() {
        _log.value = "Toast"
        showToast("Toast")
    }

    fun demoPostError() {
        _log.value = "postError"
        postError("postError")
    }

    override fun onPageOverlayRetry() {
        showPageContent()
        showToast("onPageOverlayRetry")
    }
}
