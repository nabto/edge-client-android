pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Edge Client Android"
include(":library")
include(":library-ktx")
include(":iam-util")
include(":iam-util-ktx")
include(":webrtc")
include(":webrtc-demo")
