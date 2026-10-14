# Devcontainer — SampleSPIService
Base image only — Java 17 (Gradle) + GoDaddy Artifactory. No stunt-doubles services wired.

| | |
|---|---|
| Base image | mcr.microsoft.com/devcontainers/base:ubuntu |
| Start | `atlas-container start` or VS Code F1 → "Reopen in Container" |
| Shell | `atlas-container shell` |
| Host credentials | ARTIFACTORY_EMAIL, ARTIFACTORY_TOKEN, GIT_TOKEN, GOCODE_API_TOKEN, GOCODE_BASE_URL |
| stunt CLI | `stunt ps` · `stunt up <service>` · `stunt down <service>` (inside container) |

This repo builds Android system services (Poynt payment/security SPI) with no network listener and no run command — see `.devcontainer/README.md` § Running the app. The devcontainer provisions a JDK and Gradle but **not** the Android SDK the build itself needs (`compileSdkVersion`/build-tools) — that must be added manually; there is no catalog feature for it.

In-container context: `.devcontainer/agent/agents-devcontainer.md` (auto-loaded by Claude Code inside the container).
