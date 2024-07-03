import com.nabto.edge.NabtoConfig

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
   testOptions {
       targetSdk = NabtoConfig.testTargetSdk
       managedDevices {
           localDevices {
                create("pixel2api27") {
                    device = "Pixel 2"
                    apiLevel = 27
                }
                create("pixel3api28") {
                    device = "Pixel 3"
                    apiLevel = 28
                }
                create("pixel4api29") {
                    device = "Pixel 4"
                    apiLevel = 29
                    systemImageSource = "aosp"
                }
                create("pixel5api30") {
                    device = "Pixel 5"
                    apiLevel = 30
                    systemImageSource = "aosp"
                }
                create("pixel6api31") {
                    device = "Pixel 6"
                    apiLevel = 31
                    systemImageSource = "aosp"
                }
                create("pixel7api32") {
                    device = "Pixel 7"
                    apiLevel = 32
                    systemImageSource = "aosp"
                }
                create("pixel8api33") {
                    device = "Pixel 8"
                    apiLevel = 33
                    systemImageSource = "aosp"
                }
                create("pixel8proapi34") {
                    device = "Pixel 8 Pro"
                    apiLevel = 34
                    systemImageSource = "aosp"
                }
            }
            groups {
                create("phones") {
                    targetDevices.add(devices["pixel3api28"])
                    targetDevices.add(devices["pixel4api29"])
                    targetDevices.add(devices["pixel5api30"])
                    targetDevices.add(devices["pixel6api31"])
                    targetDevices.add(devices["pixel7api32"])
                    targetDevices.add(devices["pixel8api33"])
                    targetDevices.add(devices["pixel8proapi34"])
                }
                create("todo") {
                    targetDevices.add(devices["pixel2api27"])
                }
            }
        }
    }
}

dependencies {
    //implementation(libs.androidx.appcompat)
    implementation(libs.jetbrains.annotations)
    implementation(libs.bundles.jackson)
    testImplementation(libs.junit)
    androidTestImplementation(project(":testdata"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
