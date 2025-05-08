package com.example.koktajlista

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.koktajlista.MainActivity.Companion.currentTime
import com.example.koktajlista.MainActivity.Companion.isRunning
import com.example.koktajlista.MainActivity.Companion.savedMillis
import com.example.koktajlista.MainActivity.Companion.showTimer
import com.example.koktajlista.MainActivity.Companion.startTime
import com.example.koktajlista.ui.theme.KoktajListaTheme
import com.example.koktajlista.ui.theme.Typography
import com.yourdomain.yourapp.api.CocktailApiHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        var showTimer: Boolean = false;
        var isRunning: Boolean = false;
        var startTime: Long = 0;
        var currentTime: Long = 0;
        var savedMillis: Long = 0;
    }
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
    var category by remember { mutableStateOf<String?>(null)}
    var drink by remember { mutableStateOf<Int?>(null)}

    BackHandler {
        currentScreen = when (val screen = currentScreen) {
            is Screen.ItemList -> {
                Screen.CategoryList;
            }

            is Screen.DrinkView -> {
                Screen.ItemList(category!!)
            }

            Screen.CategoryList -> TODO()
        }
    }
    ModalNavigationDrawer(
        drawerContent = {
            showTimer()
        }
    ) {
        when (val screen = currentScreen) {
            is Screen.CategoryList -> {
                CategoryList { c ->
                    category = c
                    currentScreen = Screen.ItemList(category!!)
                }
            }

            is Screen.ItemList -> {
                ItemList(screen.category) { d ->
                    drink = d.drinkId
                    currentScreen = Screen.DrinkView(drink!!)
                }
            }

            is Screen.DrinkView -> {
                DrinkView(screen.drinkId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun CategoryList(onCategoryClick: (String) -> Unit) {
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    CoroutineScope(Dispatchers.Main).launch {
        categories = CocktailApiHandler().getCategories()
    }
    Scaffold(
        topBar = {
        TopAppBar(
            title = { Text("Categories") },
        )
    }) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.padding(16.dp)) {
            items(categories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onCategoryClick(category) }) {
                    Text(text = category, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ItemList(category: String, onDrinkClick: (DrinkStruct) -> Unit) {
    var items by remember { mutableStateOf<List<DrinkStruct>>(emptyList()) }
    CoroutineScope(Dispatchers.Main).launch {
        items = CocktailApiHandler().getDrinksByType("c", category)
    }
    Scaffold(topBar = {
            TopAppBar(
                title = { Text("$category") },
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.padding(16.dp)) {
            items(items) { drink ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onDrinkClick(drink) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = drink.drinkName)
                        if (drink.drinkImage.isNotEmpty()) {
                            val bitmap = BitmapFactory.decodeByteArray(
                                drink.drinkImage,
                                0,
                                drink.drinkImage.size
                            )
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun DrinkView(drinkId: Int) {
    var drink by remember { mutableStateOf<DrinkStruct?>(null) }
    var language by remember { mutableStateOf("EN") }
    CoroutineScope(Dispatchers.Main).launch {
        drink = CocktailApiHandler().getDrinkById(drinkId)
    }

    drink?.let { d ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(d.drinkName) },
                )
            }
        )
        { paddingValues ->
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
                Row() {
                    d.instructions.forEach { it ->
                        Button(onClick = {language = it.key}) {
                            Text(text = it.key)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Instructions:", fontWeight = FontWeight.Bold)
                Text(text = d.instructions[language] ?: "No instructions provided")

                Spacer(modifier = Modifier.height(16.dp))
                Row()
                {
                    Column()
                    {
                        Text(text = "Ingredients:", fontWeight = FontWeight.Bold)
                        d.ingredients.forEach { ingredient ->
                            Text(text = ingredient)
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun showTimer() {
    var time by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var timeStart by remember { mutableStateOf( 0L )}
    var storedTime by remember { mutableStateOf( 0L )}

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1)
            time = System.currentTimeMillis() - timeStart + storedTime
        }
    }

    ModalDrawerSheet {
        Text("Stopwatch", modifier = Modifier.padding(16.dp))
        HorizontalDivider()

        Text(
            text = formatTime(time),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                isRunning = true
                startTime = System.currentTimeMillis()
            }) {
                Text("Start")
            }
            Button(onClick = { isRunning = false }) {
                Text("Stop")
            }
            Button(onClick = {
                isRunning = false
                storedTime = 0
                startTime = 0
                time = 0
            }) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return "%02d:%02d:%02d:%03d".format(hours,minutes%60,seconds %60, millis%1000)
}


sealed class Screen {
    data object CategoryList : Screen()
    data class ItemList(val category: String) : Screen()
    data class DrinkView(val drinkId: Int) : Screen()
}