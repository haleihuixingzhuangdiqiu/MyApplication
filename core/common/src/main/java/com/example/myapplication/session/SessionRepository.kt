package com.example.myapplication.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class SessionState(
    val isLoggedIn: Boolean,
    /** 展示用昵称，登录后一般为账号名。 */
    val displayName: String?,
)

/**
 * 本地会话占位：持久化 + [StateFlow]；供各模块判断登录态与登录后恢复挂起操作。
 */
@Singleton
class SessionRepository @Inject constructor(
    private val localStore: SessionLocalStore,
) {

    private val _state = MutableStateFlow(localStore.readState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    @Volatile
    private var pendingFollowEntryId: Int? = null

    @Volatile
    private var pendingCartPostId: Int? = null

    /**
     * 模拟账号密码登录：非空密码即视为成功（演示用）。
     */
    suspend fun signIn(username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        val u = username.trim()
        if (u.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("请输入账号"))
        }
        if (password.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("请输入密码"))
        }
        localStore.writeSignedIn(u)
        _state.value = localStore.readState()
        Result.success(Unit)
    }

    /** 兼容旧占位入口（如测试）。 */
    suspend fun signInMock(displayName: String) = withContext(Dispatchers.IO) {
        localStore.writeSignedIn(displayName.ifBlank { "访客" })
        _state.value = localStore.readState()
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        localStore.clear()
        pendingFollowEntryId = null
        pendingCartPostId = null
        _state.value = localStore.readState()
    }

    fun setPendingFollowEntryId(id: Int) {
        pendingFollowEntryId = id
    }

    fun consumePendingFollowEntryId(): Int? {
        val v = pendingFollowEntryId
        pendingFollowEntryId = null
        return v
    }

    fun setPendingCartPostId(id: Int) {
        pendingCartPostId = id
    }

    fun consumePendingCartPostId(): Int? {
        val v = pendingCartPostId
        pendingCartPostId = null
        return v
    }
}
