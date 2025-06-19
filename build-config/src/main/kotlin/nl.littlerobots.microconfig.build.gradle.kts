import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

extensions.configure<KotlinMultiplatformExtension> {
  jvm()
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  @Suppress("UnstableApiUsage")
  androidLibrary {
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
}
