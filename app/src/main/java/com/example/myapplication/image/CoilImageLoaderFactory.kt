package com.example.myapplication.image

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.example.myapplication.BuildConfig
import okhttp3.OkHttpClient

/**
 * Coil 与 OkHttp 磁盘缓存目录分离；内存按进程比例、磁盘约 128MB（可按业务调整）。
 */
object CoilImageLoaderFactory {

    fun create(context: Context, okHttpClient: OkHttpClient): ImageLoader =
        ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .respectCacheHeaders(true)
            .crossfade(280)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.22)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_image_cache"))
                    .maxSizeBytes(128L * 1024 * 1024)
                    .build()
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
}
