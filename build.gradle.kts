plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.jetbrains.dokka) apply false
    id("signing")
    id("maven-publish")
}

apply(from="$rootDir/scripts/versioning.gradle")
val buildVersionName: groovy.lang.Closure<String> by extra

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

task("showVersion") {
    println("VersionName: "  + buildVersionName())
}
