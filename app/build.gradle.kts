plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.personalnotesreminder"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.personalnotesreminder"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.7.2")
    annotationProcessor("androidx.room:room-compiler:2.7.2")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.4")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

}