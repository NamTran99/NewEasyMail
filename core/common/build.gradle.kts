plugins {
    id(ThunderbirdPlugins.Library.jvm)
    alias(libs.plugins.android.lint)
}

dependencies {
    api (libs.gson)
    testImplementation(projects.core.testing)
}
