package com.bennysamuel.livem.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.bennysamuel.liveem.db.LiveEmDB
import org.koin.core.module.Module
import org.koin.dsl.module

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = LiveEmDB.Schema,
            name = "liveEmDb.db"
        )
    }
}

actual val sqlDelightModule: Module = module {
    single { DriverFactory() }
}