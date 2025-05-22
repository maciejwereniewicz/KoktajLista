package com.example.koktajlista

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface CocktailApi {
    @GET("list.php?c=list")
    fun fetchCategories(): Call<CategoryResponse>

    @GET("search.php")
    fun fetchDrinksByName(@QueryMap options: Map<String, String>): Call<DrinkResponse>

    @GET("filter.php")
    fun fetchDrinksByType(@QueryMap options: Map<String, String>): Call<DrinkResponse>

    @GET("lookup.php")
    fun fetchById(@Query("i") id: Int): Call<LongDrinkResponse>
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
