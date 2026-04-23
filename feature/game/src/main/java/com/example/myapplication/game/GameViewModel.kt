package com.example.myapplication.game

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.AppMetaEntity
import com.example.myapplication.database.HomePostCacheEntity
import com.example.myapplication.mvvm.BaseViewModel
import com.example.myapplication.mvvm.launchInlineLoading
import com.example.myapplication.game.adapter.GameBannerUi
import com.example.myapplication.game.adapter.GameMetaHintModel
import com.example.myapplication.game.adapter.GamePostRowModel
import com.example.myapplication.game.adapter.GameSectionTitleModel
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.ModuleGroupSectionModel
import com.tory.module_adapter.views.ModuleSpaceModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class GameViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val db: AppDatabase,
    private val gameDemoRepository: GameDemoRepository,
) : BaseViewModel() {

    private val tabTitles: Array<String> =
        appContext.resources.getStringArray(R.array.game_tab_labels)

    private val _screenTitle = MutableStateFlow(appContext.getString(R.string.game_screen_title))
    val screenTitle: StateFlow<String> = _screenTitle.asStateFlow()

    private val _roomHint = MutableStateFlow(appContext.getString(R.string.game_hint_init))
    private val _selectedTab = MutableStateFlow(0)

    val selectedTabIndex: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _banners = MutableStateFlow(defaultBanners())
    val banners: StateFlow<List<GameBannerUi>> = _banners.asStateFlow()

    /** 混合列表：提示条 + 分区标题 + 宫格帖子（与 ModuleAdapter register 顺序一致）。 */
    val feedRows: StateFlow<List<Any>> = combine(
        gameDemoRepository.observeCachedPosts(),
        _selectedTab,
        _roomHint,
    ) { entities, tab, hint ->
        buildFeed(entities, tab, hint)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        launchInlineLoading {
            db.appMetaDao().upsert(AppMetaEntity("last_open_game", System.currentTimeMillis().toString()))
            val count = db.appMetaDao().count()
            gameDemoRepository.refreshFromNetwork()
                .onSuccess { n ->
                    _roomHint.value = appContext.getString(
                        R.string.game_hint_ok,
                        count,
                        n,
                    )
                }
                .onFailure {
                    _roomHint.value = appContext.getString(R.string.game_hint_network_fail, count)
                }
        }
    }

    fun selectTab(index: Int) {
        val i = index.coerceIn(0, tabTitles.lastIndex)
        if (_selectedTab.value != i) {
            _selectedTab.value = i
        }
    }

    fun refreshPosts() {
        launchInlineLoading {
            val count = db.appMetaDao().count()
            gameDemoRepository.refreshFromNetwork()
                .onSuccess { n ->
                    _roomHint.value = appContext.getString(R.string.game_hint_refreshed, n)
                }
                .onFailure { e ->
                    postError(e.message ?: appContext.getString(R.string.game_refresh_fail))
                    _roomHint.value = appContext.getString(R.string.game_hint_network_fail, count)
                }
        }
    }

    private fun buildFeed(
        entities: List<HomePostCacheEntity>,
        tab: Int,
        hint: String,
    ): List<Any> = buildList {
        add(GameMetaHintModel(hint))
        add(ModuleSpaceModel(height = 8.dp(appContext), tag = "sp-hint"))
        val label = tabTitles.getOrElse(tab) { tabTitles[0] }
        add(
            GameSectionTitleModel(
                sectionId = "main-$tab",
                title = appContext.getString(R.string.game_section_feed_title, label),
                subtitle = appContext.getString(R.string.game_section_feed_subtitle),
            ),
        )
        val filtered = entities.filter { it.postId % 4 == tab }
        if (filtered.isEmpty()) {
            add(
                GameSectionTitleModel(
                    sectionId = "empty",
                    title = appContext.getString(R.string.game_section_empty_title),
                    subtitle = appContext.getString(R.string.game_section_empty_subtitle),
                ),
            )
        } else {
            val mid = (filtered.size + 1) / 2
            filtered.take(mid).forEach { add(GamePostRowModel.from(it)) }
            if (filtered.size > 1) {
                add(ModuleGroupSectionModel(tag = "split-feed"))
                add(
                    GameSectionTitleModel(
                        sectionId = "more-$tab",
                        title = appContext.getString(R.string.game_section_more_title),
                        subtitle = appContext.getString(R.string.game_section_more_subtitle, filtered.size - mid),
                    ),
                )
                filtered.drop(mid).forEach { add(GamePostRowModel.from(it)) }
            }
        }
    }

    private fun defaultBanners(): List<GameBannerUi> = listOf(
        GameBannerUi(
            id = 1,
            title = appContext.getString(R.string.game_banner_1_title),
            subtitle = appContext.getString(R.string.game_banner_1_sub),
            imageUrl = "https://picsum.photos/seed/game-banner-1/1200/520",
        ),
        GameBannerUi(
            id = 2,
            title = appContext.getString(R.string.game_banner_2_title),
            subtitle = appContext.getString(R.string.game_banner_2_sub),
            imageUrl = "https://picsum.photos/seed/game-banner-2/1200/520",
        ),
        GameBannerUi(
            id = 3,
            title = appContext.getString(R.string.game_banner_3_title),
            subtitle = appContext.getString(R.string.game_banner_3_sub),
            imageUrl = "https://picsum.photos/seed/game-banner-3/1200/520",
        ),
    )
}
