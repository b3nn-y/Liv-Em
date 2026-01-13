package com.bennysamuel.livem.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bennysamuel.livem.AppViewModel
import com.bennysamuel.livem.db.LiveEmDbUtil
import com.bennysamuel.livem.onboarding.OnboardingScreen
import com.bennysamuel.livem.ui.HomeScreen
import com.bennysamuel.livem.ui.JournalEditorScreen
import com.bennysamuel.livem.ui.ProfileSetupScreen
import com.bennysamuel.livem.user.SessionManager

@Composable
fun LiveEmApp(
    sessionManager: SessionManager
) {
    val navController = rememberNavController()


    val appViewModel: AppViewModel = viewModel { AppViewModel() }


    val startDestination = if (sessionManager.isSignedIn()) AppRoute.Home else AppRoute.Onboarding

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<AppRoute.Onboarding> {
            OnboardingScreen {
                navController.navigate(AppRoute.ProfileSetup)
            }
        }

        composable<AppRoute.ProfileSetup> {
            ProfileSetupScreen(
                onDone = { name, dob ->
                    sessionManager.signIn(name, dob)
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.Home> {
            HomeScreen(
                user = sessionManager.getUser()!!,
                onSignOut = {
                    sessionManager.signOut()
                    navController.navigate(AppRoute.Onboarding) {
                        popUpTo(AppRoute.Home) { inclusive = true }
                    }
                },
                appViewModel = appViewModel,
                enterJournalEntry = { navController.navigate(AppRoute.JournalEntry) },
                backToHome = {navController.popBackStack()}
            )
        }

        composable<AppRoute.JournalEntry> {
            JournalEditorScreen(
                onBack = { navController.popBackStack() },
                appViewModel = appViewModel
            )
        }
    }
}

//
//    when (route) {
//        AppRoute.Onboarding ->
//            OnboardingScreen {
//                route = AppRoute.ProfileSetup
//            }
//
//        AppRoute.ProfileSetup ->
//            ProfileSetupScreen(
//                onDone = { name, dob ->
//                    sessionManager.signIn(name, dob)
//                    route = AppRoute.Home
//                }
//            )
//
//        AppRoute.Home ->
//            HomeScreen(
//                user = sessionManager.getUser()!!,
//                onSignOut = {
//                    sessionManager.signOut()
//                    route = AppRoute.Onboarding
//                },
//                appViewModel = appViewModel,
//                backToHome = {
//                    route = AppRoute.Home
//                },
//                enterJournalEntry = { route = AppRoute.JournalEntry }
//            )
//
//        AppRoute.JournalEntry -> {
//            JournalEditorScreen(
//                onBack = { route = AppRoute.Home },
//                onSave = { title, content, tags, isFav ->},
//                appViewModel = appViewModel
//            )
//        }
//    }
//}



data class TabItem(val title: String, val icon: ImageVector)