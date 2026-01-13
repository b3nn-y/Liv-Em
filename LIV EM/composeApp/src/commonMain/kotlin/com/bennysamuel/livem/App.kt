package com.bennysamuel.livem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.bennysamuel.livem.db.sqlDelightModule
import com.bennysamuel.livem.nav.LiveEmApp
import com.bennysamuel.livem.theme.LivEmTheme
import com.bennysamuel.livem.user.SessionManager
import com.russhwolf.settings.Settings
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

@Composable
@Preview
fun App() {


    val settings: Settings = Settings()

    val sessionManager = remember {
        SessionManager(settings)
    }

    LivEmTheme {
        LiveEmApp(
            sessionManager = sessionManager
        )
    }

//    MaterialTheme {
//        var showContent by remember { mutableStateOf(false) }
//        Column(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.primaryContainer)
//                .safeContentPadding()
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            Button(onClick = { showContent = !showContent }) {
//                Text("Click me!")
//            }
//            AnimatedVisibility(showContent) {
//                val greeting = remember { Greeting().greet() }
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                    Image(painterResource(Res.drawable.compose_multiplatform), null)
//                    Text("Compose: $greeting")
//                }
//            }
//        }
//    }
}

fun initKoinModules(
    koinModules: List<Module> = listOf<Module>(),
    config: KoinAppDeclaration? = null
): Boolean {
    val isKoinStarted = KoinPlatformTools.defaultContext().getOrNull()
    if (isKoinStarted == null) {
        try {
            startKoin {
                config?.invoke(this)
                modules(
                    sqlDelightModule,
                )
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
    return true
}