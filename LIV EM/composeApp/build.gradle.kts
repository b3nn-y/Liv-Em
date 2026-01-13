import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("app.cash.sqldelight") version "2.2.1"
    kotlin("plugin.serialization") version "2.3.0"

}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
//    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("app.cash.sqldelight:android-driver:2.2.1")
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")



        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("io.github.alexzhirkevich:compottie:2.0.0")
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("media.kamel:kamel-image:0.9.3")
            implementation("io.github.onseok:peekaboo-ui:0.5.2")
            implementation("io.github.onseok:peekaboo-image-picker:0.5.2")
//            implementation("io.ktor:ktor-client-core:2.3.12")
//            implementation("io.ktor:ktor-client-cio:2.3.12")
            implementation(libs.ktor.client.core)
            implementation(libs.coil.compose.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.mp)
            implementation(libs.coil.network.ktor)
            implementation(libs.koin.core)
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("ai.koog:koog-agents:0.6.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")

            implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc13")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }

        iosMain.dependencies {
            implementation("app.cash.sqldelight:native-driver:2.2.1")
        }
    }

    sqldelight {
        databases {
            create("LiveEmDB") {
                packageName.set("com.bennysamuel.liveem.db")
                srcDirs.setFrom("src/commonMain/sqldelight/liveEmDatabase")

            }
        }
    }
}

android {
    namespace = "com.bennysamuel.livem"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bennysamuel.livem"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"

            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.bennysamuel.livem.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.bennysamuel.livem"
            packageVersion = "1.0.0"
        }
    }
}
