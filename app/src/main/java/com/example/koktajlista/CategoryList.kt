package com.example.koktajlista

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.koktajlista.CocktailApiHandler
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryList(onCategoryClick: (String) -> Unit) {
    val context = LocalContext.current
    var categories by remember { mutableStateOf<List<String>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loadedFromCache by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val handler = CocktailApiHandler()
            val key = "categories"
            val cacheFile = File(context.filesDir, "$key.json")
            val hadCache = cacheFile.exists()

            val result = handler.getCategories(context)
            categories = result

            if (result.isEmpty()) {
                if (hadCache) {
                    error = "Brak internetu – dane mogą być nieaktualne."
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
            TopAppBar(title = { Text("Kategorie") })
        }
    ) { paddingValues ->
        when {
            error != null && categories.isNullOrEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Nieznany błąd")
                }
            }
            !categories.isNullOrEmpty() -> {
                Column(modifier = Modifier.padding(paddingValues)) {
                    if (loadedFromCache) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(8.dp),
                        ) {
                            Text(
                                text = "Załadowano dane z pamięci (offline)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(categories!!) { category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { onCategoryClick(category) }
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            else -> LoadingScreen()
        }
    }
}
