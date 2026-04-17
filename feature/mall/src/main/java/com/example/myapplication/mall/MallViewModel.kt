package com.example.myapplication.mall

import androidx.lifecycle.viewModelScope
import com.example.myapplication.framework.BaseViewModel
import com.example.myapplication.mall.adapter.MallCatalogRowModel
import com.example.myapplication.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MallViewModel @Inject constructor(
    private val mallCatalogRepository: MallCatalogRepository,
    private val sessionRepository: SessionRepository,
) : BaseViewModel() {

    private val _screenTitle = MutableStateFlow("好物广场")
    val screenTitle: StateFlow<String> = _screenTitle.asStateFlow()

    private val _items = MutableStateFlow<List<MallItem>>(emptyList())
    private val _cartPostIds = MutableStateFlow<Set<Int>>(emptySet())

    private val _requireLogin = Channel<Unit>(Channel.BUFFERED)
    val requireLogin = _requireLogin.receiveAsFlow()

    val needsLoginGate: StateFlow<Boolean> =
        sessionRepository.state.map { !it.isLoggedIn }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            true,
        )

    val statusHint: StateFlow<String> = combine(_items, _cartPostIds, sessionRepository.state) { items, cart, session ->
        if (!session.isLoggedIn) {
            "在售 ${items.size} · 登录后使用购物车 · 下拉同步"
        } else {
            "在售 ${items.size} · 购物车 ${cart.size} 件 · 下拉同步"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val rows: StateFlow<List<MallCatalogRowModel>> =
        combine(_items, _cartPostIds, sessionRepository.state) { items, cart, session ->
            val effectiveCart = if (session.isLoggedIn) cart else emptySet()
            items.map { MallCatalogRowModel.from(it, effectiveCart.contains(it.id)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            syncCartWithSession()
            showPageLoading()
            refreshInternal()
        }
        viewModelScope.launch {
            sessionRepository.state.collect {
                syncCartWithSession()
            }
        }
    }

    private suspend fun syncCartWithSession() {
        if (sessionRepository.state.value.isLoggedIn) {
            _cartPostIds.value = mallCatalogRepository.loadCartPostIds()
        } else {
            _cartPostIds.value = emptySet()
            mallCatalogRepository.saveCartPostIds(emptySet())
        }
    }

    override fun onPageOverlayRetry() {
        viewModelScope.launch {
            showPageLoading()
            refreshInternal()
        }
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            setLoading(true)
            try {
                refreshInternal()
            } finally {
                setLoading(false)
            }
        }
    }

    fun toggleCart(postId: Int) {
        viewModelScope.launch {
            if (!sessionRepository.state.value.isLoggedIn) {
                sessionRepository.setPendingCartPostId(postId)
                _requireLogin.send(Unit)
                return@launch
            }
            val next = _cartPostIds.value.toMutableSet()
            if (!next.remove(postId)) {
                next.add(postId)
            }
            _cartPostIds.value = next
            mallCatalogRepository.saveCartPostIds(next)
        }
    }

    /** 登录页返回后由 Fragment [onResume] 调用，恢复「加入购物车」。 */
    fun applyPendingCartAfterLogin() {
        viewModelScope.launch {
            val id = sessionRepository.consumePendingCartPostId() ?: return@launch
            if (!sessionRepository.state.value.isLoggedIn) return@launch
            val next = _cartPostIds.value.toMutableSet()
            next.add(id)
            _cartPostIds.value = next
            mallCatalogRepository.saveCartPostIds(next)
        }
    }

    private suspend fun refreshInternal() {
        mallCatalogRepository.fetchCatalog()
            .onSuccess { list ->
                _items.value = list
                if (list.isEmpty()) {
                    showPageEmpty()
                } else {
                    hidePageOverlay()
                }
            }
            .onFailure { e ->
                showPageError(e.message ?: "加载失败", allowRetry = true)
            }
    }
}
