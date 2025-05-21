package com.example.koktajlista

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkView(
    drinkId: Int
) {
    val context = LocalContext.current
    var drink by remember { mutableStateOf<DrinkStruct?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var language by remember { mutableStateOf("EN") }
    var loadedFromCache by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(drinkId) {
        scope.launch {
            val handler = CocktailApiHandler()
            val key = "drink_$drinkId"
            val cacheFile = File(context.filesDir, "$key.json")
            val hadCache = cacheFile.exists()

            val result = handler.getDrinkById(drinkId, context)
            drink = result

            if (result == null) {
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

    val flagEmojiMap = mapOf(
        "EN" to "🇬🇧",
        "ES" to "🇪🇸",
        "DE" to "🇩🇪",
        "FR" to "🇫🇷",
        "IT" to "🇮🇹"
    )

    // Get configuration to detect orientation and screen width
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    if (error != null && drink == null) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Szczegóły drinka") })
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "Nieznany błąd")
            }
        }
        return
    }

    if (drink == null) {
        LoadingScreen()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(drink!!.drinkName) })
        }
    ) { paddingValues ->
        // Decide layout based on screen width and orientation
        val modifier = Modifier.padding(paddingValues).fillMaxSize()

        when {
            screenWidthDp < 600 -> {
                // Phone layout
                DrinkViewPhone(
                    drink = drink!!,
                    language = language,
                    onLanguageChange = { language = it },
                    loadedFromCache = loadedFromCache,
                    flagEmojiMap = flagEmojiMap,
                    modifier = modifier
                )
            }

            else -> {
                // Tablet or large screen layout
                DrinkViewTablet(
                    drink = drink!!,
                    language = language,
                    onLanguageChange = { language = it },
                    loadedFromCache = loadedFromCache,
                    flagEmojiMap = flagEmojiMap,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun DrinkViewPhone(
    drink: DrinkStruct,
    language: String,
    onLanguageChange: (String) -> Unit,
    loadedFromCache: Boolean,
    flagEmojiMap: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) {
        // Language selector in vertical column on the left
        Column(
            modifier = Modifier.width(80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguageSelectorVertical(
                availableLanguages = drink.instructions.keys.toList(),
                selectedLanguage = language,
                onLanguageChange = onLanguageChange,
                flagEmojiMap = flagEmojiMap
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Main content scrollable column
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (drink.drinkImage.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(drink.drinkImage, 0, drink.drinkImage.size)
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Image of ${drink.drinkName}",
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Instructions:", fontWeight = FontWeight.Bold)
            Text(text = drink.instructions[language] ?: "No instructions provided")

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Ingredients:", fontWeight = FontWeight.Bold)
            drink.ingredients.forEach { ingredient ->
                Text(text = ingredient)
            }
        }
    }
}

@Composable
fun DrinkViewTablet(
    drink: DrinkStruct,
    language: String,
    onLanguageChange: (String) -> Unit,
    loadedFromCache: Boolean,
    flagEmojiMap: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Left pane: Image + cache info
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (drink.drinkImage.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(drink.drinkImage, 0, drink.drinkImage.size)
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Image of ${drink.drinkName}",
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Right pane: Instructions + Ingredients + language selector below ingredients
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "Instructions:", fontWeight = FontWeight.Bold)
            Text(text = drink.instructions[language] ?: "No instructions provided")

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Ingredients:", fontWeight = FontWeight.Bold)
            drink.ingredients.forEach { ingredient ->
                Text(text = ingredient)
            }

            Spacer(modifier = Modifier.height(24.dp))

            LanguageSelectorHorizontal(
                availableLanguages = drink.instructions.keys.toList(),
                selectedLanguage = language,
                onLanguageChange = onLanguageChange,
                flagEmojiMap = flagEmojiMap
            )
        }
    }
}

// Vertical language selector for portrait phones (compact vertical buttons)
@Composable
fun LanguageSelectorVertical(
    availableLanguages: List<String>,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    flagEmojiMap: Map<String, String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        availableLanguages.forEach { langCode ->
            Button(
                onClick = { onLanguageChange(langCode) },
                colors = if (langCode == selectedLanguage)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else
                    ButtonDefaults.buttonColors()
            ) {
                Text(text = flagEmojiMap[langCode] ?: langCode, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Horizontal language selector for landscape/tablets (normal row of buttons)
@Composable
fun LanguageSelectorHorizontal(
    availableLanguages: List<String>,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    flagEmojiMap: Map<String, String>
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        availableLanguages.forEach { langCode ->
            Button(
                onClick = { onLanguageChange(langCode) },
                colors = if (langCode == selectedLanguage)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                else
                    ButtonDefaults.buttonColors()
            ) {
                Text(text = langCode)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = flagEmojiMap[langCode] ?: "")
            }
        }
    }
}
