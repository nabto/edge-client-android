import com.nabto.edge.NabtoConfig

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.nabto.edge.client.test"
    compileSdk = NabtoConfig.compileSdk

    defaultConfig {
        minSdk = NabtoConfig.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        targetSdk = NabtoConfig.testTargetSdk
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.junit)
    androidTestImplementation(project(":library"))
    androidTestImplementation(project(":testdata"))
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
}
