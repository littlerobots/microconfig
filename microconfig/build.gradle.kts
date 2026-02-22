import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.mavenPublish)
  id("nl.littlerobots.microconfig.build")
}

mavenPublishing {
  coordinates("nl.littlerobots.microconfig", "microconfig", version as String)
}

kotlin {
  androidLibrary {
    namespace = "nl.littlerobots.microconfig"

    compilerOptions {
      jvmTarget = JvmTarget.JVM_1_8
    }
  }

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

