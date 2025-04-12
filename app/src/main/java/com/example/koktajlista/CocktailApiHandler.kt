package com.yourdomain.yourapp.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import retrofit2.Call


class CocktailApiHandler {
    suspend fun getCategories(): List<String> {
        return withContext(Dispatchers.IO) {
            val call = ApiClient.cocktailApi.fetchCategories()
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    response.body()?.drinks?.map { it.strCategory } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}