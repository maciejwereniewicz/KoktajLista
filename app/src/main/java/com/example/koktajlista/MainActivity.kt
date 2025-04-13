package com.example.koktajlista

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.koktajlista.ui.theme.KoktajListaTheme
import com.yourdomain.yourapp.api.CocktailApiHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoktajListaTheme {
                MainScreen()
            }
        }
    }
}


@Composable
fun MainScreen() {
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    if (selectedCategory == null) {
        CategoryList { category ->
            selectedCategory = category
        }
    } else {
        ItemList(selectedCategory!!)
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
fun ItemList(category: String) {
    var items by remember { mutableStateOf<List<DrinkStruct>>(emptyList()) }
    CoroutineScope(Dispatchers.Main).launch {
        items = CocktailApiHandler().getDrinksByType("c",category)
    }

    Scaffold(topBar = { Text("$category Items")  }) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.padding(16.dp)) {
            items(items) { drink ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = drink.drinkName)
                        if (drink.drinkImage.isNotEmpty()) {
                            val bitmap = BitmapFactory.decodeByteArray(drink.drinkImage, 0, drink.drinkImage.size)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Image of ${drink.drinkName}",
                                modifier = Modifier.height(200.dp).fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
