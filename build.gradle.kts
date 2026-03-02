import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    intellijPlatform {
        create("IC", "2025.1.7")
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
    }
}

val pluginVersion = providers.gradleProperty("pluginVersion").get()
version = pluginVersion
val javaVersion = providers.gradleProperty("javaVersion").get()

kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        jvmTarget = JvmTarget.fromTarget(javaVersion)
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
            untilBuild = "261.*"
        }
        id = "com.github.ginex25.RiverpodX"
        name = "RiverpodX"
        version = pluginVersion
        changeNotes = file("release/RELEASE_CHANGE_NOTES.html").readText()
        description = file("release/DESCRIPTION.html").readText()
        vendor {
            name = "ginex25"
            url = "https://github.com/ginex25/RiverpodX"
        }
    }
    signing {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    publishing {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    pluginVerification {
        verificationReportsFormats = VerifyPluginTask.VerificationReportsFormats.ALL
        subsystemsToCheck = VerifyPluginTask.Subsystems.ALL
        ides {
            recommended()
            create("IU", "261.21849.20")
        }
    }
}

tasks.named("buildPlugin", Zip::class.java) {
    archiveBaseName.set("RiverpodX")
    archiveVersion.set("${project.version}")
}