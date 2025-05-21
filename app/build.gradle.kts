plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // ✅ Add this
}

android {
    namespace = "com.lowpolystudio.techshelf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lowpolystudio.techshelf"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

configurations.all {
    exclude(group = "com.github.NanoHttpd.nanohttpd")
}

dependencies {
    implementation(libs.firebase.bom)
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.database)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    

    implementation("androidx.core:core:1.13.1") {
        exclude(group = "com.android.support", module = "support-compat")
    }

    implementation("com.jideguru:folioreader:0.6.3") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.github.NanoHttpd.nanohttpd")
        // Add exclusion for com.github.codetoart artifacts
        exclude(group = "com.github.codetoart")
    }

    implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.1")

    // Fuel networking library
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel-android:2.3.1")

    // UI dependencies
    implementation(libs.pdfbox.android)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.vectordrawable:vectordrawable-animated:1.1.0")
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase BoM - manages versions for Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Add Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Add Firestore (or Realtime Database if you prefer)
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.android.gms:play-services-auth:21.0.0")
}
