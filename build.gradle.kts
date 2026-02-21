import com.diffplug.gradle.spotless.SpotlessExtension

buildscript { dependencies { classpath(libs.kotlin.gradle.plugin) } }

plugins {
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.kotlinx.serialization) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.versionCatalogUpdate)
  alias(libs.plugins.spotless)
}

configure<SpotlessExtension> {
  kotlin {
    targetExclude("**/build/**/*")
    target("**/*.kt")
    ktfmt("0.61")
    licenseHeaderFile(rootProject.file("spotless/license.txt"))
  }
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt("0.61")
  }
}

tasks.register<Copy>("updateGitHooks") {
  inputs.files("./.git/hooks", "./scripts/pre-commit")
  from("./scripts/pre-commit")
  into("./.git/hooks")
}
