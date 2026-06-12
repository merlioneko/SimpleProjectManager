package com.aqualion.vani.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aqualion.vani.ui.detailscreen.ProjectDetailScreen
import com.aqualion.vani.ui.listscreen.ListScreenPreview
import com.aqualion.vani.ui.listscreen.ProjectListScreen
import com.aqualion.vani.ui.theme.VaniTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaniTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.PROJECT_LIST.name,
        modifier = Modifier,
    ) {
        composable(
            Screen.PROJECT_LIST.name
        ) {
            ProjectListScreen(onProjectSelected = { project ->
                navController.navigate("${Screen.PROJECT_DETAIL.name}/${project.id}")
            })
        }
        composable(
            route = "${Screen.PROJECT_DETAIL.name}/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.IntType }),
            enterTransition = {
                slideInHorizontally(animationSpec = tween(300, easing = LinearEasing)) {
                    it / 2
                } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(300, easing = LinearEasing)) {
                    it / 2
                } + fadeOut()
            }
        ) {
            ProjectDetailScreen()
        }
    }
}

enum class Screen {
    PROJECT_LIST,
    PROJECT_DETAIL
}


@Preview
@Composable
fun MainScreenPreview() {
    VaniTheme {
        ListScreenPreview()
    }
}
