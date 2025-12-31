plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // ARMv7 Only Configuration
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a"))
        }
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    buildTypes {
        // Debug build - fastest, no obfuscation
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        
        // Release build - no minification for speed
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
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
    
    // Configure external native build for CMake - ARMv7 optimized
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    
    buildFeatures {
        buildConfig = true
        viewBinding = true
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

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // UI dependencies (keep minimal)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
