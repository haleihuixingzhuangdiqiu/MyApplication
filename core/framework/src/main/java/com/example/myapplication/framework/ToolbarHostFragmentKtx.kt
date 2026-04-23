package com.example.myapplication.framework

import androidx.fragment.app.Fragment
import com.example.myapplication.common.FeatureStandaloneToolbarHost
import com.example.myapplication.common.MainToolbarHost

/**
 * 各业务 Fragment 嵌入 [com.example.myapplication.MainActivity] 或独立 [FeatureStandaloneToolbarHost] Activity 时，
 * 统一在此发布顶栏标题、在独立壳上显示返回，避免在多个 Fragment 里重复 `when (activity) is …`。
 */
fun Fragment.publishToolbarTitle(title: CharSequence) {
    when (val a = requireActivity()) {
        is MainToolbarHost -> a.setMainToolbarTitle(title)
        is FeatureStandaloneToolbarHost -> a.setFeatureToolbarTitle(title)
    }
}

/**
 * 仅在独立壳上显示「向上」：嵌入 [MainToolbarHost]（如门户 [com.example.myapplication.MainActivity]）时直接忽略，
 * 避免 [com.example.myapplication.mvvm.BaseUiActivity] 统一实现 [FeatureStandaloneToolbarHost] 误触主壳顶栏。
 */
fun Fragment.showStandaloneToolbarBackIfHost() {
    val a = requireActivity()
    if (a is MainToolbarHost) return
    (a as? FeatureStandaloneToolbarHost)?.setFeatureToolbarBackVisible(true)
}
