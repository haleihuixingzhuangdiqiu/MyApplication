package com.example.myapplication.common.toast

import android.app.Application
import com.hjq.toast.Toaster
import com.hjq.toast.style.CustomToastStyle
import com.example.myapplication.common.R

/**
 * [Toaster] 全局初始化，由 [com.example.myapplication.startup.LibrariesStartupInitializer] 在
 * [Application.onCreate] 之前调用（与 Application 中其它冷启动项一致）。
 * 使用自定义布局（[android.R.id.message] 为文字，与 [getActivity/Toaster](https://github.com/getActivity/Toaster) 约定一致），
 * 各扩展方法用 [ToastParams.style] 覆盖不同版式见 [StringToastExt]。
 */
object AppToaster {
    fun init(app: Application) {
        if (Toaster.isInit()) return
        val defaultStyle = CustomToastStyle(R.layout.common_toast_plain)
        Toaster.init(app, null, defaultStyle)
    }
}
