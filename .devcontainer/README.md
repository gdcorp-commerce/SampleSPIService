# SampleSPIService Devcontainer

Java 17 (Gradle) devcontainer with GoDaddy Artifactory wiring. No stunt-doubles services wired — this app has no external HTTP/service dependencies to mock.

## What is this?

A **devcontainer** is a pre-configured development environment that runs in Docker. Once set up, `atlas-container start` gives you a fully wired container with the project toolchain, AI coding agents, and all dependencies ready to go — no local language/runtime setup, no manual credential juggling.

📖 **Quick start guide:** [Code Atlas - Containers](https://godaddy-corp.atlassian.net/wiki/spaces/SELLMORE/pages/4447535626/Code+Atlas+-+Containers)

---

## Starting the container

**Option A — atlas-container CLI (recommended):**
```sh
atlas-container start          # start or attach (from repo root)
atlas-container shell          # open a bash shell in a running container
atlas-container stop           # stop the container (preserves state)
atlas-container rebuild        # rebuild the image from scratch
atlas-container rm             # remove the container (keeps cached images)
atlas-container rm --purge     # full cleanup including cached images
```

Install once: `npm install -g @code-atlas/atlas-container --registry=https://gdartifactory1.jfrog.io/artifactory/api/npm/node-virt/`

First-time setup: `atlas-container setup && atlas-container login`

**Option B — VS Code:**
`F1` → "Dev Containers: Reopen in Container"

**Option C — JetBrains Gateway:**
Gateway → Remote Development → Dev Containers → Open Dev Container

---

## Running the app

Install: `./gradlew dependencies`
Build: `./gradlew assembleDebug`
Run: not applicable — this repo builds Android system services (`PaymentSPIService`, `SecuritySPIService`) that are installed onto Poynt POS hardware, not an app you run locally. There is no Activity/UI and no network port.

> **⚠ Android SDK not provisioned.** This devcontainer installs a JDK and Gradle, but not the Android SDK (`compileSdkVersion 30` / `targetSdkVersion 34` / build-tools) that `app/build.gradle` needs to actually compile. Install it manually inside the container (e.g. `sdkmanager "platforms;android-34" "build-tools;30.0.0"` after downloading the command-line tools) and point `local.properties`'s `sdk.dir` at it, or add your own `postCreateCommand` to automate this — there is no catalog feature for it yet.

---

## Regenerating

```sh
/gdc:scaffold-devcontainer
```

Then run `atlas-container rebuild` to pick up the updated `devcontainer.json`.
