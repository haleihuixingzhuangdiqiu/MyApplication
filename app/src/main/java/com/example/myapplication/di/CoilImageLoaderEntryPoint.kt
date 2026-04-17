package com.example.myapplication.di

import coil.ImageLoader
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.components.SingletonComponent

/** 供 [androidx.startup.Initializer] 等在 Application#onCreate 之前通过 [dagger.hilt.android.EarlyEntryPoints] 访问。 */
@EarlyEntryPoint
@InstallIn(SingletonComponent::class)
interface CoilImageLoaderEntryPoint {
    fun imageLoader(): ImageLoader
}
