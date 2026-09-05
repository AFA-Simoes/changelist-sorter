import org.jetbrains.changelog.Changelog
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

fun properties(key: String) = project.findProperty(key).toString()

version = properties("pluginVersion")
description = properties("pluginDescription")

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("org.jetbrains.changelog") version "2.5.0"
    id("io.github.ben-manes.versions") version "0.61.0"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

val lombokVersion = "1.18.48"

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(providers.gradleProperty("platformVersion"))
        pluginVerifier()
    }

    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // The IntelliJ Platform Gradle Plugin's sandboxed test runner bridges through JUnit 4
    // internals (org.junit.runners.model.Statement) even for a pure JUnit 5 suite.
    testRuntimeOnly("junit:junit:4.13.2")

    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        description = providers.gradleProperty("pluginDescription")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }

        changeNotes = provider {
            changelog.renderItem(
                changelog.getLatest(),
                Changelog.OutputType.HTML
            )
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    publishing {
        // token defaults to the PUBLISH_TOKEN environment variable
    }
}

changelog {
    version.set(properties("pluginVersion"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(properties("javaVersion").toInt())
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<DependencyUpdatesTask> {
        rejectVersionIf {
            (
                listOf("RELEASE", "FINAL", "GA").any { candidate.version.uppercase().contains(it) }
                ||
                "^[0-9,.v-]+(-r)?$".toRegex().matches(candidate.version)
            ).not()
        }
    }
}
