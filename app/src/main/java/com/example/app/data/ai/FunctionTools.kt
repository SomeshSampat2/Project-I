package com.example.app.data.ai

import com.example.app.data.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

object FunctionTools {
    private val userRepository = UserRepository()

    suspend fun fetchUsers(page: Int): JSONObject {
        val response = userRepository.getUsers(page)
        return JSONObject().apply {
            put("users", JSONArray().apply {
                response.data.forEach { user ->
                    put(JSONObject().apply {
                        put("name", "${user.first_name} ${user.last_name}")
                        put("email", user.email)
                        put("avatar", user.avatar)
                    })
                }
            })
        }
    }
} 