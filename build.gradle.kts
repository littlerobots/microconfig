import com.diffplug.gradle.spotless.SpotlessExtension

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
    target("**/*.kt")
    ktfmt("0.54")
    licenseHeaderFile(rootProject.file("spotless/license.txt"))
  }
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt("0.54")
  }
}

tasks.register<Copy>("updateGitHooks") {
  inputs.files("./.git/hooks", "./scripts/pre-commit")
  from("./scripts/pre-commit")
  into("./.git/hooks")
}
