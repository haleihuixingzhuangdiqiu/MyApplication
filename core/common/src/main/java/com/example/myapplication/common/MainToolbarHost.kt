package com.example.myapplication.common

/**
 * 由 [com.example.myapplication.MainActivity] 实现：单壳内子 Tab 只上报标题，不显示「向上」返回。
 */
interface MainToolbarHost {

    fun setMainToolbarTitle(title: CharSequence)
}
