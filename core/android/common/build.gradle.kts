plugins {
    id(ThunderbirdPlugins.Library.android)
}

android {
    namespace = "app.k9mail.core.android.common"
}

dependencies {
    api(projects.core.common)
    api(platform(libs.firebase.bom))
    api(libs.firebase.analytics)
    api(libs.firebase.auth)
    api(libs.firebase.firestore.ktx)
    api(libs.firebase.crashlytics)
    api(libs.firebase.crashlytics.ktx)
    api(projects.feature.account.oldSetup)
    api(libs.play.services.ads)
    api(libs.billing)
    api(libs.androidx.datastore.core.android)

    testImplementation(projects.core.testing)
    testImplementation(libs.robolectric)
}
