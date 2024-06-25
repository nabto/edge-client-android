plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.android.library") version "8.5.0" apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
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
