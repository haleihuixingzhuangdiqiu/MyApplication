package com.example.myapplication.sandbox

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.framework.BaseUiActivity
import com.example.myapplication.framework.PageOverlayHost
import com.example.myapplication.navigation.RoutePaths
import kotlinx.coroutines.launch

@Route(path = RoutePaths.SANDBOX_VIEWMODEL_TEST)
class SandboxViewModelTestActivity : BaseUiActivity() {

    override val standaloneShellLayoutId: Int = R.layout.activity_sandbox_viewmodel_test_shell

    override val standaloneToolbarId: Int = R.id.toolbar

    private lateinit var viewModel: SandboxViewModelTestViewModel
    private lateinit var overlayHost: PageOverlayHost
    private lateinit var inlineProgress: ProgressBar
    private lateinit var inlineLoadingText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SandboxViewModelTestViewModel::class.java]
        overlayHost = attachPageOverlayHost { viewModel.onPageOverlayRetry() }
        inlineProgress = findViewById(R.id.progress_inline_loading)
        inlineLoadingText = findViewById(R.id.text_inline_loading)
        statusText = findViewById(R.id.text_vm_status)

        bindActions()
        bindViewModel()
    }

    private fun bindActions() {
        findViewById<Button>(R.id.btn_vm_show_toast).setOnClickListener { viewModel.triggerToast() }
        findViewById<Button>(R.id.btn_vm_post_error).setOnClickListener { viewModel.triggerError() }
        findViewById<Button>(R.id.btn_vm_emit_message).setOnClickListener { viewModel.triggerMessage() }
        findViewById<Button>(R.id.btn_vm_inline_success).setOnClickListener { viewModel.triggerInlineLoadingSuccess() }
        findViewById<Button>(R.id.btn_vm_inline_fail).setOnClickListener { viewModel.triggerInlineLoadingFailure() }
        findViewById<Button>(R.id.btn_vm_page_loading_hide).setOnClickListener { viewModel.triggerPageLoadingThenHide() }
        findViewById<Button>(R.id.btn_vm_page_empty).setOnClickListener { viewModel.triggerPageEmpty() }
        findViewById<Button>(R.id.btn_vm_page_error).setOnClickListener { viewModel.triggerPageError() }
        findViewById<Button>(R.id.btn_vm_page_success).setOnClickListener { viewModel.triggerPageLoadingToSuccess() }
        findViewById<Button>(R.id.btn_vm_page_empty_async).setOnClickListener { viewModel.triggerPageLoadingToEmpty() }
        findViewById<Button>(R.id.btn_vm_page_error_async).setOnClickListener { viewModel.triggerPageLoadingToError() }
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loading.collect { loading ->
                        inlineProgress.visibility = if (loading) View.VISIBLE else View.GONE
                        inlineLoadingText.text = getString(
                            R.string.sandbox_vm_test_inline_loading,
                            if (loading) "true" else "false",
                        )
                    }
                }
                launch {
                    viewModel.pageOverlay.collect { state ->
                        overlayHost.render(state)
                    }
                }
                launch {
                    viewModel.status.collect { status ->
                        statusText.text = status
                    }
                }
                launch {
                    viewModel.userMessage.collect { event ->
                        event.getContentIfNotHandled()?.let { msg ->
                            android.widget.Toast.makeText(this@SandboxViewModelTestActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
