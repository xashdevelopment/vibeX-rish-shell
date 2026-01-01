plugins {
    id("com.android.application")
}

android {
    namespace = "com.vibe.termplugin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vibe.termplugin"
        minSdk = 21
        targetSdk = 34
        versionName = "1.0"
        versionCode = 1
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Disable unneeded features for speed
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
        }
    }

    // Configure source sets for Java code
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
        }
    }

    // Lint configuration - skip for faster builds
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        checkDependencies = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // AndroidX and Material Design - Required for Material attributes like colorControlNormal
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core:1.12.0")
    
    // Preserve existing Native references if any
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}
