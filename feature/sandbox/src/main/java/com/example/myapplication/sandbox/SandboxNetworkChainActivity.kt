package com.example.myapplication.sandbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.common.toast.showError
import com.example.myapplication.common.toast.showInfo
import com.example.myapplication.mvvm.BaseUiActivity
import com.example.myapplication.mvvm.Event
import com.example.myapplication.mvvm.UiUserMessage
import com.example.myapplication.mvvm.PageOverlayHost
import com.example.myapplication.mvvm.PageOverlayState
import com.example.myapplication.mvvm.collectFlows
import com.example.myapplication.navigation.RoutePaths
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/** 见 [SandboxNetworkChainViewModel] 顶部场景说明与 `dataXxx` / `envXxx` / `flowXxx` 方法。 */
@Route(path = RoutePaths.SANDBOX_NETWORK_CHAIN)
@AndroidEntryPoint
class SandboxNetworkChainActivity : BaseUiActivity() {

    override val standaloneShellLayoutId: Int = R.layout.activity_sandbox_network_chain_shell
    override val standaloneToolbarId: Int = R.id.toolbar

    private val viewModel: SandboxNetworkChainViewModel by viewModels()
    private lateinit var overlayHost: PageOverlayHost
    private var floatDialog: androidx.appcompat.app.AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.sandbox_network_chain_title)
        overlayHost = attachPageOverlayHost { viewModel.onPageOverlayRetry() }

        val textLog = findViewById<TextView>(R.id.text_log)
        val textDebug = findViewById<TextView>(R.id.text_debug)

        with(viewModel) {
            listOf(
                R.id.btn_d_dialog to { dataDialogSuccess() },
                R.id.btn_d_page to { dataPageWithOverlay() },
                R.id.btn_d_defer to { dataDeferredDialog() },
                R.id.btn_d_net_silent to { dataNetworkOnNetworkOnly() },
                R.id.btn_d_net_default to { dataNetworkDefaultToastOnly() },
                R.id.btn_d_on_error to { dataRequestOnErrorOnly() },
                R.id.btn_e_default to { envEncapsulationAutoMessage() },
                R.id.btn_e_skip to { envHubWithSkipGlobal() },
                R.id.btn_e_biz to { envCustomOnError() },
                R.id.btn_e_skip_enc to { envSkipEncapsulationOnError() },
                R.id.btn_f_data to { flowHandleData() },
                R.id.btn_f_env to { flowHandleResult() },
                R.id.btn_f_as to { flowAsStateDemo() },
            ).forEach { (id, block) -> findViewById<View>(id).setOnClickListener { block() } }
        }

        collectFlows {
            viewModel.pageOverlay.onValue { overlayHost.render(it) }
            viewModel.logLine.onValue { text -> textLog.text = text }
            viewModel.userMessage.onValue { event: Event<UiUserMessage> ->
                event.getContentIfNotHandled()?.let { msg ->
                    when (msg) {
                        is UiUserMessage.ErrorMessage -> msg.errorMessage.showError()
                        is UiUserMessage.InfoMessage -> msg.text.showInfo()
                    }
                }
            }
            val pageLabel: (PageOverlayState) -> String = {
                when (it) {
                    PageOverlayState.Hidden -> "Hidden"
                    PageOverlayState.Loading -> "Loading"
                    is PageOverlayState.Empty -> "Empty"
                    is PageOverlayState.Error -> "Error"
                }
            }
            fun refreshDebug() {
                textDebug.text = "d=${viewModel.dialogLoading.value} 整页=${pageLabel(viewModel.pageOverlay.value)}"
            }
            viewModel.dialogLoading.onValue { showDialog ->
                if (showDialog) {
                    if (floatDialog == null) {
                        val content = LayoutInflater.from(this@SandboxNetworkChainActivity)
                            .inflate(R.layout.sandbox_dialog_float_loading, null)
                        val dlg = MaterialAlertDialogBuilder(this@SandboxNetworkChainActivity)
                            .setView(content)
                            .setCancelable(false)
                            .create()
                        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        floatDialog = dlg
                    }
                    if (floatDialog?.isShowing != true) floatDialog?.show()
                } else {
                    floatDialog?.dismiss()
                }
                refreshDebug()
            }
            viewModel.pageOverlay.onValue { refreshDebug() }
        }
    }

    override fun onDestroy() {
        floatDialog?.dismiss()
        floatDialog = null
        super.onDestroy()
    }
}
