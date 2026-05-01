/**
 * Reference copy of the Maven publish convention.
 *
 * **ToastX:** The active configuration is **inlined in the root `build.gradle.kts`** (after the
 * `publishSecretsFile` block). Do not add `apply(from = ...)` for this file when the root already
 * uses `plugins { id("com.vanniktech.maven.publish") ... apply false }`: Gradle would load the
 * plugin on two classpaths and `configure<MavenPublishBaseExtension>` fails at runtime.
 *
 * For a fresh project without that conflict, you can use:
 *
 *   plugins { id("com.vanniktech.maven.publish") version "0.35.0" apply false }
 *   apply(from = rootProject.file("publishToMaven/maven-publish-convention.gradle.kts"))
 *
 * and paste the `subprojects { ... }` block from root `build.gradle.kts` into this file (without
 * the `inceptionYear` name clash — use one variable name for the POM year).
 *
 * Set `MAVEN_PUBLISH_*` in `gradle.properties` — see `gradle.properties.example` and `README.md`.
 */

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import java.io.File
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.plugins.signing.SigningExtension

private fun resolveGpgExecutable(project: Project): String {
    val fromEnv = System.getenv("GPG_PATH")?.trim().orEmpty()
    if (fromEnv.isNotEmpty()) {
        val f = File(fromEnv)
        if (f.canExecute()) return f.absolutePath
        if (fromEnv == "gpg" || fromEnv == "gpg2") return fromEnv
    }
    val fromProp = project.findProperty("signing.gnupg.executable")?.toString()?.trim().orEmpty()
    if (fromProp.isNotEmpty()) return fromProp
    for (c in listOf("/opt/homebrew/bin/gpg", "/opt/homebrew/bin/gpg2", "/usr/local/bin/gpg", "/usr/local/bin/gpg2")) {
        if (File(c).canExecute()) return c
    }
    return "gpg"
}

val publishGroup = providers.gradleProperty("MAVEN_PUBLISH_GROUP").orElse("com.example.library").get()
val publishVersion = providers.gradleProperty("MAVEN_PUBLISH_VERSION").orElse("0.1.0").get()
val moduleNames =
    providers.gradleProperty("MAVEN_PUBLISH_MODULES").orElse("").get()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

val pomName = providers.gradleProperty("MAVEN_PUBLISH_POM_NAME").orElse("Library").get()
val pomDescription = providers.gradleProperty("MAVEN_PUBLISH_POM_DESCRIPTION").orElse("Android library").get()
val pomUrl = providers.gradleProperty("MAVEN_PUBLISH_POM_URL").orElse("https://github.com/example/example").get()
val licenseName = providers.gradleProperty("MAVEN_PUBLISH_LICENSE_NAME").orElse("The Apache License, Version 2.0").get()
val licenseUrl = providers.gradleProperty("MAVEN_PUBLISH_LICENSE_URL")
    .orElse("https://www.apache.org/licenses/LICENSE-2.0.txt").get()
val developerId = providers.gradleProperty("MAVEN_PUBLISH_DEVELOPER_ID").orElse("developer").get()
val developerName = providers.gradleProperty("MAVEN_PUBLISH_DEVELOPER_NAME").orElse("Developer").get()
val developerUrl = providers.gradleProperty("MAVEN_PUBLISH_DEVELOPER_URL").orElse(pomUrl).get()
val scmUrl = providers.gradleProperty("MAVEN_PUBLISH_SCM_URL").orElse(pomUrl).get()
val scmConnection = providers.gradleProperty("MAVEN_PUBLISH_SCM_CONNECTION")
    .orElse("scm:git:git://github.com/example/example.git").get()
val scmDevConnection = providers.gradleProperty("MAVEN_PUBLISH_SCM_DEV_CONNECTION")
    .orElse("scm:git:ssh://git@github.com/example/example.git").get()
val inceptionYear = providers.gradleProperty("MAVEN_PUBLISH_INCEPTION_YEAR").orElse("2025").get()

val folderRepoRelative = providers.gradleProperty("MAVEN_PUBLISH_FOLDER_REPO").orElse("build/maven-repo").get()
val useSigning =
    providers.gradleProperty("MAVEN_PUBLISH_SIGNING").orElse("true").get().toBoolean()
val mavenArtifactIdOverride =
    providers.gradleProperty("MAVEN_PUBLISH_ARTIFACT_ID").orElse("").get()

subprojects {
    if (moduleNames.isEmpty() || name !in moduleNames) {
        return@subprojects
    }

    group = publishGroup
    version = publishVersion

    plugins.apply("com.vanniktech.maven.publish")

    afterEvaluate {
        val mavenArtifactId = mavenArtifactIdOverride.ifBlank { project.name }
        extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
            coordinates(publishGroup, mavenArtifactId, publishVersion)
            // Central: pass -PSONATYPE_HOST=CENTRAL_PORTAL (and SONATYPE_AUTOMATIC_RELEASE); do not
            // also call publishToMavenCentral() here — vanniktech configures it and a second call fails.
            if (useSigning) {
                signAllPublications()
            }
            pom {
                name.set("$pomName: $mavenArtifactId")
                description.set("$pomDescription — $mavenArtifactId")
                inceptionYear.set(inceptionYear)
                url.set(pomUrl)
                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                        distribution.set(licenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(developerId)
                        name.set(developerName)
                        url.set(developerUrl)
                    }
                }
                scm {
                    url.set(scmUrl)
                    connection.set(scmConnection)
                    developerConnection.set(scmDevConnection)
                }
            }
        }

        if (useSigning) {
            val gpgExecutable = resolveGpgExecutable(project)
            extensions.extraProperties.set("signing.gnupg.executable", gpgExecutable)
            extensions.configure<SigningExtension>("signing") {
                useGpgCmd()
            }
        }

        extensions.configure<PublishingExtension>("publishing") {
            repositories {
                maven {
                    name = "MavenFolderRepo"
                    url = uri(rootProject.layout.projectDirectory.dir(folderRepoRelative))
                }
            }
        }
    }
}
