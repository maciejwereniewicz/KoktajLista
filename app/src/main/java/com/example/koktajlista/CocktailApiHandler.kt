package com.yourdomain.yourapp.api

import com.example.koktajlista.ApiClient
import com.example.koktajlista.DrinkStruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

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

    suspend fun getDrinksByType(type: String, value: String): List<DrinkStruct> {
        return withContext(Dispatchers.IO) {
            val call = ApiClient.cocktailApi.fetchDrinksByType(
                mapOf(type to value)
            )
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    response.body()?.drinks?.map {
                        DrinkStruct(
                            drinkName = it.strDrink,
                            drinkId = it.idDrink,
                            drinkImage = URL(it.strDrinkThumb).readBytes()
                        )
                    } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
