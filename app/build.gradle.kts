plugins {
    id("com.android.application")
}

android {
    namespace = "top.moye.miraibotwatch"
    compileSdk = 33
    
    defaultConfig {
        applicationId = "top.moye.miraibotwatch"
        minSdk = 19
        targetSdk = 33
        versionCode = 2
        versionName = "1.1"
        resConfigs("zh")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.11.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.11.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.1")
    implementation("com.squareup.okhttp3:okhttp:3.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}