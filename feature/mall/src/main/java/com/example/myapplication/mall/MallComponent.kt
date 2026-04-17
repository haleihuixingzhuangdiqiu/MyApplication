package com.example.myapplication.mall

import com.alibaba.android.arouter.launcher.ARouter
import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IDynamicComponent
import com.example.myapplication.common.CcNames
import com.example.myapplication.navigation.RoutePaths

class MallComponent : IDynamicComponent {

    override fun getName(): String = CcNames.MALL

    override fun onCall(cc: CC): Boolean {
        ARouter.getInstance().build(RoutePaths.MALL).navigation(cc.context)
        CC.sendCCResult(cc.callId, CCResult.success())
        return false
    }
}
