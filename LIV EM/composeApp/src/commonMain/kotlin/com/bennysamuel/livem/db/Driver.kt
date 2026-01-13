package com.bennysamuel.livem.db

import app.cash.sqldelight.db.SqlDriver
import com.bennysamuel.liveem.db.LiveEmDB
import org.koin.core.module.Module

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): LiveEmDB {
    val driver = driverFactory.createDriver()
    return LiveEmDB(driver)
}


expect val sqlDelightModule: Module