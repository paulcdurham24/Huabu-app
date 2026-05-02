plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics.plugin)
}

android {
    namespace = "com.huabu.app"
    compileSdk = 35

    signingConfigs {
        create("release") {
            val localPropsFile = rootProject.file("local.properties")
            if (localPropsFile.exists()) {
                val props = org.jetbrains.kotlin.konan.properties.loadProperties(localPropsFile.path)
                val ksPath = props["KEYSTORE_PATH"] as? String
                storeFile     = if (ksPath != null) file(ksPath) else null
                storePassword = props["KEYSTORE_PASSWORD"] as? String ?: ""
                keyAlias      = props["KEY_ALIAS"] as? String ?: ""
                keyPassword   = props["KEY_PASSWORD"] as? String ?: ""
            }
        }
    }

    defaultConfig {
        applicationId = "com.huabu.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 10       // increment by 1 for every Play Store upload
        versionName = "1.9.0" // shown to users on the Play Store listing

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProps = org.jetbrains.kotlin.konan.properties.loadProperties(rootProject.file("local.properties").path)
        buildConfigField("String", "YOUTUBE_API_KEY", "\"${localProps["YOUTUBE_API_KEY"] ?: ""}\"")
        buildConfigField("String", "GIPHY_API_KEY", "\"${localProps["GIPHY_API_KEY"] ?: ""}\"") 

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            // NDK r28+ — target all ABIs with 16 KB page-size alignment
            abiFilters += listOf("arm64-v8a", "x86_64", "armeabi-v7a", "x86")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // 16 KB page-size alignment — required for AGP 8.5 + NDK r28+
        jniLibs {
            useLegacyPackaging = false
        }
    }

    ndkVersion = "28.0.12674087"

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
