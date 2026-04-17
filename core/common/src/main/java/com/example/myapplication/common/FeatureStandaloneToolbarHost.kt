package com.example.myapplication.common

/**
 * 由 [com.example.myapplication.framework.BaseUiActivity] 提供默认实现；独立壳只需覆写
 * [com.example.myapplication.framework.BaseUiActivity.standaloneShellLayoutId] 与
 * [com.example.myapplication.framework.BaseUiActivity.standaloneToolbarId]。
 * 子 [androidx.fragment.app.Fragment] 通过本接口控制顶栏标题与返回箭头。
 */
interface FeatureStandaloneToolbarHost {

    fun setFeatureToolbarTitle(title: CharSequence)

    fun setFeatureToolbarBackVisible(visible: Boolean)
}
