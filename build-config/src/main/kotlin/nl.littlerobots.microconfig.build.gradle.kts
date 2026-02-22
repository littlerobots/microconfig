import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

version = "1.0.0"

extensions.configure<KotlinMultiplatformExtension> {
  jvm()
  iosArm64()
  iosSimulatorArm64()

  extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
    compileSdk = 36
    minSdk = 24
  }
}

plugins.withId("com.vanniktech.maven.publish") {
  extensions.configure<MavenPublishBaseExtension>() {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    pom {
      name = "Microconfig"
      description = "A tiny runtime configuration libary"
      inceptionYear = "2025"
      url = "https://github.com/littlerobots/microconfig"
      licenses {
        license {
          name = "The Apache License, Version 2.0"
          url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
          distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
      }
      developers {
        developer {
          id = "hvisser"
          name = "Hugo Visser"
          url = "https://github.com/hvisser/"
        }
      }
      scm {
        url = "https://github.com/littlerobots/microconfig"
        connection = "scm:git:git://github.com/littlerobots/microconfig.git"
        developerConnection = "scm:git:ssh://git@github.com/littlerobots/microconfig.git"
      }
    }
  }
}

plugins.withId("signing") {
  extensions.configure<SigningExtension>() {
    val signingId = providers.gradleProperty("signingId").orNull
    val signingPassword = providers.gradleProperty("signingPassword").orNull
    val signingKeyId = providers.gradleProperty("signingKeyId").orNull
    if (signingId != null && signingPassword != null && signingKeyId != null) {
      useInMemoryPgpKeys(signingKeyId, signingId, signingPassword)
    } else {
      useGpgCmd()
    }
  }
}
