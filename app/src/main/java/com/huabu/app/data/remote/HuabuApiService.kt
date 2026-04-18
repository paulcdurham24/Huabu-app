package com.huabu.app.data.remote

import com.huabu.app.data.model.Post
import com.huabu.app.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface HuabuApiService {

    @GET("feed")
    suspend fun getFeed(
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<Post>>

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<User>

    @GET("users/{userId}/posts")
    suspend fun getUserPosts(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0
    ): Response<List<Post>>

    @GET("users/{userId}/friends")
    suspend fun getFriends(@Path("userId") userId: String): Response<List<User>>

    @POST("posts")
    suspend fun createPost(@Body post: Post): Response<Post>

    @POST("users/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String): Response<Unit>

    @DELETE("users/{userId}/follow")
    suspend fun unfollowUser(@Path("userId") userId: String): Response<Unit>

    @GET("search/users")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>
}
