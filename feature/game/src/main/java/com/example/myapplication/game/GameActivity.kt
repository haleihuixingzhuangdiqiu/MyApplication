package com.example.myapplication.game

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.mvvm.BaseUiActivity
import dagger.hilt.android.AndroidEntryPoint

@Route(path = RoutePaths.GAME)
@AndroidEntryPoint
class GameActivity : BaseUiActivity() {

    override val standaloneShellLayoutId: Int = R.layout.activity_game

    override val standaloneToolbarId: Int = R.id.toolbar
}
