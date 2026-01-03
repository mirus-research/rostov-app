plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "ru.mirus.rostovondon"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.mirus.rostovondon"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        addManifestPlaceholders(
            mapOf(
                "VKIDRedirectHost" to "vk.com",
                "VKIDRedirectScheme" to "vk53965275",
                "VKIDClientID" to "53965275",
                "VKIDClientSecret" to "tj8eH0bI6MwU92EC0qER"
            )
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true       // включает минификацию кода
            isShrinkResources = true       // удаляет неиспользуемые ресурсы
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"    // твои правила ProGuard
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    //Объекты карты
    implementation("com.squareup.okhttp3:okhttp:3.12.13")
    implementation("com.google.code.gson:gson:2.10.1")

    //Яндекс карты
    implementation("com.yandex.android:maps.mobile:4.19.0-lite")
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))

    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.picasso:picasso:2.71828")

    // VK ID
    implementation("com.vk.id:onetap-xml:2.5.0")
    implementation("com.vk.id:vkid:2.5.0")
    implementation(libs.firebase.firestore)
    implementation(libs.material)
    implementation("androidx.compose.material3:material3:1.2.1")

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // AndroidX и UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.gif.drawable)

    // Тесты
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}