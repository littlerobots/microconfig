import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

extensions.configure<KotlinMultiplatformExtension> {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
    compileSdk = 36
    minSdk = 24
  }
}

