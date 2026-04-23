package com.example.myapplication.storage

/**
 * Core 层统一键值存储抽象（MMKV 实现）；**业务侧请优先用 [KV] / [KvScope]**，一般不必直接持本接口。
 * 低层 `putObject` / `getObject` 见 `KeyValueObject.kt`（Gson JSON）。
 */
interface KeyValueStore {

    // --- 基础类型（与 MMKV 原生能力对齐）---

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    fun getString(key: String, defaultValue: String? = null): String?

    fun getInt(key: String, defaultValue: Int = 0): Int

    fun getLong(key: String, defaultValue: Long = 0L): Long

    fun getFloat(key: String, defaultValue: Float = 0f): Float

    fun getDouble(key: String, defaultValue: Double = 0.0): Double

    /** 不存在时一般返回 [defaultValue]；若需区分“未存”与空数组请结合 [containsKey]。 */
    fun getByteArray(key: String, defaultValue: ByteArray? = null): ByteArray?

    /** 不存在或曾写入 `null`（已 remove）时回退 [defaultValue]。 */
    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>?

    fun putBoolean(key: String, value: Boolean)

    fun putString(key: String, value: String?)

    fun putInt(key: String, value: Int)

    fun putLong(key: String, value: Long)

    fun putFloat(key: String, value: Float)

    fun putDouble(key: String, value: Double)

    fun putByteArray(key: String, value: ByteArray?)

    fun putStringSet(key: String, value: Set<String>?)

    fun containsKey(key: String): Boolean

    fun removeValueForKey(key: String)

    fun clearAll()
}
