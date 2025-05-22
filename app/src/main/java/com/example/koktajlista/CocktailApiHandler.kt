package com.example.koktajlista

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type
import java.net.URL
import java.net.UnknownHostException

class CocktailApiHandler {

    private val cacheDurationMillis = 60 * 60 * 1000L

    private fun getCacheFile(context: Context, key: String): File {
        return File(context.filesDir, "${key}.json")
    }

    fun isCacheValid(file: File): Boolean {
        return file.exists() && System.currentTimeMillis() - file.lastModified() < cacheDurationMillis
    }

    private fun saveCache(file: File, data: String) {
        try {
            file.writeText(data)
        } catch (e: Exception) {
            Log.e("CocktailApiHandler", "Error writing cache: ${e.message}")
        }
    }

    private fun loadCache(file: File): String? {
        return try {
            file.readText()
        } catch (e: Exception) {
            Log.e("CocktailApiHandler", "Error reading cache: ${e.message}")
            null
        }
    }

    suspend fun getCategories(context: Context): List<String> {
        val key = "categories"
        val cacheFile = getCacheFile(context, key)
        if (isCacheValid(cacheFile)) {
            val cached = loadCache(cacheFile)
            if (cached != null) {
                val listType: Type = object : TypeToken<List<String>>() {}.type
                return Gson().fromJson(cached, listType)
            }
        }

        return withContext(Dispatchers.IO) {
            val call = ApiClient.cocktailApi.fetchCategories()
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val categories = response.body()?.drinks?.map { it.strCategory } ?: emptyList()
                    saveCache(cacheFile, Gson().toJson(categories))
                    categories
                } else {
                    emptyList()
                }
            } catch (e: UnknownHostException) {
                Log.e("CocktailApiHandler", "No internet connection: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e("CocktailApiHandler", "API error: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getDrinksByName(type: String, value: String, context: Context): List<DrinkStruct> {
        val key = "drinks_${type}_$value"
        val cacheFile = getCacheFile(context, key)
        if (isCacheValid(cacheFile)) {
            val cached = loadCache(cacheFile)
            if (cached != null) {
                val listType: Type = object : TypeToken<List<DrinkStruct>>() {}.type
                return Gson().fromJson(cached, listType)
            }
        }

        return withContext(Dispatchers.IO) {
            val call = ApiClient.cocktailApi.fetchDrinksByName(mapOf(type to value))
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val drinks = response.body()?.drinks?.map {
                        DrinkStruct(
                            drinkName = it.strDrink,
                            drinkId = it.idDrink,
                            drinkImage = URL(it.strDrinkThumb).readBytes(),
                            strImageSource = "",
                            strImageAttribution = "",
                            strCreativeCommonsConfirmed = "",
                            dateModified = "",
                            ingredients = mutableListOf(),
                            measure = mutableListOf(),
                            instructions = mapOf()
                        )
                    } ?: emptyList()
                    saveCache(cacheFile, Gson().toJson(drinks))
                    drinks
                } else {
                    emptyList()
                }
            } catch (e: UnknownHostException) {
                Log.e("CocktailApiHandler", "No internet connection: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e("CocktailApiHandler", "API error: ${e.message}")
                emptyList()
            }
        }
    }


    suspend fun getDrinksByType(type: String, value: String, context: Context): List<DrinkStruct> {
        val key = "drinks_${type}_$value"
        val cacheFile = getCacheFile(context, key)
        if (isCacheValid(cacheFile)) {
            val cached = loadCache(cacheFile)
            if (cached != null) {
                val listType: Type = object : TypeToken<List<DrinkStruct>>() {}.type
                return Gson().fromJson(cached, listType)
            }
        }

        return withContext(Dispatchers.IO) {
            val call = ApiClient.cocktailApi.fetchDrinksByType(mapOf(type to value))
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val drinks = response.body()?.drinks?.map {
                        DrinkStruct(
                            drinkName = it.strDrink,
                            drinkId = it.idDrink,
                            drinkImage = URL(it.strDrinkThumb).readBytes(),
                            strImageSource = "",
                            strImageAttribution = "",
                            strCreativeCommonsConfirmed = "",
                            dateModified = "",
                            ingredients = mutableListOf(),
                            measure = mutableListOf(),
                            instructions = mapOf()
                        )
                    } ?: emptyList()
                    saveCache(cacheFile, Gson().toJson(drinks))
                    drinks
                } else {
                    emptyList()
                }
            } catch (e: UnknownHostException) {
                Log.e("CocktailApiHandler", "No internet connection: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e("CocktailApiHandler", "API error: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getDrinkById(id: Int, context: Context): DrinkStruct? {
        val key = "drink_$id"
        val cacheFile = getCacheFile(context, key)
        if (isCacheValid(cacheFile)) {
            val cached = loadCache(cacheFile)
            if (cached != null) {
                return Gson().fromJson(cached, DrinkStruct::class.java)
            }
        }

        return withContext(Dispatchers.IO) {
            val call = ApiClient.cocktailApi.fetchById(id)
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val drink = response.body()?.drinks?.firstOrNull()
                    if (drink != null) {
                        val ingredients = mutableListOf<String>()
                        val measures = mutableListOf<String>()

                        for (i in 1..15) {
                            val ingredientField = drink::class.java.getDeclaredField("strIngredient$i")
                            val measureField = drink::class.java.getDeclaredField("strMeasure$i")
                            ingredientField.isAccessible = true
                            measureField.isAccessible = true

                            val ingredient = ingredientField.get(drink) as? String
                            val measure = measureField.get(drink) as? String

                            if (!ingredient.isNullOrBlank()) {
                                ingredients.add(ingredient)
                                measures.add(measure ?: "")
                            }
                        }

                        val imageBytes: ByteArray = try {
                            URL(drink.strDrinkThumb).readBytes()
                        } catch (e: Exception) {
                            ByteArray(0)
                        }

                        val instructions = mapOf(
                            "EN" to drink.strInstructions,
                            "ES" to drink.strInstructionsES,
                            "DE" to drink.strInstructionsDE,
                            "FR" to drink.strInstructionsFR,
                            "IT" to drink.strInstructionsIT,
                            "ZH-HANS" to drink.strInstructionsZHHANS,
                            "ZH-HANT" to drink.strInstructionsZHHANT
                        ).filterValues { !it.isNullOrBlank() }

                        val drinkStruct = DrinkStruct(
                            drinkName = drink.strDrink?:"",
                            drinkImage = imageBytes?: ByteArray(0),
                            drinkId = drink.idDrink?: 0,
                            strImageSource = drink.strImageSource?: "",
                            strImageAttribution = drink.strImageAttribution?: "",
                            strCreativeCommonsConfirmed = drink.strCreativeCommonsConfirmed?: "",
                            dateModified = drink.dateModified?: "",
                            ingredients = ingredients?: mutableListOf(),
                            measure = measures?: mutableListOf(),
                            instructions = instructions?: mapOf()
                        )

                        saveCache(cacheFile, Gson().toJson(drinkStruct))
                        return@withContext drinkStruct
                    }
                }
                return@withContext null
            } catch (e: UnknownHostException) {
                Log.e("CocktailApiHandler", "No internet connection: ${e.message}")
                return@withContext null
            } catch (e: Exception) {
                Log.e("CocktailApiHandler", "API error: ${e.message}")
                return@withContext null
            }
        }
    }
}