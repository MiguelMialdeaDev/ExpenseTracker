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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ExpenseTracker"

include(":app")

// Core modules
include(":core:ui")
include(":core:data")
include(":core:domain")

// Feature modules
include(":feature:home:domain")
include(":feature:home:ui")

include(":feature:add:domain")
include(":feature:add:ui")

include(":feature:dashboard:domain")
include(":feature:dashboard:ui")

include(":feature:filter:domain")
include(":feature:filter:ui")

include(":feature:budget:domain")
include(":feature:budget:ui")
