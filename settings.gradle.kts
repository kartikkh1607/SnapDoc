pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SnapDoc"
include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:navigation")
include(":data:billing")
include(":data:prefs")
include(":data:specs")
include(":domain:camera")
include(":domain:export")
include(":domain:pipeline")
include(":domain:print")
include(":domain:watermark")
include(":feature:camera")
include(":feature:doc_detail")
include(":feature:export")
include(":feature:home")
include(":feature:onboarding")
include(":feature:preview")
include(":feature:processing")
include(":feature:review")
include(":feature:settings")
include(":feature:splash")
 