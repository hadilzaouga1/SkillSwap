
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "tn.skillswap.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "tn.skillswap.app"
        minSdk = 26
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionCode = 1
        versionName = "1.0"

        // URL du backend Spring Boot
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"http://localhost:8085/api/\""
        )

        // URL Keycloak
        buildConfigField(
            "String",
            "KEYCLOAK_URL",
            "\"http://127.0.0.1:8180/\""
        )

        buildConfigField(
            "String",
            "KEYCLOAK_REALM",
            "\"skillswap\""
        )

        buildConfigField(
            "String",
            "KEYCLOAK_CLIENT_ID",
            "\"api-gateway\""
        )

        buildConfigField(
            "String",
            "REGISTRATION_CLIENT_ID",
            "\"skillswap-web\""
        )

        buildConfigField(
            "String",
            "REGISTRATION_REDIRECT",
            "\"http://localhost:4200/\""
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))

    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore)

    // Retrofit et JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // Room : cache local
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coil : images distantes
    implementation(libs.coil.compose)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")

    debugImplementation(libs.compose.ui.test.manifest)
}
