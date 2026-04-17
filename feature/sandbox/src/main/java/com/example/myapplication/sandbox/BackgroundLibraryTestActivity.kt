package com.example.myapplication.sandbox

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.framework.BaseUiActivity

/**
 * [BackgroundLibrary](https://github.com/JavaNoober/BackgroundLibrary) 独立试页面。
 */
@Route(path = RoutePaths.SANDBOX_BACKGROUND)
class BackgroundLibraryTestActivity : BaseUiActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_library_test)
    }
}
