plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(libs.gson)
  implementation(libs.jakarta.persistence)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.client.auth)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.okhttp)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.slf4j.nop)
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
  }
}
