/**
 * Copy this folder next to your project root `settings.gradle.kts`, then in root `build.gradle.kts`:
 *
 *   plugins { id("com.vanniktech.maven.publish") version "0.36.0" apply false }
 *   apply(from = rootProject.file("publishToMaven/maven-publish-convention.gradle.kts"))
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
val useMavenCentral =
    providers.gradleProperty("MAVEN_PUBLISH_MAVEN_CENTRAL").orElse("false").get().toBoolean()
val centralAutoRelease =
    providers.gradleProperty("MAVEN_PUBLISH_CENTRAL_AUTO_RELEASE").orElse("true").get().toBoolean()
val useSigning =
    providers.gradleProperty("MAVEN_PUBLISH_SIGNING").orElse("true").get().toBoolean()

subprojects {
    if (moduleNames.isEmpty() || name !in moduleNames) {
        return@subprojects
    }

    group = publishGroup
    version = publishVersion

    plugins.apply("com.vanniktech.maven.publish")

    afterEvaluate {
        extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
            coordinates(publishGroup, project.name, publishVersion)
            if (useMavenCentral) {
                publishToMavenCentral(automaticRelease = centralAutoRelease)
            }
            if (useSigning) {
                signAllPublications()
            }
            pom {
                name.set("$pomName: ${project.name}")
                description.set("$pomDescription — ${project.name}")
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
