package com.yourdomain.yourapp.api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Category(
    val strCategory: String
)

data class CategoryResponse(
    val drinks: List<Category>
)

interface CocktailApi {
    @GET("list.php?c=list")
    fun fetchCategories(): Call<CategoryResponse>
}

object ApiClient {
    private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

    val cocktailApi: CocktailApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CocktailApi::class.java)
    }
}
