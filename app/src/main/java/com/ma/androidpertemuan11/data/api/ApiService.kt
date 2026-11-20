package com.ma.androidpertemuan11.data.api

import com.ma.androidpertemuan11.data.model.Post
import com.ma.androidpertemuan11.data.model.PostRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: String): Post

    @POST("posts")
    suspend fun createPost(@Body request: PostRequest): Response<Post>

    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body request: PostRequest): Response<Post>

    @PATCH("posts/{id}")
    suspend fun patchPost(@Path("id") id: String, @Body request: PostRequest): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<ResponseBody>
}
