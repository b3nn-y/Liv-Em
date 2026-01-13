package com.bennysamuel.livem.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.bennysamuel.liveem.db.LiveEmDB
import org.koin.core.module.Module
import org.koin.dsl.module

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(LiveEmDB.Schema, context, "liveEmDb.db")
}

actual val sqlDelightModule: Module = module{
    single { DriverFactory(get()) }
}