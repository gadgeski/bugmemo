package com.example.bugmemo.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GistService {
    @POST("gists")
    suspend fun createGist(
        @Header("Authorization") authHeader: String,
        @Body request: GistRequest,
    ): GistResponse
}
