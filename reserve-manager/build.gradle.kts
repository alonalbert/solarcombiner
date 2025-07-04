import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

plugins {
  alias(libs.plugins.kotlin.jvm)
  application
}
kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
  }
}

dependencies {
  implementation(project(":shared"))
  implementation(libs.kotlinx.cli)
  implementation(libs.kotlinx.coroutines.core.jvm)
}

application {
  mainClass.set("com.alonalbert.solar.reservemanager.ReserveManager")
}

tasks.register("selfRunningJar") {
  dependsOn("jar")
  group = "custom"

  val runner = project.file("jar-runner-stub.sh")
  val jar = layout.buildDirectory.file("libs/reserve-manager.jar")

  val outputJarFile = layout.buildDirectory.file("output/reserve-manager")

  inputs.file(runner)
    .withPathSensitivity(PathSensitivity.RELATIVE)
    .withPropertyName("runner")
  inputs.file(jar)
    .withPathSensitivity(PathSensitivity.RELATIVE)
    .withPropertyName("originalJar")
  outputs.file(outputJarFile)
    .withPropertyName("selfRunningJar")

  doLast {
    val originalJar = jar.get().asFile
    val outputFile = outputJarFile.get().asFile

    if (!runner.exists()) {
      throw GradleException("Source file to append does not exist: ${runner.absolutePath}")
    }
    if (!originalJar.exists()) {
      throw GradleException("Original JAR does not exist: ${originalJar.absolutePath}")
    }

    logger.lifecycle("Concatenating ${originalJar.name} to ${runner.name} into ${outputFile.name}")

    outputFile.outputStream().use { fos ->
      runner.inputStream().use { sourceIs ->
        sourceIs.copyTo(fos)
      }
      originalJar.inputStream().use { jarIs ->
        jarIs.copyTo(fos)
      }
    }
    val permissions = PosixFilePermissions.fromString("rwxr-xr-x")
    Files.setPosixFilePermissions(outputFile.toPath(), permissions)
    logger.lifecycle("Successfully created ${outputFile.name}")
  }
}

 tasks.named("build") {
     dependsOn("selfRunningJar")
 }
