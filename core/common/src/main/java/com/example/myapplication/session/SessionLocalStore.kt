package com.example.myapplication.session

import com.example.myapplication.storage.KV
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 会话持久化细节封装在本地存储层，避免业务侧散落 key 与底层存储实现。
 */
@Singleton
class SessionLocalStore @Inject constructor() {

    private val store = KV.session

    fun readState(): SessionState =
        SessionState(
            isLoggedIn = store.getBoolean(KEY_LOGGED_IN, false),
            displayName = store.getString(KEY_NAME, null),
        )

    fun writeSignedIn(displayName: String) {
        store.putBoolean(KEY_LOGGED_IN, true)
        store.putString(KEY_NAME, displayName)
    }

    fun clear() {
        store.clear()
    }

    private companion object {
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_NAME = "display_name"
    }
}
