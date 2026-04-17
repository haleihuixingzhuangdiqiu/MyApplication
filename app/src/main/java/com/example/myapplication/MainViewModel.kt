package com.example.myapplication

import com.example.myapplication.navigation.RoutePaths
import com.example.myapplication.framework.BaseViewModel
import com.example.myapplication.framework.Event
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 门户壳 ViewModel：底部 Tab 切换在 [MainActivity] 内处理；此处仅保留 ARouter 外链（如沙箱）。 */
@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel() {

    private val _navigateTo = MutableSharedFlow<Event<String>>(extraBufferCapacity = 1)
    val navigateTo: SharedFlow<Event<String>> = _navigateTo.asSharedFlow()

    fun openGame() {
        viewModelScope.launch { _navigateTo.emit(Event(RoutePaths.GAME)) }
    }

    fun openSocial() {
        viewModelScope.launch { _navigateTo.emit(Event(RoutePaths.SOCIAL)) }
    }

    fun openMall() {
        viewModelScope.launch { _navigateTo.emit(Event(RoutePaths.MALL)) }
    }

    fun openSandbox() {
        viewModelScope.launch { _navigateTo.emit(Event(RoutePaths.SANDBOX_HUB)) }
    }
}
