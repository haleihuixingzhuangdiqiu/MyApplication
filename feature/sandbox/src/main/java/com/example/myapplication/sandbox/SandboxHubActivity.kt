package com.example.myapplication.sandbox

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.mvvm.BaseUiActivity
import com.example.myapplication.navigation.RoutePaths

@Route(path = RoutePaths.SANDBOX_HUB)
class SandboxHubActivity : BaseUiActivity() {

    override val standaloneShellLayoutId: Int = R.layout.activity_sandbox_hub_shell

    override val standaloneToolbarId: Int = R.id.toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<Button>(R.id.btn_open_background_library_test).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_BACKGROUND).navigation()
        }
        findViewById<Button>(R.id.btn_open_module_adapter_test).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_MODULE_ADAPTER).navigation()
        }
        findViewById<Button>(R.id.btn_open_page_state_test).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_PAGE_STATE).navigation()
        }
        findViewById<Button>(R.id.btn_open_storage_adapt_test).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_STORAGE_ADAPT).navigation()
        }
        findViewById<Button>(R.id.btn_open_network_chain).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_NETWORK_CHAIN).navigation()
        }
        findViewById<Button>(R.id.btn_open_paged_refresh).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_PAGED_REFRESH).navigation()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sandbox_hub, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_viewmodel_test -> {
                ARouter.getInstance().build(RoutePaths.SANDBOX_VIEWMODEL_TEST).navigation()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
