package com.example.koktajlista

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CocktailApi {
    @GET("list.php?c=list")
    fun fetchCategories(): Call<CategoryResponse>

    @GET("list.php?g=list")
    fun fetchGlasses(): Call<GlassResponse>

    @GET("list.php?i=list")
    fun fetchIngredients(): Call<IngredientResponse>

    @GET("list.php?a=list")
    fun fetchAlcoholics(): Call<AlcoholicResponse>


    @GET("filter.php")
    fun fetchDrinksByType(@QueryMap options: Map<String, String>): Call<DrinkResponse>

    @GET("lookup.php?i={id}")
    fun fetchById(@Path("id") id: Int): Call<Drink>
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
