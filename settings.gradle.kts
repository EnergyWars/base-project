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
rootProject.name = "BaseApp"
include(":app")
include(":lib:astronomy")
include(":lib:backupcore")
include(":lib:charts")
include(":lib:database")
include(":lib:diagnostics")
include(":lib:drafts")
include(":lib:entrylock")
include(":lib:folders")
include(":lib:maintenance")
include(":lib:media")
include(":lib:modules")
include(":lib:navigation")
include(":lib:notifications")
include(":lib:pdf")
include(":lib:prefsbackup")
include(":lib:qr")
include(":lib:quickpicker")
include(":lib:settings")
include(":lib:textarea")
include(":lib:ui-core")
