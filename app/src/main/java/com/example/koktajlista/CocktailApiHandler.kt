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
                            drinkImage = URL(it.strDrinkThumb).readBytes(),
                            strImageSource = "",
                            strImageAttribution = "",
                            strCreativeCommonsConfirmed = "",
                            dateModified = "",
                            ingredients = mutableListOf<String>(),
                            measure = mutableListOf<String>(),
                            instructions = mapOf()
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

    suspend fun getDrinkById(id: Int): DrinkStruct? {
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

                        // Download image as ByteArray
                        val imageBytes: ByteArray = try {
                            URL(drink.strDrinkThumb).readBytes()
                        } catch (e: Exception) {
                            ByteArray(0) // fallback if image download fails
                        }

                        // Gather instructions in all available languages
                        val instructions = mapOf(
                            "EN" to drink.strInstructions,
                            "ES" to drink.strInstructionsES,
                            "DE" to drink.strInstructionsDE,
                            "FR" to drink.strInstructionsFR,
                            "IT" to drink.strInstructionsIT,
                            "ZH-HANS" to drink.strInstructionsZHHANS,
                            "ZH-HANT" to drink.strInstructionsZHHANT
                        ).filterValues { !it.isNullOrBlank() }

                        return@withContext DrinkStruct(
                            drinkName = drink.strDrink,
                            drinkImage = imageBytes,
                            drinkId = drink.idDrink,
                            strImageSource = drink.strImageSource,
                            strImageAttribution = drink.strImageAttribution,
                            strCreativeCommonsConfirmed = drink.strCreativeCommonsConfirmed,
                            dateModified = drink.dateModified,
                            ingredients = ingredients,
                            measure = measures,
                            instructions = instructions
                        )
                    }
                }
                return@withContext null
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }


}
