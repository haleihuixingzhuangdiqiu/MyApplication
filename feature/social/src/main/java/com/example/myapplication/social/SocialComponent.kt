package com.example.myapplication.social

import com.alibaba.android.arouter.launcher.ARouter
import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IDynamicComponent
import com.example.myapplication.common.CcNames
import com.example.myapplication.navigation.RoutePaths

class SocialComponent : IDynamicComponent {

    override fun getName(): String = CcNames.SOCIAL

    override fun onCall(cc: CC): Boolean {
        ARouter.getInstance().build(RoutePaths.SOCIAL).navigation(cc.context)
        CC.sendCCResult(cc.callId, CCResult.success())
        return false
    }
}
