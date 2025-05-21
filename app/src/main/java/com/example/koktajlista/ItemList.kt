package com.example.koktajlista

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.koktajlista.CocktailApiHandler
import com.example.koktajlista.DrinkStruct
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemList(category: String, onDrinkClick: (DrinkStruct) -> Unit) {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<DrinkStruct>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loadedFromCache by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(category) {
        scope.launch {
            val handler = CocktailApiHandler()
            val key = "drinks_c_$category"
            val cacheFile = File(context.filesDir, "$key.json")
            val hadCache = cacheFile.exists()

            val result = handler.getDrinksByType("c", category, context)
            items = result

            if (result.isEmpty()) {
                if (hadCache) {
                    error = "Brak internetu – pokazano dane z pamięci (mogą być nieaktualne)."
                    loadedFromCache = true
                } else {
                    error = "Brak internetu i brak zapisanych danych."
                    loadedFromCache = false
                }
            } else {
                error = null
                loadedFromCache = !handler.isCacheValid(cacheFile)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(category) })
        }
    ) { paddingValues ->
        when {
            error != null && items.isNullOrEmpty() -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = error ?: "Nieznany błąd")
                }
            }

            !items.isNullOrEmpty() -> {
                Column(modifier = Modifier.padding(paddingValues)) {
                    if (loadedFromCache) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Załadowano dane z pamięci (offline)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(items!!) { drink ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { onDrinkClick(drink) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    drink.drinkImage.takeIf { it.isNotEmpty() }?.let {
                                        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .padding(8.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Text(
                                        text = drink.drinkName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> LoadingScreen()
        }
    }
}
