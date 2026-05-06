import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlinSerialization)
}

// Load local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

// Firebase configuration
val firebaseApiKey = localProperties.getProperty("firebase.apiKey") ?: ""
val firebaseAuthDomain = localProperties.getProperty("firebase.authDomain") ?: ""
val firebaseProjectId = localProperties.getProperty("firebase.projectId") ?: ""
val firebaseStorageBucket = localProperties.getProperty("firebase.storageBucket") ?: ""
val firebaseGcmSenderId = localProperties.getProperty("firebase.gcmSenderId") ?: ""
val firebaseApplicationId = localProperties.getProperty("firebase.applicationId") ?: ""

// Android signing configuration
val signingStoreFile = localProperties.getProperty("signing.storeFile") ?: ""
val signingStorePassword = localProperties.getProperty("signing.storePassword") ?: ""
val signingKeyAlias = localProperties.getProperty("signing.keyAlias") ?: ""
val signingKeyPassword = localProperties.getProperty("signing.keyPassword") ?: ""

// Version from environment (for CI) or default
val versionNameFromEnv = System.getenv("VERSION_NAME") ?: "0.1.0"
val versionCodeFromEnv = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 100

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add("-Xir-property-lazy-initialization")
            }
        }
    }
    
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
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
    
    sourceSets {
        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }
        
        androidMain.dependencies {
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.kmp.zip)
        }
        
        iosMain.dependencies {
            implementation(libs.kmp.zip)
        }
        
        jsMain.dependencies {
            implementation(npm("exceljs", "4.3.0"))
            implementation(npm("@js-joda/timezone", "2.3.0"))
        }
        
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material)
            implementation(libs.material.icons.extended)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)
            implementation(libs.material3)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.navigation.compose)
            implementation(libs.kmpauth.firebase)
            implementation(libs.kmpauth.google)
            implementation(libs.kmpauth.uihelper)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            api(libs.logging)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.html)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

// macOS Finder drops .DS_Store into the klib cache at any time, corrupting IC.
// Delete at configuration time so they're gone before the compiler reads the cache.
fileTree(layout.buildDirectory).matching { include("**/.DS_Store") }.forEach { it.delete() }

android {
    namespace = "org.gmautostop.hitchlogmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    defaultConfig {
        applicationId = "org.gmautostop.hitchlogmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = versionCodeFromEnv
        versionName = versionNameFromEnv
        
        // Pass Firebase config to BuildConfig
        buildConfigField("String", "FIREBASE_API_KEY", "\"$firebaseApiKey\"")
        buildConfigField("String", "FIREBASE_AUTH_DOMAIN", "\"$firebaseAuthDomain\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"$firebaseProjectId\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"$firebaseStorageBucket\"")
        buildConfigField("String", "FIREBASE_GCM_SENDER_ID", "\"$firebaseGcmSenderId\"")
        buildConfigField("String", "FIREBASE_APPLICATION_ID", "\"$firebaseApplicationId\"")
    }
    
    // Signing configuration
    signingConfigs {
        create("release") {
            storeFile = if (signingStoreFile.isNotEmpty()) {
                rootProject.file(signingStoreFile)
            } else {
                // CI: decode from environment
                val keystoreBase64 = System.getenv("ANDROID_KEYSTORE_BASE64")
                if (keystoreBase64 != null) {
                    val keystoreFile = file("${layout.buildDirectory.get()}/keystore.jks")
                    keystoreFile.parentFile.mkdirs()
                    keystoreFile.writeBytes(Base64.getDecoder().decode(keystoreBase64))
                    keystoreFile
                } else {
                    null
                }
            }
            storePassword = signingStorePassword.ifEmpty { 
                System.getenv("ANDROID_KEYSTORE_PASSWORD") 
            }
            keyAlias = signingKeyAlias.ifEmpty { 
                System.getenv("ANDROID_KEY_ALIAS") 
            }
            keyPassword = signingKeyPassword.ifEmpty { 
                System.getenv("ANDROID_KEY_PASSWORD") 
            }
            enableV1Signing = true
            enableV2Signing = true
        }
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "GMA Hitchlog (debug)")
        }
        getByName("release") {
            resValue("string", "app_name", "GMA Hitchlog")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    // Split APKs by architecture
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    dependencies {
        implementation(libs.koin.android)
        debugImplementation(libs.ui.tooling)
    }
}

