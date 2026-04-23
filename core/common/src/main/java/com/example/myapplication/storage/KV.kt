package com.example.myapplication.storage

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

/**
 * 常用 MMKV **分库 id**，避免各业务魔法字符串。新增业务域时在此加常量，并可在 [KV] 上增加只读属性。
 */
object KvId {
    const val APP = "app"
    const val SESSION = "app_session"
    const val MALL = "mall"
    const val GAME = "game"
    const val SOCIAL = "social"
}

/**
 * 键值存取**唯一推荐入口**（`Kv.mall.put("k", v)` / `Kv.of("custom_id").getString("k")`），无需自管 [KeyValueStore] 与 [KeyValueStoreFactory]。
 *
 * 每个 [storeId] 对应一个 MMKV 文件；同 id 在进程内复用同一句柄（内部缓存）。[keyPrefix] 只作用于 key 字符串，不改变文件分库。
 */
object KV {

    private val storeCache = ConcurrentHashMap<String, KeyValueStore>()

    internal fun storeFor(storeId: String): KeyValueStore =
        storeCache.getOrPut(storeId) { openKeyValueStoreInternal(storeId) }

    /**
     * @param storeId MMKV 分库 id，需稳定不变以便持久化；自定义域建议复用 [KvId] 或自建常量。
     * @param keyPrefix 业务 key 前缀（如 `"user_"`），再 `put("name", …)` 实际为 `user_name`。
     */
    @JvmStatic
    @JvmOverloads
    fun of(storeId: String, keyPrefix: String = ""): KvScope = KvScope(storeId, keyPrefix)

    val app: KvScope get() = of(KvId.APP)
    val session: KvScope get() = of(KvId.SESSION)
    val mall: KvScope get() = of(KvId.MALL)
    val game: KvScope get() = of(KvId.GAME)
    val social: KvScope get() = of(KvId.SOCIAL)
}

/**
 * 已绑定 [storeId] 与可选 [keyPrefix] 的读写面；与 [KeyValueStore] 能力对齐，并增加 [put] 多类型重载。
 *
 * 注意 [clear] 会清空**整个** [storeId] 对应文件，与 [keyPrefix] 无关；仅想删某 key 用 [remove]。
 */
class KvScope internal constructor(
    private val storeId: String,
    private val keyPrefix: String = "",
) {

    private val s: KeyValueStore get() = KV.storeFor(storeId)
    private fun k(key: String) = if (keyPrefix.isEmpty()) key else keyPrefix + key

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = s.getBoolean(k(key), defaultValue)
    fun getString(key: String, defaultValue: String? = null): String? = s.getString(k(key), defaultValue)
    fun getInt(key: String, defaultValue: Int = 0): Int = s.getInt(k(key), defaultValue)
    fun getLong(key: String, defaultValue: Long = 0L): Long = s.getLong(k(key), defaultValue)
    fun getFloat(key: String, defaultValue: Float = 0f): Float = s.getFloat(k(key), defaultValue)
    fun getDouble(key: String, defaultValue: Double = 0.0): Double = s.getDouble(k(key), defaultValue)
    fun getByteArray(key: String, defaultValue: ByteArray? = null): ByteArray? = s.getByteArray(k(key), defaultValue)
    fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>? = s.getStringSet(k(key), defaultValue)
    fun containsKey(key: String): Boolean = s.containsKey(k(key))

    fun putBoolean(key: String, value: Boolean) = s.putBoolean(k(key), value)
    fun putString(key: String, value: String?) = s.putString(k(key), value)
    fun putInt(key: String, value: Int) = s.putInt(k(key), value)
    fun putLong(key: String, value: Long) = s.putLong(k(key), value)
    fun putFloat(key: String, value: Float) = s.putFloat(k(key), value)
    fun putDouble(key: String, value: Double) = s.putDouble(k(key), value)
    fun putByteArray(key: String, value: ByteArray?) = s.putByteArray(k(key), value)
    fun putStringSet(key: String, value: Set<String>?) = s.putStringSet(k(key), value)

    fun putObject(key: String, value: Any?, gson: Gson = KeyValueJson.defaultGson) = s.putObject(k(key), value, gson)

    fun <T> getObject(key: String, type: Type, gson: Gson = KeyValueJson.defaultGson): T? = s.getObject(k(key), type, gson)
    fun <T> getObject(key: String, clazz: Class<T>, gson: Gson = KeyValueJson.defaultGson): T? = s.getObject(k(key), clazz, gson)

    /**
     * 按运行时类型分派；非上述基础类型、且非可转为 `Set<String>` 的集合，则走 JSON [putObject]。
     */
    fun put(key: String, value: Any?) {
        val full = k(key)
        when (value) {
            null -> s.removeValueForKey(full)
            is String -> s.putString(full, value)
            is Int -> s.putInt(full, value)
            is Long -> s.putLong(full, value)
            is Float -> s.putFloat(full, value)
            is Double -> s.putDouble(full, value)
            is Boolean -> s.putBoolean(full, value)
            is ByteArray -> s.putByteArray(full, value)
            is Set<*> -> {
                if (value.isEmpty() || value.all { it is String }) {
                    @Suppress("UNCHECKED_CAST")
                    s.putStringSet(full, (value as Set<String>).toSet())
                } else {
                    s.putObject(full, value, KeyValueJson.defaultGson)
                }
            }
            else -> s.putObject(full, value, KeyValueJson.defaultGson)
        }
    }

    fun remove(key: String) = s.removeValueForKey(k(key))

    /** 清空 [storeId] 对应**整库**；若使用 [keyPrefix] 仅影响 key 拼写，仍清掉同库内全部项。 */
    fun clear() = s.clearAll()
}

/**
 * `reified` 读对象；[KvScope] 本体内不能用 public inline 访问 [KvScope] 的 private 成员，故以顶层扩展提供。
 */
inline fun <reified T> KvScope.getObject(key: String, gson: Gson = KeyValueJson.defaultGson): T? =
    getObject(key, object : TypeToken<T>() {}.type, gson)

internal fun openKeyValueStore(id: String): KeyValueStore = KV.storeFor(id)

internal fun openKeyValueStoreInternal(id: String): KeyValueStore {
    MmkvInitializer.ensureInitialized()
    val mmkv = requireNotNull(MMKV.mmkvWithID(id, MMKV.SINGLE_PROCESS_MODE)) {
        "Unable to open key-value store: $id"
    }
    return MmkvKeyValueStore(mmkv)
}
