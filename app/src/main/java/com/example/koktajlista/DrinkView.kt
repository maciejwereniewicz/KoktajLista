package com.example.koktajlista

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
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
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.FlowColumn
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow


@SuppressLint("QueryPermissionsNeeded")
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

    // Scaffold state do wyświetlania Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

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

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val message = drink!!.ingredients.joinToString(separator = ", ", prefix = "Składniki: ")
                    val smsUri = Uri.parse("smsto:") // numer zostaw pusty, żeby użytkownik wpisał sam
                    val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                        putExtra("sms_body", message)
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Wyślij SMS"
                )
            }
        }

    ) { paddingValues ->

        val modifier = Modifier.padding(paddingValues).fillMaxSize()

        when {
            screenWidthDp < 600 -> {
                DrinkViewPhone(
                    drink = drink!!,
                    language = language,
                    onLanguageChange = { language = it },
                    loadedFromCache = loadedFromCache,
                    flagEmojiMap = flagEmojiMap,
                    modifier = modifier
                )
            }

            screenHeightDp < 800 -> {
                DrinkViewPhoneVertical(
                    drink = drink!!,
                    language = language,
                    onLanguageChange = { language = it },
                    loadedFromCache = loadedFromCache,
                    flagEmojiMap = flagEmojiMap,
                    modifier = modifier
                )
            }

            else -> {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkViewPhone(
    drink: DrinkStruct,
    language: String,
    onLanguageChange: (String) -> Unit,
    loadedFromCache: Boolean,
    flagEmojiMap: Map<String, String>,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberLazyListState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    LanguageSelectorHorizontalWrapped(
                        availableLanguages = drink.instructions.keys.toList(),
                        selectedLanguage = language,
                        onLanguageChange = onLanguageChange,
                        flagEmojiMap = flagEmojiMap
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize()
        ) {
            if (drink.drinkImage.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(drink.drinkImage, 0, drink.drinkImage.size)
                item {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Image of ${drink.drinkName}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Język instrukcji:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

            }

            // Cache info
            if (loadedFromCache) {
                item {
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
            }

            // Instrukcje
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Instructions:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Text(
                    text = drink.instructions[language] ?: "No instructions provided",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Składniki
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Ingredients:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            }

            items(drink.ingredients) { ingredient ->
                Text(
                    text = ingredient,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Wybór języka

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}



@Composable
fun DrinkViewPhoneVertical(
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
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Image of ${drink.drinkName}",
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Center pane: Instructions + ingredients
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
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
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right pane: Language flags
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            LanguageSelectorVerticalWrapped(
                availableLanguages = drink.instructions.keys.toList(),
                selectedLanguage = language,
                onLanguageChange = onLanguageChange,
                flagEmojiMap = flagEmojiMap
            )
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
        // Left pane: image + cache info + flags
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

            // Flags below image
            LanguageSelectorHorizontalWrapped(
                availableLanguages = drink.instructions.keys.toList(),
                selectedLanguage = language,
                onLanguageChange = onLanguageChange,
                flagEmojiMap = flagEmojiMap
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Center pane: scrollable content
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
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

@Composable
fun LanguageSelectorHorizontalWrapped(
    availableLanguages: List<String>,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    flagEmojiMap: Map<String, String>
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        availableLanguages.forEach { lang ->
            val isSelected = lang == selectedLanguage
            val label = lang.uppercase()
            val flag = flagEmojiMap[lang] ?: ""

            Button(
                onClick = { onLanguageChange(lang) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "$label $flag")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageSelectorVerticalWrapped(
    availableLanguages: List<String>,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    flagEmojiMap: Map<String, String>
) {
    FlowColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        availableLanguages.forEach { lang ->
            val isSelected = lang == selectedLanguage
            val label = lang.uppercase()
            val flag = flagEmojiMap[lang] ?: ""

            Button(
                onClick = { onLanguageChange(lang) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "$label $flag")
            }
        }
    }
}


