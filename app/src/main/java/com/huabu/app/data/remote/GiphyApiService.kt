package com.huabu.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class GiphySearchResponse(
    val data: List<GiphyResult> = emptyList(),
    val pagination: GiphyPagination = GiphyPagination()
)

data class GiphyPagination(
    val count: Int = 0,
    val offset: Int = 0,
    val total_count: Int = 0
)

data class GiphyResult(
    val id: String = "",
    val title: String = "",
    val images: GiphyImages = GiphyImages()
) {
    val gifUrl: String get() = images.original?.url
        ?: images.fixed_height?.url
        ?: ""
    
    val previewUrl: String get() = images.fixed_height_small?.url
        ?: images.preview_gif?.url
        ?: images.fixed_height?.url
        ?: gifUrl
}

data class GiphyImages(
    val original: GiphyImageData? = null,
    val fixed_height: GiphyImageData? = null,
    val fixed_height_small: GiphyImageData? = null,
    val preview_gif: GiphyImageData? = null,
    val downsized: GiphyImageData? = null
)

data class GiphyImageData(
    val url: String = "",
    val width: String = "",
    val height: String = "",
    val size: String = ""
)

interface GiphyApiService {
    @GET("v1/gifs/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g"
    ): GiphySearchResponse

    @GET("v1/gifs/trending")
    suspend fun trending(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g"
    ): GiphySearchResponse
}
