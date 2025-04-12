package com.example.koktajlista

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koktajlista.ui.theme.KoktajListaTheme
import com.yourdomain.yourapp.api.ApiClient
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
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    if (selectedCategory == null) {
        CategoryList { category ->
            selectedCategory = category
        }
    } else {
        ItemList(selectedCategory!!) {
            selectedCategory = null
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

@Composable
fun ItemList(category: String, onBackClick: () -> Unit) {
    val items = listOf("Item 1 in $category", "Item 2 in $category", "Item 3 in $category")

    Scaffold(topBar = { Text("$category Items")  }) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.padding(16.dp)) {
            items(items) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(text = item, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
