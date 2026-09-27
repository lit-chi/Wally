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
        maven {
            url = uri(rootDir.resolve("third_party/xlsxwriter-android-0.2.0"))
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Wally"
include(":app")
