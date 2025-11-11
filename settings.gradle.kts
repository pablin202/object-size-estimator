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


rootProject.name = "ObjectSizeEstimator"

// App module
include(":app")

// Feature modules
include(":feature:camera")
include(":feature:settings")

// Domain module (Clean Architecture - business logic, independent of everything)
include(":domain")

// Core modules (Infrastructure)
include(":core:common")
include(":core:data")
include(":core:datastore")
include(":core:ml")
include(":core:camera")
include(":core:performance")
