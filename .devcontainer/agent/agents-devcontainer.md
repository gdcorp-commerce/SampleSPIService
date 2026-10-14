# Devcontainer — SampleSPIService

Running inside the SampleSPIService devcontainer.

## Environment
- Base image: mcr.microsoft.com/devcontainers/base:ubuntu
- Language: Java 17 (Gradle) — version guessed, not declared in the repo; required by Android Gradle Plugin 8.7
- Workspace: /workspace
- This is an Android app (Poynt payment/security SPI services, no Activity/UI) — build and unit-test only; it has no run command and no network port. The Android SDK is not provisioned by this devcontainer — see `.project-docs/context/devcontainer.md`.

## Credentials available (forwarded from host shell)
- ARTIFACTORY_EMAIL / ARTIFACTORY_TOKEN — GoDaddy Artifactory (aliased to ARTIFACTORY_USERNAME/PASSWORD and ARTIFACTORY_WRITE_USERNAME/PASSWORD, which this repo's build.gradle reads directly)
- GOCODE_API_TOKEN / GOCODE_BASE_URL — AI coding agents (gocode LiteLLM proxy)

## Context files
| Topic | File |
|-------|------|
| Claude Code — credentials, denied ops, MCP wiring | [claude-code.md](claude-code.md) |
| Container safety — git mode, blocked hosts, MCPs | [devcontainer-commons.md](devcontainer-commons.md) |
| Artifactory registry config | [gd-artifactory.md](gd-artifactory.md) |
| Certs, Docker network, CA trust | [stunt-doubles-core.md](stunt-doubles-core.md) |
