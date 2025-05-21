package com.example.koktajlista

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
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
            Screen.CategoryList -> Screen.MainPage
            Screen.MainPage -> {
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
                storedTimeR = storedTime,
            ) { newTime, newIsRunning, newTimeStart, newStoredTime ->
                time = newTime
                isRunning = newIsRunning
                timeStart = newTimeStart
                storedTime = newStoredTime
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                is Screen.MainPage -> "Strona główna"
                                is Screen.CategoryList -> "Kategorie"
                                is Screen.ItemList -> "Lista koktajli"
                                is Screen.DrinkView -> "Szczegóły koktajlu"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        if (currentScreen != Screen.MainPage) {
                            IconButton(onClick = {
                                currentScreen = when (currentScreen) {
                                    is Screen.ItemList -> Screen.CategoryList
                                    is Screen.DrinkView -> category?.let { Screen.ItemList(it) } ?: Screen.CategoryList
                                    is Screen.CategoryList -> Screen.MainPage
                                    else -> Screen.MainPage
                                }
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
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
                    Screen.MainPage -> WelcomeScreen(onContinue = {
                        currentScreen = Screen.CategoryList
                    })
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Witamy w KoktajLista!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aplikacja służy do przeglądania przepisów na różnorodne koktajle i napoje. " +
                        "Możesz przejrzeć kategorie, wybrać interesujący Cię koktajl oraz zobaczyć szczegóły przepisu.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Zaczynamy! \uD83C\uDF7A")
            }
        }
    }
}
