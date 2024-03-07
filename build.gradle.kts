
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
    id("signing")
    id("maven-publish")
}

import groovy.lang.Closure
apply(from="versioning.gradle")
val buildVersionName: Closure<Any> by ext

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

//apply plugin: 'signing'
//apply plugin: 'maven-publish'
//apply plugin: 'org.jetbrains.dokka'

//apply from: 'versioning.gradle'

//task clean(type: Delete) {
//    delete rootProject.buildDir
//}

task("showVersion") {
    println("VersionName: "  + buildVersionName())
}
