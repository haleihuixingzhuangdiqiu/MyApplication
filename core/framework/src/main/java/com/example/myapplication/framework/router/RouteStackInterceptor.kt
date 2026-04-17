package com.example.myapplication.framework.router

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.example.myapplication.framework.RouteStack

/**
 * 在路由放行前记录 path，供调试/埋点（非系统返回栈）。
 * 放在 :core:framework 并走 kapt，避免 :app 的 Hilt Java 编译阶段触发 ARouter 却缺少 AROUTER_MODULE_NAME。
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
