package com.bennysamuel.livem

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    initKoinModules ()
    App()

}