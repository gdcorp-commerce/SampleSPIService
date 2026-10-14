## gd-artifactory — GoDaddy Artifactory package manager configuration

Configures Gradle to route dependency lookups through GoDaddy JFrog Artifactory at `gdartifactory1.jfrog.io`.

**Credentials** — sourced from the environment at tool runtime (never baked into the image):

| Env var | Purpose |
|---------|---------|
| `ARTIFACTORY_EMAIL` | Artifactory username / email |
| `ARTIFACTORY_TOKEN` | Artifactory identity token or API key |

These must be in `remoteEnv` in `devcontainer.json` so they are re-read from the host shell on every IDE reconnect.

**Configured package manager:**

| Manager | Registry URL | Config file |
|---------|-------------|-------------|
| Gradle | `https://gdartifactory1.jfrog.io/artifactory/java-virt` | `~/.gradle/init.d/artifactory.gradle` |

**This repo's own `build.gradle` also declares explicit Maven repos with their own credential blocks** (not covered by the feature's global init script):

| Repo | Env vars read directly by `build.gradle` |
|------|-------------------------------------------|
| `gradle-gdcorp-commerce-snapshots-local`, `gradle-gdcorp-commerce-releases-local` | `ARTIFACTORY_WRITE_USERNAME`, `ARTIFACTORY_WRITE_PASSWORD` |
| `java-virt` | `ARTIFACTORY_USERNAME`, `ARTIFACTORY_PASSWORD` |

`devcontainer.json`'s `remoteEnv` aliases all four to `ARTIFACTORY_EMAIL`/`ARTIFACTORY_TOKEN` so only the standard two credentials need to be set on the host.

**Check what is configured:**
```bash
cat /usr/local/share/gd-artifactory/post-create.sh
cat ~/.gradle/init.d/artifactory.gradle 2>/dev/null
```

**Verify connectivity** — requires `ARTIFACTORY_EMAIL` and `ARTIFACTORY_TOKEN` to be set:
```bash
./gradlew dependencies   # run from /workspace
```

**If a build reports "could not find artifact":** the feature's Maven mirror (`mirrorOf="*"`) routes every repo ID declared anywhere in the project through `mavenRepo`'s virtual repo, which silently shadows any repo ID `mavenRepo` doesn't aggregate. This repo's three declared repos (`gradle-gdcorp-commerce-snapshots-local`, `gradle-gdcorp-commerce-releases-local`, `java-virt`) all point at `gdartifactory1.jfrog.io` — the same host as `artifactoryUrl` — so no `additionalMavenRepoIds` exclusion was needed; add one only if a specific one of them turns out not to be aggregated by `mavenRepo` in practice.
