package com.example.myapplication.storage

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * MMKV 全局初始化入口，集中放在 core 内，避免上层直接接触三方库。
 */
object MmkvInitializer {

    @Volatile
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            MMKV.initialize(context.applicationContext)
            initialized = true
        }
    }

    internal fun ensureInitialized() {
        check(initialized) { "MMKV is not initialized. Call MmkvInitializer.initialize(context) first." }
    }
}
