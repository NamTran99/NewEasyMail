plugins {
    id(ThunderbirdPlugins.Library.androidCompose)
}

android {
    namespace = "app.k9mail.core.ui.compose.designsystem"
    resourcePrefix = "designsystem_"
}

dependencies {
    api(projects.core.ui.compose.theme2.common)
    api(projects.core.ui.compose.theme2.k9mail)
    api(projects.core.ui.compose.theme2.thunderbird)
    api(projects.core.android.common)
    api(libs.lottieJson.compose)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    testImplementation(projects.core.ui.compose.testing)
}
