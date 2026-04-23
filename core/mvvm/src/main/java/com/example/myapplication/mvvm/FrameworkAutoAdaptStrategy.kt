package com.example.myapplication.mvvm

import android.app.Activity
import me.jessyan.autosize.AutoAdaptStrategy
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.DefaultAutoAdaptStrategy

/**
 * 在 [DefaultAutoAdaptStrategy] 之前先判断 [BaseUiActivity.useAutoSize]。
 * 密度适配完全交给 [me.jessyan.autosize] 的生命周期策略，不覆写 [Activity.getResources]，避免 Lottie
 * 等库在子线程调 [getResources] 时触发主线程限制。
 */
class FrameworkAutoAdaptStrategy : AutoAdaptStrategy {

    private val defaultStrategy = DefaultAutoAdaptStrategy()

    override fun applyAdapt(target: Any, activity: Activity) {
        if (target is BaseUiActivity && !target.useAutoSize) {
            AutoSize.cancelAdapt(activity)
            return
        }
        defaultStrategy.applyAdapt(target, activity)
    }
}
