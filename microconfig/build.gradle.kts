import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidLibrary {
        namespace = "nl.littlerobots.microconfig.shared"
        compileSdk = 36
        minSdk = 24

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    (this as KotlinJvmCompilerOptions).jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
                }
            }
        }
    }

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.serialization.json)
            api(libs.kotlinx.datetime)
            api(libs.semver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

