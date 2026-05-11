pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    mavenCentral()
    google()
    maven("https://repo.dairy.foundation/releases")
    maven("https://repo.dairy.foundation/snapshots")
  }
}

rootProject.name = "NextFTC Suite"
include(":units")
include(":linalg")
include(":control")
include(":hardware")
include(":robot")