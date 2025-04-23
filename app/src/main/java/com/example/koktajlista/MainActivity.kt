package com.example.koktajlista

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.example.koktajlista.ui.theme.KoktajListaTheme
import com.example.koktajlista.ui.theme.Typography
import com.yourdomain.yourapp.api.CocktailApiHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoktajListaTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.CategoryList) }

    when (val screen = currentScreen) {
        is Screen.CategoryList -> {
            CategoryList { category ->
                currentScreen = Screen.ItemList(category)
            }
        }
        is Screen.ItemList -> {
            ItemList(screen.category) { drink ->
                currentScreen = Screen.DrinkView(drink.drinkId)
            }
        }
        is Screen.DrinkView -> {
            DrinkView(screen.drinkId)
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun CategoryList(onCategoryClick: (String) -> Unit) {
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    CoroutineScope(Dispatchers.Main).launch {
        categories = CocktailApiHandler().getCategories()
    }
    Scaffold(topBar = { Text("Categories") }) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.padding(16.dp)) {
            items(categories) { category ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onCategoryClick(category) }) {
                    Text(text = category, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ItemList(category: String, onDrinkClick: (DrinkStruct) -> Unit) {
    var items by remember { mutableStateOf<List<DrinkStruct>>(emptyList()) }
    CoroutineScope(Dispatchers.Main).launch {
        items = CocktailApiHandler().getDrinksByType("c",category)
    }

    Scaffold(topBar = { Text("$category Items")  }) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.padding(16.dp)) {
            items(items) { drink ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onDrinkClick(drink) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = drink.drinkName)
                        if (drink.drinkImage.isNotEmpty()) {
                            val bitmap = BitmapFactory.decodeByteArray(drink.drinkImage, 0, drink.drinkImage.size)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Image of ${drink.drinkName}",
                                modifier = Modifier
                                    .height(200.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun DrinkView(drinkId: Int) {
    var drink by remember { mutableStateOf<DrinkStruct?>(null) }
    CoroutineScope(Dispatchers.Main).launch {
        drink = CocktailApiHandler().getDrinkById(drinkId)
    }

    drink?.let { d ->
        Scaffold(topBar = { Text(text = d.drinkName, style = Typography.headlineLarge) }) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (d.drinkImage.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(d.drinkImage, 0, d.drinkImage.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Image of ${d.drinkName}",
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Instructions:", fontWeight = FontWeight.Bold)
                Text(text = d.instructions["EN"] ?: "No instructions provided")

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Ingredients:", fontWeight = FontWeight.Bold)
                d.ingredients.forEach { ingredient ->
                    Text(text = ingredient)
                }
            }
        }
    }
}

sealed class Screen {
    data object CategoryList : Screen()
    data class ItemList(val category: String) : Screen()
    data class DrinkView(val drinkId: Int) : Screen()
}
