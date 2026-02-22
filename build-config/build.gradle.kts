plugins {
  `kotlin-dsl`
}

dependencies {
  compileOnly(libs.kotlin.multiplatform.plugin)
  compileOnly(libs.androidLibrary.multiplatform.plugin)
  compileOnly(libs.mavenPublishPlugin)
}
