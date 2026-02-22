import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.mavenPublish)
  id("nl.littlerobots.microconfig.build")
}

mavenPublishing {
  coordinates("nl.littlerobots.microconfig", "client", version as String)
  pom {
    name = "Microconfig client"
  }
}

kotlin {
  androidLibrary {
    namespace = "nl.littlerobots.microconfig.client"

    compilerOptions {
      jvmTarget = JvmTarget.JVM_1_8
    }
  }

  kotlin {
    compilerOptions {
      freeCompilerArgs.add("-Xexpect-actual-classes")
    }
  }

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
