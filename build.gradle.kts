import com.nabto.edge.NabtoConfig

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.jetbrains.dokka) apply false
    id("base")
    id("maven-publish")
    id("org.jreleaser") version "1.19.0"
}

apply(from="$rootDir/scripts/versioning.gradle")
val buildVersionName: groovy.lang.Closure<String> by extra


rootProject.extra.apply {
    set("JRELEASER_GROUP_ID", NabtoConfig.artifactGroup)
}
apply(from="$rootDir/scripts/jreleaser.gradle")

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

task("showVersion") {
    println("VersionName: "  + buildVersionName())
}
