# Dev Container for IntelliJ IDEA

This dev container uses official devcontainer features instead of a custom Dockerfile, making it simpler and more maintainable.

## Prerequisites

- Docker Desktop installed and running
- IntelliJ IDEA Ultimate (2023.3 or later) with Gateway plugin

## Setup Instructions

### Using IntelliJ IDEA Gateway

1. Install JetBrains Gateway (standalone or via IntelliJ IDEA)
2. Open Gateway and select "New Project from VCS"
3. Clone this repository
4. Gateway will automatically detect and use the devcontainer configuration

### Using VS Code (for testing)

1. Install the "Dev Containers" extension
2. Open Command Palette → "Dev Containers: Reopen in Container"

## Features

The devcontainer automatically installs:

- **Java 17** (Eclipse Temurin JDK)
- **Gradle** (latest version)
- **GitHub CLI** for GitHub integration
- **Claude Code CLI** for AI assistance
- **Docker-in-Docker** for containerized testing
- **Common utilities** (git, zsh, oh-my-zsh)

## Environment

- Base image: Ubuntu (Microsoft devcontainers base)
- Working directory: `/workspaces/openapi-test`
- Gradle cache: Persisted locally at `.gradle`
- User: `vscode` (with sudo access)

## Useful Aliases

The container includes these Gradle aliases:
- `gw` → `./gradlew`
- `gwb` → `./gradlew build`
- `gwt` → `./gradlew test`
- `gwc` → `./gradlew clean`
- `gwr` → `./gradlew run`

## Accessing Claude Code

Claude Code is automatically installed and configured. You can use the `claude` command directly in the terminal.

## Network Configuration

The container runs with host network mode to enable seamless access to local services and Claude Code.