# DAS — DIGITAL ASSISTANT SELECTOR

<p align="center">
  <strong>A Modern Kotlin / Gradle Solution for DAS — Digital Assistant Selector — Built with High Reliability & Performance</strong>
</p>

<p align="center">
  <em>"An Android library that turns any app into a **digital assistant with on-screen
text selection**."</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Gradle-blue.svg?logo=kotlin&logoColor=white" alt="Kotlin / Gradle"/>
  <img src="https://img.shields.io/badge/Version-1.0.0-orange.svg" alt="Version 1.0.0"/>
  <img src="https://img.shields.io/badge/Layer-Backend%20%7C%20Core-brightgreen.svg" alt="Category"/>
  <img src="https://img.shields.io/badge/Platforms-JVM%20%7C%20Cross--Platform" alt="Platforms"/>
  <img src="https://img.shields.io/badge/Status-Active-brightgreen.svg" alt="Status"/>
</p>

---

## Table of Contents

- [Overview](#overview)
- [Core Features & Capabilities](#core-features--capabilities)
- [Architecture & Systems Design](#architecture--systems-design)
- [Controls & Interface Reference](#controls--interface-reference)
- [Project Structure & Code Map](#project-structure--code-map)
- [Getting Started & Development](#getting-started--development)
  - [Prerequisites](#prerequisites)
  - [Running the Application](#running-the-application)
  - [Running the Test Suites](#running-the-test-suites)
  - [Building & Packaging](#building--packaging)
- [Engineering Rules for Contributors](#engineering-rules-for-contributors)
- [Credits & License](#credits--license)

---

## Overview

An Android library that turns any app into a **digital assistant with on-screen
text selection**. Once the host app is configured as the device's default
assistant, DAS overlays the current screen with tappable bounding boxes around
every detected text element, lets the user pick the text they want, and hands
the selection back to the host app.
The implementation was extracted from [Mockery](https://github.com/iktwo/Mockery)
and litertlm, which both shipped near-identical copies of this mechanism.

### Key Highlights
- **Modern Architecture:** Strict separation of domain logic, state orchestration, and presentation layers.
- **Multiplatform Ready:** Built from the ground up to support responsive desktop, mobile, and native execution targets.
- **Deterministic & Resilient:** Designed with transaction-safe state changes, data validation, and fault-tolerant storage.
- **Developer Ergonomics:** Streamlined test runners, comprehensive type safety, and clean asset pipelines.

---

## Core Features & Capabilities

| Feature / Subsystem | Capability | Operational Status |
|---|---|---|
| **Shared Domain Engine** | Pure Kotlin common logic shared 100% across all target platforms | Multiplatform Verified |
| **Declarative UI** | Jetpack Compose / Compose Multiplatform reactive UI state management | Adaptive Layout |
| **Asynchronous Flow** | Kotlin Coroutines and StateFlow for frictionless event propagation | Non-blocking |
| **Modular Dependency Injection** | Scalable component wiring using Koin dependency injection | Production Ready |

---

## Architecture & Systems Design

**DAS — Digital Assistant Selector** is organized around strict separation of concerns to guarantee long-term maintainability, deterministic state updates, and isolated testability:

```
 +---------------------------------------------------------------+ 
 |                      Presentation Layer                       | 
 |   - User Input & Gesture Dispatch                             | 
 |   - Reactive State Observation & Screen Layout                | 
 +-------------------------------+-------------------------------+ 
                                 | (Actions / Events)             
                                 v                                
 +---------------------------------------------------------------+ 
 |                   Domain & State Coordinator                  | 
 |   - Immutable State Transitions & Reducers                    | 
 |   - Transaction Validation & Pre-execution Guards             | 
 +-------------------------------+-------------------------------+ 
                                 | (Storage / Network / IO)       
                                 v                                
 +---------------------------------------------------------------+ 
 |                   Infrastructure & Services                   | 
 |   - Persistence, Network Clients, Platform Adapters           | 
 +---------------------------------------------------------------+ 
```

### Key Architectural Tenets
1. **Single Source of Truth:** State flows downward through reactive observers; mutations are executed strictly via validated transactions.
2. **Separation of Presentation and Logic:** Domain rules are decoupled from UI rendering trees, enabling comprehensive headless verification.
3. **Resilient Persistence:** Saves, caches, and exports utilize atomic file swapping (`.tmp` write followed by atomic rename) to guard against mid-write power loss.

---

## Controls & Interface Reference

| Input / Interface | Action / Command | Description |
|---|---|---|
| `Primary Action` / `Enter` | Confirm / Execute | Triggers primary interactive action |
| `Secondary Action` / `Space` | Alternate / Inspect | Inspects element or performs contextual action |
| `Esc` / `Back` | Dismiss / Return | Backs out of modal dialogs or cancels current operation |
| `Tab` / `Shift+Tab` | Navigation | Moves focus between interactive controls and form inputs |

---

## Project Structure & Code Map

```text
digital_assistant_selector/
├── LICENSE
├── README.md          # Project documentation
├── build.gradle.kts
├── das/
│   ├── build.gradle.kts
│   ├── consumer-rules.pro
│   └── src/
├── gradle/
│   ├── gradle-daemon-jvm.properties
│   ├── libs.versions.toml     # Configuration
│   └── wrapper/
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
└── settings.gradle.kts
```

---

## Getting Started & Development

### Prerequisites
- **JDK 17+ or JDK 21+** (Temurin or Azul recommended)
- **Android Studio / IntelliJ IDEA** with Kotlin Multiplatform plugin
- Xcode 15+ (for iOS target compilation on macOS)

### Running the Application
```bash
# Run Desktop application
./gradlew run

# Run backend server (if applicable)
./gradlew :server:run
```

### Running the Test Suites
```bash
# Run unit tests across modules
./gradlew check
```

### Building & Packaging
```bash
# Build release distributions
./gradlew packageDistributionForCurrentOS
```

---

## Engineering Rules for Contributors

When contributing to **DAS — Digital Assistant Selector**, please adhere strictly to these core engineering standards:

1. **Data-Oriented Separation:** Keep domain logic and simulation models strictly decoupled from UI rendering code.
2. **Predictable State Transitions:** All mutations must flow through explicit commands or reducers. Avoid uncoordinated global state modifications.
3. **Atomic Persistence:** Save files and serialized state must be written to temporary staging files (`.tmp`) and swapped via atomic filesystem renames.
4. **Responsive & Accessible UI:** Maintain visible focus indicators, respect system safe-area insets, and ensure fluid resizing across resolutions.
5. **Strict Type Safety:** Leverage full typing across signatures, schemas, and API contracts; never bypass compiler or linter checks.
6. **Headless Verification:** Ensure core game and domain logic can execute headlessly without hardware display or audio devices.
7. **Asset Optimization:** Store vector assets as clean, optimized SVGs and verify rasterization across standard density targets (1x, 2x, 3x).
8. **Defensive Error Handling:** Catch invalid inputs and IO failures gracefully with clear, actionable diagnostic logging.
9. **Preserve User Data:** Never overwrite or invalidate prior saved user data without automated, tested migration routines.
10. **Clean Commits:** Write concise, atomic commit messages following conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`).

---

## Credits & License

Developed with care by **Isaac SH** ([@Iktwo](https://github.com/Iktwo)). Built with Kotlin / Gradle.

<p align="center">
  <sub>Crafted for high performance, modular architecture, and enduring quality.</sub>
</p>
