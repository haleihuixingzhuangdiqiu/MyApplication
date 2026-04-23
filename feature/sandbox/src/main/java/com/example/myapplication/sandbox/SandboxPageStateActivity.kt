package com.example.myapplication.sandbox

import android.os.Bundle
import android.widget.Button
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.mvvm.BaseUiActivity
import com.example.myapplication.mvvm.PageOverlayHost
import com.example.myapplication.mvvm.PageOverlayState

/**
 * 演示 [PageOverlayState] + [PageOverlayHost]（与 [com.example.myapplication.mvvm.BaseViewModel.pageOverlay] 同源状态模型）。
 */
@Route(path = RoutePaths.SANDBOX_PAGE_STATE)
class SandboxPageStateActivity : BaseUiActivity() {

    private lateinit var overlayHost: PageOverlayHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sandbox_page_state)
        overlayHost = attachPageOverlayHost {
            overlayHost.render(PageOverlayState.Hidden)
        }

        findViewById<Button>(R.id.btn_overlay_loading).setOnClickListener {
            overlayHost.render(PageOverlayState.Loading)
        }
        findViewById<Button>(R.id.btn_overlay_empty_default).setOnClickListener {
            overlayHost.render(PageOverlayState.Empty(hint = null))
        }
        findViewById<Button>(R.id.btn_overlay_empty_custom).setOnClickListener {
            overlayHost.render(PageOverlayState.Empty(hint = getString(R.string.sandbox_page_state_empty_custom)))
        }
        findViewById<Button>(R.id.btn_overlay_error_retry).setOnClickListener {
            overlayHost.render(
                PageOverlayState.Error(
                    message = getString(R.string.sandbox_page_state_error_sample),
                    allowRetry = true,
                ),
            )
        }
        findViewById<Button>(R.id.btn_overlay_error_no_retry).setOnClickListener {
            overlayHost.render(
                PageOverlayState.Error(
                    message = getString(R.string.sandbox_page_state_error_no_retry_hint),
                    allowRetry = false,
                ),
            )
        }
        findViewById<Button>(R.id.btn_overlay_hide).setOnClickListener {
            overlayHost.render(PageOverlayState.Hidden)
        }
    }
}
