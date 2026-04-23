package com.example.myapplication.storage

import com.tencent.mmkv.MMKV
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 可注入的工厂，与 [KV] / [openKeyValueStore] 共享实现；**业务日常请用 [KV.mall]、[KV.of] 等**，本类供单测 mock / Hilt 替换。
 */
@Singleton
class KeyValueStoreFactory @Inject constructor() {

    fun open(id: String): KeyValueStore = openKeyValueStore(id)
}

internal class MmkvKeyValueStore(
    private val delegate: MMKV,
) : KeyValueStore {

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        delegate.decodeBool(key, defaultValue)

    override fun getString(key: String, defaultValue: String?): String? =
        delegate.decodeString(key, defaultValue)

    override fun getInt(key: String, defaultValue: Int): Int =
        delegate.decodeInt(key, defaultValue)

    override fun getLong(key: String, defaultValue: Long): Long =
        delegate.decodeLong(key, defaultValue)

    override fun getFloat(key: String, defaultValue: Float): Float =
        delegate.decodeFloat(key, defaultValue)

    override fun getDouble(key: String, defaultValue: Double): Double =
        delegate.decodeDouble(key, defaultValue)

    override fun getByteArray(key: String, defaultValue: ByteArray?): ByteArray? {
        if (!delegate.containsKey(key)) return defaultValue
        return delegate.decodeBytes(key) ?: defaultValue
    }

    override fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? =
        delegate.decodeStringSet(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        delegate.encode(key, value)
    }

    override fun putString(key: String, value: String?) {
        if (value == null) {
            delegate.removeValueForKey(key)
        } else {
            delegate.encode(key, value)
        }
    }

    override fun putInt(key: String, value: Int) {
        delegate.encode(key, value)
    }

    override fun putLong(key: String, value: Long) {
        delegate.encode(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        delegate.encode(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        delegate.encode(key, value)
    }

    override fun putByteArray(key: String, value: ByteArray?) {
        if (value == null) {
            delegate.removeValueForKey(key)
        } else {
            delegate.encode(key, value)
        }
    }

    override fun putStringSet(key: String, value: Set<String>?) {
        if (value == null) {
            delegate.removeValueForKey(key)
        } else {
            delegate.encode(key, value)
        }
    }

    override fun containsKey(key: String): Boolean = delegate.containsKey(key)

    override fun removeValueForKey(key: String) {
        delegate.removeValueForKey(key)
    }

    override fun clearAll() {
        delegate.clearAll()
    }
}
