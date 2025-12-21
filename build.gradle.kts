plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
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
        create("IC", "2024.1.7")
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.github.ginex25.RiverpodX"
        name = "RiverpodX"
        version = "1.4.0"
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "253.*"
        }
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
        ides {
            recommended()
            create("IU", "2025.3")
        }
    }
}


tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
