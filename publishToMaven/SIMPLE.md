# publishToMaven — simple guide

## What this folder is

Copy **`publishToMaven`** into your Android/Kotlin **project root** (next to `settings.gradle.kts`). It adds Maven publishing (local folder, `~/.m2`, or Maven Central).

## Setup (3 steps)

1. **Root `build.gradle.kts`** — add:

```kotlin
plugins { id("com.vanniktech.maven.publish") version "0.35.0" apply false }
apply(from = rootProject.file("publishToMaven/maven-publish-convention.gradle.kts"))
```

2. **Root `gradle.properties`** — copy lines from **`gradle.properties.example`** and fill **`MAVEN_PUBLISH_GROUP`**, **`MAVEN_PUBLISH_VERSION`**, **`MAVEN_PUBLISH_MODULES`** (your library module folder names, comma-separated).

3. **Publish to your machine** (no Central, no GPG needed if you use local-only settings):

```bash
./publishToMaven/publish.sh
```

Same as: `./gradlew publishToMavenLocal`

---

## Secrets: put only in `~/.gradle/gradle.properties`

**Never commit these.** They are **not** copied from `gradle.properties.example` into the project.

### Maven Central (Sonatype)

| Property | Where it comes from |
|----------|---------------------|
| **`mavenCentralUsername`** | Sonatype [Central Portal](https://central.sonatype.com) → account → **Generate User Token**. The **username** field the portal shows (often a long token id). |
| **`mavenCentralPassword`** | The **password** from that same generated token (not your login password). |

You need these when you run **`./publishToMaven/publish-central.sh`** (or `publishToMavenCentral` / `publishAndReleaseToMavenCentral` with **`-PSONATYPE_HOST=CENTRAL_PORTAL`**).

### GPG signing (`signing.keyId`, `signing.password`, `signing.secretKeyRingFile`)

Maven Central expects **signed** artifacts. These three describe **your GPG key**.

| Property | What it is | How to get it |
|----------|------------|----------------|
| **`signing.keyId`** | Public key id (hex). | Run **`gpg --list-secret-keys --keyid-format LONG`**. Use the **16-character** id on the `sec` line (e.g. `ABCD1234EF567890`), or often the **last 8 characters** work with Gradle — see [Gradle signing](https://docs.gradle.org/current/userguide/signing_plugin.html). |
| **`signing.password`** | Passphrase for that private key. | The passphrase you set when you created the key with **`gpg --full-generate-key`**. |
| **`signing.secretKeyRingFile`** | Path to a file that contains your **secret** key. | Many setups use an **exported** file: e.g. `gpg --export-secret-keys YOUR_KEY_ID > ~/secring.gpg` then set this property to **`/Users/you/secring.gpg`** (absolute path). On some systems an old **`~/.gnupg/secring.gpg`** exists; modern GnuPG may use a keybox instead — exporting as above avoids confusion. |

**Create a key (once):**

```bash
gpg --full-generate-key
```

Choose RSA, key size 4096, your name and email, and a strong passphrase.

**List keys:**

```bash
gpg --list-secret-keys --keyid-format LONG
```

**Publish your public key** to a keyserver (Maven Central requirement): see [Central’s GPG guide](https://central.sonatype.org/publish/requirements/gpg/).

### If signing fails with `Inappropriate ioctl for device`

GnuPG’s pinentry needs a real terminal (or loopback mode).

1. Run publish from **Terminal.app** or **iTerm**, not a minimal/embedded shell, and use **`./publishToMaven/publish-central.sh`** (it sets `GPG_TTY`), or manually: `export GPG_TTY=$(tty)` then `./gradlew …`.
2. On macOS, use a GUI pinentry: `brew install pinentry-mac`, then in **`~/.gnupg/gpg-agent.conf`** add  
   `pinentry-program /opt/homebrew/bin/pinentry-mac`  
   (Intel Homebrew: `/usr/local/bin/pinentry-mac`). Then `gpgconf --kill gpg-agent` and try again.
3. **Loopback** (passphrase from `signing.password` / secrets file): in **`~/.gnupg/gpg.conf`** add `pinentry-mode loopback`, in **`~/.gnupg/gpg-agent.conf`** add `allow-loopback-pinentry`, then `gpgconf --kill gpg-agent`. Ensure **`signing.password`** in `secrets.properties` matches your key passphrase.

---

## Local testing without GPG

Set in **project** `gradle.properties`:

- **`MAVEN_PUBLISH_SIGNING=false`**

Then try **`./publishToMaven/publish.sh`**. If Gradle still requires signing for your setup, keep signing enabled and configure GPG in **`~/.gradle/gradle.properties`**.

---

## Files in this folder

| File | Purpose |
|------|---------|
| `maven-publish-convention.gradle.kts` | Gradle script — applied from root `build.gradle.kts`. |
| `gradle.properties.example` | Copy the **`MAVEN_PUBLISH_*`** lines into your project `gradle.properties`. |
| `publish.sh` | Runs `./gradlew` from the project root. |
| **`SIMPLE.md`** | This file. |

For more detail, see the main repo **`PUBLISHING.md`** (Maven Central namespace, etc.).
