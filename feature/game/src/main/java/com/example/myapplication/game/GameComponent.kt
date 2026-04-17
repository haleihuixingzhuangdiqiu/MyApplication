package com.example.myapplication.game

import com.alibaba.android.arouter.launcher.ARouter
import com.billy.cc.core.component.CC
import com.billy.cc.core.component.CCResult
import com.billy.cc.core.component.IDynamicComponent
import com.example.myapplication.common.CcNames
import com.example.myapplication.navigation.RoutePaths

/** 跨模块入口：打开游戏广场 Activity。 */
class GameComponent : IDynamicComponent {

    override fun getName(): String = CcNames.GAME

    override fun onCall(cc: CC): Boolean {
        ARouter.getInstance().build(RoutePaths.GAME).navigation(cc.context)
        CC.sendCCResult(cc.callId, CCResult.success())
        return false
    }
}
