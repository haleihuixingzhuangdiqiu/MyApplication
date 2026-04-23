package com.example.myapplication.storage

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * 在 [KeyValueStore] 上以 **JSON 字符串** 存取任意可 Gson 反序列化的类型（内部仍走 [KeyValueStore.putString]/[getString]）。
 *
 * 默认使用 [KeyValueJson.defaultGson]；需自定义时间格式、TypeAdapter 等时，在每次 [putObject]/[getObject] 传入自建的 [Gson] 即可。
 */
object KeyValueJson {
    /**
     * 全模块共享的 [Gson] 实例（线程安全），避免每次 put/get 都 `Gson()`。
     * 若与后端共用一套序列化规则，可改为在 Application 中初始化后赋值（需自行保证只赋一次）。
     */
    @JvmField
    var defaultGson: Gson = Gson()
}

/**
 * 将 [value] 序列化为 JSON 后写入；`value == null` 时 [removeValueForKey]（与 [putString] 置 null 行为一致）。
 */
fun KeyValueStore.putObject(
    key: String,
    value: Any?,
    gson: Gson = KeyValueJson.defaultGson,
) {
    if (value == null) {
        removeValueForKey(key)
    } else {
        putString(key, gson.toJson(value))
    }
}

/**
 * 读取 JSON 并反序列化为 [T]；无数据、JSON 非法或与类型不匹配时返回 `null`（不抛给业务侧）。
 * 支持 `reified` 范型，例如 `getObject<List<Foo>>("k")`（需 [Foo] 有可用无参构造等 Gson 能处理的形态）。
 */
inline fun <reified T> KeyValueStore.getObject(
    key: String,
    gson: Gson = KeyValueJson.defaultGson,
): T? = getObject(key, object : TypeToken<T>() {}.type, gson)

/**
 * 非 reified 或需显式 [Type]（如 `TypeToken` / `ParameterizedType`）时使用。
 */
fun <T> KeyValueStore.getObject(
    key: String,
    type: Type,
    gson: Gson = KeyValueJson.defaultGson,
): T? {
    val json = getString(key) ?: return null
    return try {
        gson.fromJson(json, type) as? T
    } catch (_: JsonSyntaxException) {
        null
    } catch (_: Exception) {
        null
    }
}

/**
 * [Class] 版，便于 Java 或仅有 `Class<T>` 的场景。
 */
fun <T> KeyValueStore.getObject(
    key: String,
    clazz: Class<T>,
    gson: Gson = KeyValueJson.defaultGson,
): T? {
    val json = getString(key) ?: return null
    return try {
        gson.fromJson(json, clazz)
    } catch (_: Exception) {
        null
    }
}
