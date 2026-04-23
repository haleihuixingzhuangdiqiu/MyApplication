package com.example.myapplication.startup

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import coil.Coil
import com.alibaba.android.arouter.launcher.ARouter
import com.billy.cc.core.component.CC
import com.example.myapplication.BuildConfig
import com.example.myapplication.cc.CcComponents
import com.example.myapplication.di.CoilImageLoaderEntryPoint
import com.example.myapplication.storage.MmkvInitializer
import com.example.myapplication.framework.FrameworkAutoAdaptStrategy
import com.noober.background.BackgroundLibrary
import dagger.hilt.android.EarlyEntryPoints
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.AutoSizeConfig
import timber.log.Timber

/**
 * 冷启动库初始化：AutoSize、MMKV、Timber（仅 Debug）、BackgroundLibrary、Coil（`@EarlyEntryPoint`）、CC、ARouter、CC 组件注册。
 * 在 [android.app.Application.onCreate] 之前执行。
 */
class LibrariesStartupInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val app = context.applicationContext as Application

        // 屏幕适配属于全局基础设施，放到统一启动链路里做初始化。
        AutoSize.checkAndInit(app)
        AutoSizeConfig.getInstance()
            .setAutoAdaptStrategy(FrameworkAutoAdaptStrategy())
            .setDesignWidthInDp(DESIGN_WIDTH_DP)
        MmkvInitializer.initialize(app)

        if (BuildConfig.DEBUG) {
            Timber.plant(
                object : Timber.DebugTree() {
                    override fun createStackElementTag(element: StackTraceElement): String =
                        "(${element.fileName}:${element.lineNumber})#${element.methodName}"
                },
            )
        }

        BackgroundLibrary.inject(app)

        val imageLoader = EarlyEntryPoints.get(app, CoilImageLoaderEntryPoint::class.java).imageLoader()
        Coil.setImageLoader(imageLoader)

        CC.init(app)
        if (BuildConfig.DEBUG) {
            CC.enableDebug(true)
            CC.enableVerboseLog(true)
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(app)

        CcComponents.registerAll()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    private companion object {
        const val DESIGN_WIDTH_DP = 375
    }
}
