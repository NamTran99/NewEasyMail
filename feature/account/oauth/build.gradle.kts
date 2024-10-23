plugins {
    id(ThunderbirdPlugins.Library.androidCompose)
}

android {
    namespace = "app.k9mail.feature.account.oauth"
    resourcePrefix = "account_oauth_"

    productFlavors {
        getByName("googleSignKey") {
            manifestPlaceholders["SHA_1_BASE_64"] = "bUjlmtXgQPWIGwT/ThVlVpUjzx4="
        }
        getByName("localKey") {
            manifestPlaceholders["SHA_1_BASE_64"] = "KmAHRBTcv3IcOhQzOWEB7uOl2w4="
        }
    }
}

dependencies {
    implementation(projects.core.ui.compose.designsystem)
    implementation(projects.core.common)

    implementation(projects.mail.common)

    implementation(projects.feature.account.common)

    implementation(libs.appauth)
    implementation(libs.androidx.compose.material3)
    implementation(libs.timber)

    testImplementation(projects.core.ui.compose.testing)
    implementation(libs.jwtdecode)
    api (libs.msal)
}
