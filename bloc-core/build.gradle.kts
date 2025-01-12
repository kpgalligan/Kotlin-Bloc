import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlin-parcelize")
    id("org.jetbrains.dokka")

    // todo create a separate module for Jetbrains Compose
    id("org.jetbrains.compose")
}

version = "1.0"

kotlin {
    android()

    val isMacOsX = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
    if (isMacOsX) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    cocoapods {
        summary = "Reactive state management library for KMM"
        homepage = "https://github.com/1gravity/Kotlin-Bloc"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "bloc-core"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(KotlinX.coroutines.core)

                // Essenty (https://github.com/arkivanov/Essenty)
                implementation("com.arkivanov.essenty:lifecycle:_")
                implementation("com.arkivanov.essenty:parcelable:_")
                implementation("com.arkivanov.essenty:state-keeper:_")
                implementation("com.arkivanov.essenty:instance-keeper:_")
                implementation("com.arkivanov.essenty:back-pressed:_")

                // Logging (https://github.com/touchlab/Kermit)
                implementation(Touchlab.kermit)

                // Kotlin Result (https://github.com/michaelbull/kotlin-result)
                implementation("com.michael-bull.kotlin-result:kotlin-result:_")
                implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:_")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(AndroidX.appCompat)
                implementation(AndroidX.activity.compose)
            }
        }
        val androidTest by getting

        if (isMacOsX) {
            val iosMain by getting
            val iosTest by getting
            val iosSimulatorArm64Main by getting {
                dependsOn(iosMain)
            }
            val iosSimulatorArm64Test by getting {
                dependsOn(iosTest)
            }
        }
    }
}

android {
    compileSdk = 32
    buildToolsVersion = "32.0.0"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 21
        targetSdk = 32
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    dataBinding {
        isEnabled = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
        useLiveLiterals = true
    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("../../docs/dokka"))
}
