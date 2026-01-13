package com.bennysamuel.livem.nav

import kotlinx.serialization.Serializable
sealed class AppRoute {

    @Serializable
    data object Onboarding : AppRoute()

    @Serializable
    data object ProfileSetup : AppRoute()

    @Serializable
    data object Home : AppRoute()

    @Serializable
    data object JournalEntry: AppRoute()


}
