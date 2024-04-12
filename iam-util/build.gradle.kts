import com.nabto.edge.NabtoConfig

plugins {
    id("com.android.library")
    id("kotlin-android")
}

rootProject.extra.apply {
    set("POM_GROUP", NabtoConfig.artifactGroup)
    set("POM_ARTIFACT_ID", "iam-util")
}

apply(from = "$rootDir/scripts/publish.gradle")

android {
    namespace = "com.nabto.edge.iamutil"
    compileSdk = NabtoConfig.compileSdk
    buildToolsVersion = NabtoConfig.buildToolsVersion

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
}

dependencies {
    implementation(libs.jetbrains.annotations)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.jackson)
    implementation(project(":library"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
