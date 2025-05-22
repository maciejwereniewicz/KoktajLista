package com.example.koktajlista

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.koktajlista.ui.theme.KoktajListaTheme
import androidx.core.content.edit

sealed class Screen {
    object MainPage : Screen()
    object CategoryList : Screen()
    data class ItemList(val category: String, val bName: Boolean) : Screen()
    data class DrinkView(val drinkId: Int) : Screen()
}


class MainActivity : ComponentActivity() {
    var lastScreen: Screen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("CocktailPrefs", Context.MODE_PRIVATE)
        val screenType = sharedPreferences.getString("lastScreen", null)
        val category = sharedPreferences.getString("lastCategory", null)
        val drinkId = sharedPreferences.getInt("lastDrinkId", -1)

        lastScreen = when (screenType) {
            "ItemList" -> if (category != null) Screen.ItemList(category, false) else null
            "DrinkView" -> if (drinkId != -1) Screen.DrinkView(drinkId) else null
            "CategoryList" -> Screen.CategoryList
            else -> Screen.MainPage
        }

        val timeStartR = sharedPreferences.getLong("timeStartR", 0L)
        val isRunningR = sharedPreferences.getBoolean("isRunningR", false)
        val timeR = sharedPreferences.getLong("timeR", 0L)
        val storedTimeR = sharedPreferences.getLong("storedTimeR", 0L)

        enableEdgeToEdge()
        setContent {
            KoktajListaTheme {
                MainScreen(
                    initialScreen = lastScreen ?: Screen.MainPage,
                    timeR = timeR,
                    isRunningR = isRunningR,
                    timeStartR = timeStartR,
                    storedTimeR = storedTimeR
                ) { screen ->
                    lastScreen = screen
                    val prefs = getSharedPreferences("CocktailPrefs", Context.MODE_PRIVATE)
                    prefs.edit {
                        putString(
                            "lastScreen", when (screen) {
                                is Screen.ItemList -> "ItemList"
                                is Screen.DrinkView -> "DrinkView"
                                is Screen.CategoryList -> "CategoryList"
                                else -> "WelcomePage"
                            }
                        )
                        (screen as? Screen.ItemList)?.let { putString("lastCategory", it.category) }
                        (screen as? Screen.DrinkView)?.let { putInt("lastDrinkId", it.drinkId) }
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        val prefs = getSharedPreferences("CocktailPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean("skipSplash", true)
        }
    }

}
