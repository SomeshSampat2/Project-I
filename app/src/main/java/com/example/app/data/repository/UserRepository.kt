package com.example.app.data.repository

import com.example.app.data.network.RetrofitClient
import com.example.app.data.network.ApiService

class UserRepository {
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)

    suspend fun getUsers(page: Int) = apiService.getUsers(page)
} 