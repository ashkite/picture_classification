package com.ashkite.pictureclassification.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onPlaceClick = { cityId -> navController.navigate(Routes.place(cityId)) },
                onDateClick = { date -> navController.navigate(Routes.date(date)) },
                onTagClick = { type, tagId -> navController.navigate(Routes.tag(type, tagId)) },
                onUnknownClick = { navController.navigate(Routes.unknownAll()) },
                onUnknownDateClick = { date -> navController.navigate(Routes.unknownDate(date)) }
            )
        }
        composable(
            route = Routes.PLACE,
            arguments = listOf(navArgument("cityId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getLong("cityId") ?: 0L
            PlaceDetailScreen(cityId = cityId, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.DATE,
            arguments = listOf(navArgument("localDate") { type = NavType.StringType })
        ) { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("localDate").orEmpty()
            DateDetailScreen(localDate = Uri.decode(raw), onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.UNKNOWN_ALL
        ) {
            UnknownDetailScreen(localDate = null, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.UNKNOWN_DATE,
            arguments = listOf(navArgument("localDate") { type = NavType.StringType })
        ) { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("localDate").orEmpty()
            UnknownDetailScreen(localDate = Uri.decode(raw), onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.TAG,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("tagId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type").orEmpty()
            val tagId = backStackEntry.arguments?.getLong("tagId") ?: 0L
            TagDetailScreen(tagType = type, tagId = tagId, onBack = { navController.popBackStack() })
        }
    }
}

object Routes {
    const val HOME = "home"
    const val PLACE = "place/{cityId}"
    const val DATE = "date/{localDate}"
    const val UNKNOWN_ALL = "unknown/all"
    const val UNKNOWN_DATE = "unknown/{localDate}"
    const val TAG = "tag/{type}/{tagId}"

    fun place(cityId: Long) = "place/$cityId"
    fun date(localDate: String) = "date/${Uri.encode(localDate)}"
    fun unknownAll() = UNKNOWN_ALL
    fun unknownDate(localDate: String) = "unknown/${Uri.encode(localDate)}"
    fun tag(type: String, tagId: Long) = "tag/${Uri.encode(type)}/$tagId"
}
