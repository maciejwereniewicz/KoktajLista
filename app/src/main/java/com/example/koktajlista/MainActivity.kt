package com.example.koktajlista

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.koktajlista.ui.theme.KoktajListaTheme
import kotlinx.coroutines.launch
import androidx.core.content.edit
import androidx.compose.ui.draw.rotate
import java.io.File

class MainActivity : ComponentActivity() {
    var lastScreen: Screen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("CocktailPrefs", Context.MODE_PRIVATE)
        val screenType = sharedPreferences.getString("lastScreen", null)
        val category = sharedPreferences.getString("lastCategory", null)
        val drinkId = sharedPreferences.getInt("lastDrinkId", -1)

        lastScreen = when (screenType) {
            "ItemList" -> if (category != null) Screen.ItemList(category) else null
            "DrinkView" -> if (drinkId != -1) Screen.DrinkView(drinkId) else null
            else -> Screen.CategoryList
        }

        val timeStartR = sharedPreferences.getLong("timeStartR", 0L)
        val isRunningR = sharedPreferences.getBoolean("isRunningR", false)
        val timeR = sharedPreferences.getLong("timeR", 0L)
        val storedTimeR = sharedPreferences.getLong("storedTimeR", 0L)

        enableEdgeToEdge()
        setContent {
            KoktajListaTheme {
                MainScreen(
                    initialScreen = lastScreen ?: Screen.CategoryList,
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
                                else -> "CategoryList"
                            }
                        )
                        (screen as? Screen.ItemList)?.let { putString("lastCategory", it.category) }
                        (screen as? Screen.DrinkView)?.let { putInt("lastDrinkId", it.drinkId) }
                    }
                }
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun MainScreen(
    initialScreen: Screen,
    timeR: Long,
    isRunningR: Boolean,
    timeStartR: Long,
    storedTimeR: Long,
    onScreenChange: (Screen) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var currentScreen by remember { mutableStateOf(initialScreen) }
    var category by remember { mutableStateOf((initialScreen as? Screen.ItemList)?.category) }
    var drink by remember { mutableStateOf((initialScreen as? Screen.DrinkView)?.drinkId) }

    var time by remember { mutableStateOf(timeR) }
    var isRunning by remember { mutableStateOf(isRunningR) }
    var timeStart by remember { mutableStateOf(timeStartR) }
    var storedTime by remember { mutableStateOf(storedTimeR) }

    LaunchedEffect(currentScreen) {
        onScreenChange(currentScreen)
    }

    LaunchedEffect(time, isRunning, timeStart, storedTime) {
        context.getSharedPreferences("CocktailPrefs", Context.MODE_PRIVATE).edit {
            putLong("timeR", time)
            putBoolean("isRunningR", isRunning)
            putLong("timeStartR", timeStart)
            putLong("storedTimeR", storedTime)
        }
    }

    BackHandler {
        currentScreen = when (currentScreen) {
            is Screen.ItemList -> Screen.CategoryList
            is Screen.DrinkView -> category?.let { Screen.ItemList(it) } ?: Screen.CategoryList
            Screen.CategoryList -> {
                activity?.finish()
                return@BackHandler
            }
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            ShowTimer(
                timeR = time,
                isRunningR = isRunning,
                timeStartR = timeStart,
                storedTimeR = storedTime
            ) { newTime, newIsRunning, newTimeStart, newStoredTime ->
                time = newTime
                isRunning = newIsRunning
                timeStart = newTimeStart
                storedTime = newStoredTime
            }
        }
    ) {
        when (val screen = currentScreen) {
            is Screen.CategoryList -> CategoryList {
                category = it
                currentScreen = Screen.ItemList(it)
            }

            is Screen.ItemList -> ItemList(screen.category) {
                drink = it.drinkId
                category = screen.category
                currentScreen = Screen.DrinkView(it.drinkId)
            }

            is Screen.DrinkView -> DrinkView(screen.drinkId)
        }
    }
}

@Composable
fun LoadingScreen() {
    var rotation by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            rotation += 10f
            kotlinx.coroutines.delay(50)
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⌛",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            modifier = Modifier.rotate(rotation)
        )
    }
}

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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkView(drinkId: Int) {
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

    when {
        error != null && drink == null -> {
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
        }

        drink != null -> {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text(drink!!.drinkName) })
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize(),
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

                    if (drink!!.drinkImage.isNotEmpty()) {
                        val bitmap = BitmapFactory.decodeByteArray(drink!!.drinkImage, 0, drink!!.drinkImage.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Image of ${drink!!.drinkName}",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        drink!!.instructions.forEach {
                            Button(onClick = { language = it.key }) {
                                Text(text = it.key)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Instructions:", fontWeight = FontWeight.Bold)
                    Text(text = drink!!.instructions[language] ?: "No instructions provided")

                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(text = "Ingredients:", fontWeight = FontWeight.Bold)
                        drink!!.ingredients.forEach { ingredient ->
                            Text(text = ingredient)
                        }
                    }
                }
            }
        }

        else -> LoadingScreen()
    }
}


sealed class Screen {
    data object CategoryList : Screen()
    data class ItemList(val category: String) : Screen()
    data class DrinkView(val drinkId: Int) : Screen()
}
