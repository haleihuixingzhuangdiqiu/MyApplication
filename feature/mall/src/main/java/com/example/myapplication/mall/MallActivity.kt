package com.example.myapplication.mall

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.framework.BaseUiActivity
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RoutePaths.MALL)
@AndroidEntryPoint
class MallActivity : BaseUiActivity() {

    override val standaloneShellLayoutId: Int = R.layout.activity_mall

    override val standaloneToolbarId: Int = R.id.toolbar
}
