package com.example.myapplication.social

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.framework.BaseUiActivity
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RoutePaths.SOCIAL)
@AndroidEntryPoint
class SocialActivity : BaseUiActivity() {

    override val standaloneShellLayoutId: Int = R.layout.activity_social

    override val standaloneToolbarId: Int = R.id.toolbar
}
