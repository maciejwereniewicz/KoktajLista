package com.example.koktajlista

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.edit
import kotlinx.coroutines.delay

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
    val prefs = context.getSharedPreferences("CocktailPrefs", Context.MODE_PRIVATE)
    val skipSplash = prefs.getBoolean("skipSplash", false)

    var splashFinished by remember { mutableStateOf(skipSplash) }


    val activity = context as? ComponentActivity

    var currentScreen by remember { mutableStateOf(initialScreen) }
    var category by remember { mutableStateOf((initialScreen as? Screen.ItemList)?.category) }
    var drink by remember { mutableStateOf((initialScreen as? Screen.DrinkView)?.drinkId) }

    var time by remember { mutableStateOf(timeR) }
    var isRunning by remember { mutableStateOf(isRunningR) }
    var timeStart by remember { mutableStateOf(timeStartR) }
    var storedTime by remember { mutableStateOf(storedTimeR) }

    if (!splashFinished) {
        CombinedSplashScreen {
            splashFinished = true
            // Clear skip flag so splash appears only once
            prefs.edit {
                putBoolean("skipSplash", false)
            }
        }

        return
    }

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
                                is Screen.MainPage -> "KoktajLista"
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
                                    is Screen.DrinkView -> category?.let { Screen.ItemList(it) } ?: Screen.MainPage
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

            FreeCansAnimation()
        }
    }
}

@Composable
fun CombinedSplashScreen(onFinish: () -> Unit) {
    var splashFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        splashFinished = true
        onFinish()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedSplashScreen(onFinish = {}) // Nie kończy, tylko animuje
    }
}


@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var scale by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        scale = 1f
        kotlinx.coroutines.delay(2000L)
        onSplashFinished()
    }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "scaleAnim"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // <-- przesunięcie w górę
            Text(
                text = "\uD83C\uDF79",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.scale(animatedScale)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "KoktajLista",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


@Composable
fun AnimatedSplashScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val logoRef = remember {
        android.widget.TextView(context).apply {
            text = "🍹"
            textSize = 96f
            textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            alpha = 0f
        }
    }

    LaunchedEffect(Unit) {
        val scaleX = ObjectAnimator.ofFloat(logoRef, "scaleX", 0f, 2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(logoRef, "scaleY", 0f, 2f, 1f)
        val alpha = ObjectAnimator.ofFloat(logoRef, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 1500
            start()
        }

        delay(2000)
        onFinish()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    )
    {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center // Wyśrodkuj zawartość Boxa
        ) {
            // Animacja (emoji) 50dp nad środkiem
            AndroidView(
                factory = { logoRef },
                modifier = Modifier.offset(y = (-75).dp) // Przesunięcie w górę
            )
            // Tekst "KoktajLista" 50dp pod środkiem
            Column(modifier = Modifier.offset(y = 75.dp)) { // Przesunięcie w dół
                Text(
                    text = "KoktajLista",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
    }

    }
}

