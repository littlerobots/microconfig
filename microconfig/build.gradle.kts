import com.android.build.api.dsl.androidLibrary

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinx.serialization)
  id("nl.littlerobots.microconfig.build")
}

kotlin {
  @Suppress("UnstableApiUsage")
  androidLibrary {
    namespace = "nl.littlerobots.microconfig.shared"
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

