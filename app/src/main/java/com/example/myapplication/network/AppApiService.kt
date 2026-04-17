package com.example.myapplication.network

import com.google.gson.JsonElement
import retrofit2.http.GET

/** 示例 Retrofit 接口，可按业务拆分 module / Service。 */
interface AppApiService {

    @GET("get")
    suspend fun sampleGet(): JsonElement
}
