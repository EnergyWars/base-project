import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries)
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace  = "com.wafflehq.base"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wafflehq.base"
        minSdk        = 26
        targetSdk     = 36
        versionCode   = 1
        versionName   = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile     = localProperties["RELEASE_STORE_FILE"]?.let { file(it as String) }
            storePassword = localProperties["RELEASE_STORE_PASSWORD"] as? String
            keyAlias      = localProperties["RELEASE_KEY_ALIAS"] as? String
            keyPassword   = localProperties["RELEASE_KEY_PASSWORD"] as? String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release").takeIf {
                localProperties["RELEASE_STORE_FILE"] != null
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental",    "true")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

val generatedFeatureAssetsDir = layout.buildDirectory.dir("generated/featureAssets")

val syncFeatureFiles by tasks.registering(Sync::class) {
    from(rootProject.file("features")) {
        include("*.md")
    }
    into(generatedFeatureAssetsDir.map { it.dir("features") })
}

android.sourceSets["main"].assets.srcDir(generatedFeatureAssetsDir)

tasks.matching {
    (it.name.startsWith("merge") && it.name.endsWith("Assets")) ||
    it.name.contains("Lint", ignoreCase = true)
}.configureEach { dependsOn(syncFeatureFiles) }

tasks.register("testClasses")

afterEvaluate {
    tasks.withType<com.mikepenz.aboutlibraries.plugin.BaseAboutLibrariesTask>().configureEach {
        pomFiles.setFrom()
    }
}

dependencies {
    implementation(project(":lib:astronomy"))
    implementation(project(":lib:backupcore"))
    implementation(project(":lib:charts"))
    implementation(project(":lib:database"))
    implementation(project(":lib:diagnostics"))
    implementation(project(":lib:drafts"))
    implementation(project(":lib:entrylock"))
    implementation(project(":lib:folders"))
    implementation(project(":lib:maintenance"))
    implementation(project(":lib:media"))
    implementation(project(":lib:modules"))
    implementation(project(":lib:navigation"))
    implementation(project(":lib:notifications"))
    implementation(project(":lib:pdf"))
    implementation(project(":lib:prefsbackup"))
    implementation(project(":lib:qr"))
    implementation(project(":lib:quickpicker"))
    implementation(project(":lib:settings"))
    implementation(project(":lib:textarea"))
    implementation(project(":lib:ui-core"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.ext.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.biometric)

    constraints {
        implementation("com.google.guava:guava:33.7.1-android") {
            because("google-api-client transitively pulls the JRE flavor of Guava, the Android flavor is the one meant for this app")
        }
        implementation("org.jetbrains.compose.material3:material3:1.9.0") {
            because("aboutlibraries-compose-m3 transitively pulls a beta artifact, pre-release artifacts are not allowed")
        }
        implementation("org.jetbrains.compose.material:material-ripple:1.9.1") {
            because("aboutlibraries-compose-m3 transitively pulls a beta artifact, pre-release artifacts are not allowed")
        }
        implementation("org.jetbrains.compose.ui:ui-backhandler:1.9.1") {
            because("aboutlibraries-compose-m3 transitively pulls a beta artifact, pre-release artifacts are not allowed")
        }
    }

    coreLibraryDesugaring(libs.android.desugar.jdk.libs)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.ui.test.manifest)
}
