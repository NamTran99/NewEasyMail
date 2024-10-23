plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("thunderbird.quality.detekt.typed")
}

android {
    configureSharedConfig()

    buildFeatures {
        buildConfig = false
    }

    dataBinding{
        enable = true
    }

    kotlinOptions {
        jvmTarget = ThunderbirdProjectConfig.javaCompatibilityVersion.toString()
    }

    flavorDimensions += "signedKey"
    productFlavors {
        create("googleSignKey") {
            dimension = "signedKey"
        }
        create("localKey") {
            dimension = "signedKey"
        }
    }

    lint {
        lintConfig = file("${rootProject.projectDir}/config/lint/lint.xml")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(platform(libs.koin.bom))

    implementation(libs.bundles.shared.jvm.main)
    implementation(libs.bundles.shared.jvm.android)

    testImplementation(libs.bundles.shared.jvm.test)
}
