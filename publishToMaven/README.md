# publishToMaven

Gradle + scripts to publish Android/Kotlin libraries with [Vanniktech Maven Publish](https://github.com/vanniktech/gradle-maven-publish-plugin).

**Start here:** **[SIMPLE.md](SIMPLE.md)** — short setup, commands, and how to find or create **Maven Central** and **GPG signing** values (`mavenCentralUsername`, `signing.keyId`, `signing.password`, `signing.secretKeyRingFile`).

## Copy-paste wire-up

1. Place this **`publishToMaven`** folder in your **project root** (next to `settings.gradle.kts`).
2. In root **`build.gradle.kts`** — add `id("com.vanniktech.maven.publish") version "0.35.0" apply false` to `plugins { }`, then either paste the `subprojects { ... }` block from **`maven-publish-convention.gradle.kts`** into this file, or use `apply(from = ...)`. **ToastX** inlines that block in root `build.gradle.kts` (see comment there): `apply(from)` plus `plugins { }` can load Vanniktech twice and fail at configure time.

3. Merge **`gradle.properties.example`** into root **`gradle.properties`** and set **`MAVEN_PUBLISH_MODULES`**, group, version.
4. Run **`./publishToMaven/publish.sh`** or **`./gradlew publishToMavenLocal`**.

## Contents

| File | Role |
|------|------|
| **[SIMPLE.md](SIMPLE.md)** | Easiest documentation (secrets + signing explained). |
| **`gradle.properties.example`** | `MAVEN_PUBLISH_*` template; comments point to SIMPLE.md for secrets. |
| **`maven-publish-convention.gradle.kts`** | Applied by your root `build.gradle.kts`. |
| **`publish.sh`** | Runs Gradle from the parent directory (project root). |

Repo-wide publishing notes: **[../PUBLISHING.md](../PUBLISHING.md)**.

