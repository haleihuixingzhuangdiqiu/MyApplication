package com.example.myapplication.sandbox

import com.alibaba.android.arouter.launcher.ARouter
import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IDynamicComponent
import com.example.myapplication.common.CcNames
import com.example.myapplication.navigation.RoutePaths

class SandboxModuleAdapterComponent : IDynamicComponent {

    override fun getName(): String = CcNames.SANDBOX_MODULE_ADAPTER

    override fun onCall(cc: CC): Boolean {
        ARouter.getInstance().build(RoutePaths.SANDBOX_MODULE_ADAPTER).navigation(cc.context)
        CC.sendCCResult(cc.callId, CCResult.success())
        return false
    }
}
