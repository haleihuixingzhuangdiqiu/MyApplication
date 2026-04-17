package com.example.myapplication.cc

import com.billy.cc.core.component.CC
import com.example.myapplication.game.GameComponent
import com.example.myapplication.mall.MallComponent
import com.example.myapplication.social.SocialComponent
import com.example.myapplication.sandbox.SandboxBackgroundComponent
import com.example.myapplication.sandbox.SandboxModuleAdapterComponent
import com.example.myapplication.sandbox.SandboxPageStateComponent

/** CC 动态组件集中注册，供 Startup 调用。 */
internal object CcComponents {

    fun registerAll() {
        CC.registerComponent(GameComponent())
        CC.registerComponent(SocialComponent())
        CC.registerComponent(MallComponent())
        CC.registerComponent(SandboxBackgroundComponent())
        CC.registerComponent(SandboxModuleAdapterComponent())
        CC.registerComponent(SandboxPageStateComponent())
    }
}
