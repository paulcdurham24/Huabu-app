package com.huabu.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class YouTubeSearchResponse(
    val items: List<YouTubeSearchItem> = emptyList()
)

data class YouTubeSearchItem(
    val id: YouTubeItemId = YouTubeItemId(),
    val snippet: YouTubeSnippet = YouTubeSnippet()
)

data class YouTubeItemId(
    val videoId: String = ""
)

data class YouTubeSnippet(
    val title: String = "",
    @SerializedName("channelTitle") val channelTitle: String = "",
    val thumbnails: YouTubeThumbnails = YouTubeThumbnails()
)

data class YouTubeThumbnails(
    val default: YouTubeThumbnail = YouTubeThumbnail()
)

data class YouTubeThumbnail(
    val url: String = ""
)

interface YouTubeApiService {
    @GET("search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String = "10",
        @Query("maxResults") maxResults: Int = 5,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse
}
