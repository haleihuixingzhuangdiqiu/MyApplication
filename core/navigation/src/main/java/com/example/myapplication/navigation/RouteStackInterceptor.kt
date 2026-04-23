package com.example.myapplication.navigation

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor

/**
 * 在路由放行前记录 path，供调试/埋点（非系统返回栈）。
 * 置于 :core:navigation，与 [RoutePaths] 同模块，由 kapt 注册到 ARouter（`AROUTER_MODULE_NAME=navigation`）。
 */
@Interceptor(priority = 300, name = "route_stack")
class RouteStackInterceptor : IInterceptor {

    override fun init(context: Context) {
        // no-op
    }

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
        RouteStack.record(postcard.path)
        callback.onContinue(postcard)
    }
}
