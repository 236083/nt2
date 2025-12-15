plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.nt2"
    compileSdk = 36 // Changed to constant as per convention, assuming 'release(36)' resolves to 36

    defaultConfig {
        applicationId = "com.example.nt2"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // View Binding を有効化
    buildFeatures {
        viewBinding = true
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
    compileOptions {
        // Kotlin Optionsに合わせ、Java 11に統一 (互換性のため)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // --- 既存の依存関係 ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ViewPager2: TabLayoutと組み合わせてコンテンツ切り替えを実現するために必須
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // --- 追加された依存関係 (CoroutinesとLocation Services) ---

    // 1. Google Location Services (FusedLocationProviderClientのために必須)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 2. Kotlin Coroutines (非同期処理のために必須)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // 3. Lifecycle KTX (lifecycleScopeを使うために推奨)
    // Fragmentのライフサイクルに合わせてCoroutinesを管理します
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")


    // --- テスト依存関係 ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}