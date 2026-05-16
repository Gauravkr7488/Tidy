// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    id("app.cash.sqldelight") version "2.3.2" apply false
}

// build.gradle.kts

buildscript {
    // Define a variable for the plugin version
    val objectboxVersion by extra("5.2.0")

    repositories {
        // Add Maven Central to the plugin repositories
        mavenCentral()
    }

    dependencies {
        // Add the plugin
        classpath("io.objectbox:objectbox-gradle-plugin:$objectboxVersion")
    }

}