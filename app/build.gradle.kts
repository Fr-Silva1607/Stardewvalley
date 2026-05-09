plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.stardewvalley"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.stardewvalley"
        minSdk = 24
        targetSdk = 36 // Actualizado a 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        dataBinding = false
    }

    // ELIMINADO: composeOptions { ... } ya no va aquí en Kotlin 2.1.0

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// Mover esto aquí afuera es más seguro
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            if (requested.name.startsWith("kotlin-stdlib-jre")) {
                useTarget("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
            } else {
                useVersion("2.1.0")
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui.geometry)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui)
    implementation(libs.play.services.maps3d)
    implementation(libs.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")

    // Base de datos Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    kapt("androidx.room:room-compiler:$room_version")

    constraints {
        implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    }
}
configurations.all {
    exclude(group = "androidx.databinding", module = "baseLibrary")
    exclude(group = "androidx.databinding", module = "library")
}