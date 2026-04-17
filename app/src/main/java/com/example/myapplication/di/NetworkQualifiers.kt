package com.example.myapplication.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.Retention

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HttpBinRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class JsonPlaceholderRetrofit
