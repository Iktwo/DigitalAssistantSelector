plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.publish)
}

group = "com.iktwo"
version = "0.1.0"

android {
    namespace = "com.iktwo.das"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "das", version.toString())

    pom {
        name = "DAS"
        description =
            "Android library that exposes an app as a digital assistant with on-screen text selection."
        inceptionYear = "2026"
        url = "https://github.com/Iktwo/DigitalAssistantSelector"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "iktwo"
                name = "Isaac SH"
                url = "https://github.com/iktwo/"
            }
        }
        scm {
            url = "https://github.com/Iktwo/DigitalAssistantSelector"
            connection = "scm:git:git://github.com/Iktwo/DigitalAssistantSelector.git"
            developerConnection = "scm:git:ssh://git@github.com/Iktwo/DigitalAssistantSelector.git"
        }
    }
}
