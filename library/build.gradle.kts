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

    defaultConfig {
        minSdk = NabtoConfig.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cFlags("-DANDROID_PLATFORM=24")
                cppFlags("-DANDROID_PLATFORM=24")
                targets("nabto_client")
            }
        }
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

    externalNativeBuild {
        cmake {
            version = "3.18.1+"
            path = file("CMakeLists.txt")
        }
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
