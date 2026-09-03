import java.net.URI
import java.security.MessageDigest

plugins {
    java
}

group = "com.mira"
version = "0.1.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

val miraFactionsVersion = "0.2.7"
val miraFactionsSha256 = "467ed2ae1826e7629ef74148008b741a9e2671d8822891196b62bcade43dc4f1"
val miraFactionsJar = layout.projectDirectory.file("libs/MiraFactions-$miraFactionsVersion.jar").asFile

fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(file.readBytes()).joinToString("") { byte -> "%02x".format(byte) }
}

fun downloadVerified(url: String, target: File, expectedSha256: String) {
    if (target.exists() && sha256(target) == expectedSha256) return
    target.parentFile.mkdirs()
    URI(url).toURL().openStream().use { input -> target.outputStream().use { output -> input.copyTo(output) } }
    check(sha256(target) == expectedSha256) { "Downloaded dependency failed SHA-256 verification: ${target.name}" }
}

val downloadMiraDependencies by tasks.registering {
    doLast {
        downloadVerified(
            "https://github.com/FiveSOCE/Mira-Factions/releases/download/v$miraFactionsVersion/MiraFactions-$miraFactionsVersion.jar",
            miraFactionsJar,
            miraFactionsSha256
        )
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(files(miraFactionsJar))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(downloadMiraDependencies)
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.jar {
    archiveFileName.set("MiraFly-${project.version}.jar")
}
