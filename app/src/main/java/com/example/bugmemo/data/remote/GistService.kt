package com.example.bugmemo.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GistService {
    /**
     * 新しい Gist を作成する
     * @param authHeader "token YOUR_ACCESS_TOKEN" 形式の認証ヘッダー
     * @param request 作成するGistの内容
     */
    @POST("gists")
    suspend fun createGist(
        @Header("Authorization") authHeader: String,
        @Body request: GistRequest,
    ): GistResponse
}
