package com.example.myapplication.sandbox

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.mvvm.BaseUiActivity
import com.example.myapplication.mvvm.PageOverlayHost
import com.example.myapplication.mvvm.Event
import com.example.myapplication.mvvm.UiUserMessage
import com.example.myapplication.common.toast.showError
import com.example.myapplication.common.toast.showInfo
import com.example.myapplication.navigation.RoutePaths
import kotlinx.coroutines.launch

@Route(path = RoutePaths.SANDBOX_VIEWMODEL_TEST)
class SandboxViewModelTestActivity : BaseUiActivity() {

    override val standaloneShellLayoutId: Int = R.layout.activity_sandbox_viewmodel_test_shell

    override val standaloneToolbarId: Int = R.id.toolbar

    private lateinit var viewModel: SandboxViewModelTestViewModel
    private lateinit var overlayHost: PageOverlayHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.sandbox_vm_test_title)

        viewModel = ViewModelProvider(this)[SandboxViewModelTestViewModel::class.java]
        overlayHost = attachPageOverlayHost { viewModel.onPageOverlayRetry() }

        findViewById<View>(R.id.btn_none).setOnClickListener { viewModel.demoNone() }
        findViewById<View>(R.id.btn_inline).setOnClickListener { viewModel.demoInline() }
        findViewById<View>(R.id.btn_page_content).setOnClickListener { viewModel.demoPageToContent() }
        findViewById<View>(R.id.btn_page_empty).setOnClickListener { viewModel.demoPageToEmpty() }
        findViewById<View>(R.id.btn_page_error).setOnClickListener { viewModel.demoPageToError() }
        findViewById<View>(R.id.btn_toast).setOnClickListener { viewModel.demoToast() }
        findViewById<View>(R.id.btn_err).setOnClickListener { viewModel.demoPostError() }

        val inlineProgress = findViewById<ProgressBar>(R.id.progress_inline_loading)
        val inlineLoadingText = findViewById<TextView>(R.id.text_inline_loading)
        val logText = findViewById<TextView>(R.id.text_vm_status)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loading.collect { loading ->
                        inlineProgress.visibility = if (loading) View.VISIBLE else View.GONE
                        inlineLoadingText.text = "loading: $loading"
                    }
                }
                launch {
                    viewModel.pageOverlay.collect { overlayHost.render(it) }
                }
                launch {
                    viewModel.log.collect { logText.text = it }
                }
                launch {
                    viewModel.userMessage.collect { event: Event<UiUserMessage> ->
                        event.getContentIfNotHandled()?.let { msg ->
                            when (msg) {
                                is UiUserMessage.ErrorMessage -> msg.errorMessage.showError()
                                is UiUserMessage.InfoMessage -> msg.text.showInfo()
                            }
                        }
                    }
                }
            }
        }
    }
}
