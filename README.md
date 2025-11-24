# AndroidImageCropper

[![](https://jitpack.io/v/emirhantunc/androidimagecropper.svg)](https://jitpack.io/#emirhantunc/androidimagecropper)

A simple and efficient image cropping library for Android applications.

check app module for how to use it

## 🛠 Installation

Follow the steps below to integrate the library into your project.

add your build.gradle implementation("com.github.emirhantunc:androidimagecropper:1.0.0") library.


Open your root **`settings.gradle.kts`** (or `settings.gradle`) file and add the JitPack repository to the `repositories` block:


**Kotlin DSL (`settings.gradle.kts`):**
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("[https://jitpack.io](https://jitpack.io)") } // <-- Add this line
    }
}
