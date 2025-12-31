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

        // ARMv7 Only Configuration
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a"))
        }
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

    // Configure external native build for CMake - ARMv7 optimized
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // Disable unneeded features for speed
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
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
    // NO EXTERNAL DEPENDENCIES - Pure Android SDK only
}
