import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localConfig = Properties().apply {
    val configFile = rootProject.file("local.properties")
    if (configFile.exists()) configFile.inputStream().use { load(it) }
}
fun clientSetting(name: String, default: String): String =
    (providers.gradleProperty(name).orNull ?: localConfig.getProperty(name) ?: default)
        .let { "\"" + it.replace("\\", "\\\\").replace("\"", "\\\"") + "\"" }

android {
    namespace = "com.keeks26.drawingfinalboss"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.keeks26.drawingfinalboss"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Publishable client configuration, never a service-role or secret key.
        buildConfigField("String", "SUPABASE_URL", clientSetting(
            "SUPABASE_URL", "https://luptgjbydcbcvbgjxkit.supabase.co"
        ))
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", clientSetting(
            "SUPABASE_PUBLISHABLE_KEY", "sb_publishable_AYdASpUtEICw2yyG4zL5cg_O9tYVgBz"
        ))
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
