package com.example.app.data.network

import com.example.app.data.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/users")
    suspend fun getUsers(@Query("page") page: Int): UserResponse
} 