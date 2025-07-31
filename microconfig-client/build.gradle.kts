import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinx.serialization)
}

kotlin {
  androidLibrary {
    namespace = "nl.littlerobots.microconfig.client"
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

  kotlin {
    compilerOptions {
      freeCompilerArgs.add("-Xexpect-actual-classes")
    }
  }

  iosX64()
  iosArm64()
  iosSimulatorArm64()
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        api(project(":microconfig"))
        api(libs.ktor.client.core)
      }
    }

    commonTest {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.ktor.client.mock)
        implementation(libs.ktor.client.cio)
      }
    }
  }
}
