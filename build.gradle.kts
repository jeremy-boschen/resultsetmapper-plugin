import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin

    // Used for testing
    alias(libs.plugins.springDependencyManagement)
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(17)

    sourceSets {
        test {
            // Tests use JDK17 to support the latest Spring Boot version
            jvmToolchain(17)
        }
    }
}


// Used for testing
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.5")
    }
}

configurations {
    all {
        exclude(module = "spring-boot-starter-logging")
    }
}


// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    implementation(libs.annotations)

    testImplementation(kotlin("test"))
    // Used for testing the embedded templates
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:postgresql")
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}


tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with (it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    runIde {
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }


    // Copies working class templates from the test module to main module's fileTemplates/j2ee directory
    // for use as IntelliJ code templates
    register("copyTemplates", Copy::class.java) {
        group = "build"

        // Always copy
        outputs.upToDateWhen { false }

        val replacements = mapOf(
            "jdbc_template" to "\${packageName}",
            "generator_name" to "\${generatorName}",
            "javax." to "\${j2eNamespace}.",
            "jakarta." to "\${j2eNamespace}.",
        )

        from("src/test/java/jdbc_template")
        into("src/main/resources/fileTemplates/j2ee")
        include("ResultSetWrapper.java", "ResultSetMapper.java", "FromRowMapper.java")
        filter { line ->
            // Process replacements for the line
            replacements.entries.fold(line) { acc, (from, to) -> acc.replace(from, to) }
        }
        rename {
            "$it.ft"
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }


    test {
        useJUnitPlatform()

        // Required for IntelliJ test framework
        systemProperty("idea.home.path", layout.projectDirectory.asFile.absolutePath)
    }
}

