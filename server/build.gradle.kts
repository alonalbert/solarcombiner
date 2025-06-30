plugins {
  alias(libs.plugins.kotlin.jpa)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
}

dependencies {
  implementation(project(":shared"))
  implementation(libs.jakarta.persistence)
  implementation(libs.jakarta.validation)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.cli)
  implementation(libs.kotlinx.coroutines.core.jvm)
  implementation(libs.kotlinx.datetime)
  implementation(libs.ktor.client.apache)
  implementation(libs.ktor.client.auth)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.web)
  implementation(libs.sqlite)
  implementation(libs.sqlite.dialect)

  implementation(libs.kandy)
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

springBoot {
  mainClass.set("com.alonalbert.pad.server.ServerKt")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
  }
}
