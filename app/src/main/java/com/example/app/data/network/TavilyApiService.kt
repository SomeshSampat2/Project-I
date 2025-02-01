package com.example.app.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface TavilyApiService {
    @POST("search")
    suspend fun search(@Body request: SearchRequest): SearchResponse
}

data class SearchRequest(
    @SerializedName("api_key") val apiKey: String = "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",
    val query: String,
    @SerializedName("search_depth") val searchDepth: String = "advanced",
    @SerializedName("include_answer") val includeAnswer: Boolean = true,
    @SerializedName("include_raw_content") val includeRawContent: Boolean = false,
    @SerializedName("include_images") val includeImages: Boolean = false,
    @SerializedName("max_results") val maxResults: Int = 5,
    @SerializedName("include_domains") val includeDomains: List<String>? = null,
    @SerializedName("exclude_domains") val excludeDomains: List<String>? = null
)

data class SearchResponse(
    val query: String,
    val answer: String?,
    val results: List<SearchResult>,
    @SerializedName("search_depth") val searchDepth: String
)

data class SearchResult(
    val title: String,
    val url: String,
    val content: String,
    val score: Double,
    val published_date: String?
) 