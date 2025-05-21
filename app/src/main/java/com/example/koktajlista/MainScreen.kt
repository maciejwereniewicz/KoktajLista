package com.example.koktajlista

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

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