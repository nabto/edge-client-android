import com.nabto.edge.NabtoConfig

plugins {
    id("com.android.library")
    id("kotlin-android")
}

rootProject.extra.apply {
    set("POM_GROUP", NabtoConfig.artifactGroup)
    set("POM_ARTIFACT_ID", "library")
}

apply(from = "$rootDir/scripts/publish.gradle")

android {
    namespace = "com.nabto.edge.client"
    compileSdk = NabtoConfig.compileSdk
    buildToolsVersion = NabtoConfig.buildToolsVersion
    ndkVersion = "26.3.11579264"

    defaultConfig {
        minSdk = NabtoConfig.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.jetbrains.annotations)
    implementation(libs.bundles.jackson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
