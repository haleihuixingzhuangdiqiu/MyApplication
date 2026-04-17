package com.example.myapplication.social

import androidx.lifecycle.viewModelScope
import com.example.myapplication.framework.BaseViewModel
import com.example.myapplication.session.SessionRepository
import com.example.myapplication.social.adapter.SocialFeedRowModel
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
class SocialViewModel @Inject constructor(
    private val socialFeedRepository: SocialFeedRepository,
    private val sessionRepository: SessionRepository,
) : BaseViewModel() {

    private val _screenTitle = MutableStateFlow("关注流")
    val screenTitle: StateFlow<String> = _screenTitle.asStateFlow()

    private val _items = MutableStateFlow<List<SocialFeedEntry>>(emptyList())
    private val _followedEntryIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _searchQuery = MutableStateFlow("")

    private val _requireLogin = Channel<Unit>(Channel.BUFFERED)
    val requireLogin = _requireLogin.receiveAsFlow()

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val feedChromeVisible: StateFlow<Boolean> =
        sessionRepository.state.map { it.isLoggedIn }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false,
        )

    val statusHint: StateFlow<String> = combine(
        _items,
        _followedEntryIds,
        sessionRepository.state,
    ) { items, follow, session ->
        if (!session.isLoggedIn) {
            "动态 ${items.size} · 登录后可关注 · 输入关键字过滤"
        } else {
            "动态 ${items.size} · 已关注 ${follow.size} · 输入关键字过滤"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val rows: StateFlow<List<SocialFeedRowModel>> =
        combine(_items, _followedEntryIds, _searchQuery, sessionRepository.state) { items, follow, q, session ->
            val effectiveFollow = if (session.isLoggedIn) follow else emptySet()
            val t = q.trim()
            items
                .filter {
                    t.isEmpty() ||
                        it.title.contains(t, ignoreCase = true) ||
                        it.anchorLine.contains(t, ignoreCase = true)
                }
                .map { SocialFeedRowModel.from(it, effectiveFollow.contains(it.id), t) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            syncFollowIdsWithSession()
            showPageLoading()
            refreshInternal()
        }
        viewModelScope.launch {
            sessionRepository.state.collect {
                syncFollowIdsWithSession()
            }
        }
    }

    private suspend fun syncFollowIdsWithSession() {
        if (sessionRepository.state.value.isLoggedIn) {
            _followedEntryIds.value = socialFeedRepository.loadFollowedEntryIds()
        } else {
            _followedEntryIds.value = emptySet()
            socialFeedRepository.saveFollowedEntryIds(emptySet())
        }
    }

    fun setSearchQuery(raw: String) {
        _searchQuery.value = raw
    }

    override fun onPageOverlayRetry() {
        viewModelScope.launch {
            showPageLoading()
            refreshInternal()
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            setLoading(true)
            try {
                refreshInternal()
            } finally {
                setLoading(false)
            }
        }
    }

    fun toggleFollow(entryId: Int) {
        viewModelScope.launch {
            if (!sessionRepository.state.value.isLoggedIn) {
                sessionRepository.setPendingFollowEntryId(entryId)
                _requireLogin.send(Unit)
                return@launch
            }
            applyFollowToggle(entryId)
        }
    }

    /** 登录页返回后由 Fragment [onResume] 调用，恢复「去关注」动作。 */
    fun applyPendingFollowAfterLogin() {
        viewModelScope.launch {
            val id = sessionRepository.consumePendingFollowEntryId() ?: return@launch
            if (!sessionRepository.state.value.isLoggedIn) return@launch
            val next = _followedEntryIds.value.toMutableSet()
            next.add(id)
            _followedEntryIds.value = next
            socialFeedRepository.saveFollowedEntryIds(next)
        }
    }

    private suspend fun applyFollowToggle(entryId: Int) {
        val next = _followedEntryIds.value.toMutableSet()
        if (!next.remove(entryId)) {
            next.add(entryId)
        }
        _followedEntryIds.value = next
        socialFeedRepository.saveFollowedEntryIds(next)
    }

    private suspend fun refreshInternal() {
        socialFeedRepository.fetchFeed()
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
