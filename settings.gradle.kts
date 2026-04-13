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
  }
}

rootProject.name = "NextFTC Suite"
include(":units")
include(":linalg")
include(":control")
include(":hardware")
include(":robot")