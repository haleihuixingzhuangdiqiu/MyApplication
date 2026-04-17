package com.example.myapplication.network.api

import com.example.myapplication.network.dto.JsonPlaceholderPostDto
import retrofit2.http.GET

/** 演示用 REST API（公开假数据）。 */
interface JsonPlaceholderApi {

    @GET("posts")
    suspend fun listPosts(): List<JsonPlaceholderPostDto>
}
