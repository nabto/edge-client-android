import com.nabto.edge.NabtoConfig

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

rootProject.extra.apply {
    set("POM_GROUP", NabtoConfig.artifactGroup)
    set("POM_ARTIFACT_ID", "iam-util-ktx")
}

apply(from = "$rootDir/scripts/publish.gradle")

android {
    namespace = "com.nabto.edge.iamutil.ktx"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.jackson)
    implementation(project(":library"))
    implementation(project(":iam-util"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.core.ktx)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
}
