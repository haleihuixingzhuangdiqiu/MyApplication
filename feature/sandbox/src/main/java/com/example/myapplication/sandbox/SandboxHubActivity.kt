package com.example.myapplication.sandbox

import android.os.Bundle
import android.widget.Button
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.framework.BaseUiActivity

@Route(path = RoutePaths.SANDBOX_HUB)
class SandboxHubActivity : BaseUiActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sandbox_hub)
        findViewById<Button>(R.id.btn_open_background_library_test).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_BACKGROUND).navigation()
        }
        findViewById<Button>(R.id.btn_open_module_adapter_test).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_MODULE_ADAPTER).navigation()
        }
        findViewById<Button>(R.id.btn_open_page_state_test).setOnClickListener {
            ARouter.getInstance().build(RoutePaths.SANDBOX_PAGE_STATE).navigation()
        }
    }
}
