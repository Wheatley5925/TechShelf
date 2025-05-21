pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // JitPack for missing dependencies
        maven { url = uri("https://groovy.jfrog.io/artifactory/plugins-release") } // Needed for PdfBox-Android
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://s3.amazonaws.com/mirego-maven/public") }
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // JitPack for missing dependencies
        maven { url = uri("https://groovy.jfrog.io/artifactory/plugins-release") } // Needed for PdfBox-Android
    }
}
include(":app")
