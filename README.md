# 📔 LIV EM: AI Life Architect

**LIV EM** is a sophisticated personal growth ecosystem built with **Kotlin Multiplatform (KMP)**. It bridges the gap between productivity tracking and emotional reflection by using a specialized **AI Agentic Framework** to analyze the alignment between your actions and your internal state.

---

## 🚀 Key Features

### 🤖 AI Life Architect (Powered by Koog & Gemini)
The heart of the app is a "Life Architect" agent that doesn't just summarize—it analyzes.
* **Deep Life Review:** Curates your entire history into a structured behavioral analysis.
* **Alignment Gap Detection:** Identifies discrepancies between your journaled moods and actual mission output.
* **Strategic Pathfinding:** Provides actionable advice for the next month based on historical trends.

### 🎯 Mission Control
* **Daily Missions:** High-fidelity Task management using Material 3.
* **The Logbook:** Historical archive of completed tasks with "Restore" capabilities.
* **Eligibility Logic:** Reports are time-gated (7/30 days) to ensure enough "data fuel" for meaningful AI insights.

### ✍️ Reflective Journaling
* **Mindful Editor:** A clean, distraction-free environment for daily thoughts.
* **Metadata Tagging:** Automatic timestamping for accurate chronological AI context.

### 📊 Stunning Reports
* **State-Aware UI:** Reports load via suspend functions with real-time UI feedback.
* **Time Capsules:** All generated AI reports are persisted in SQLDelight for lifetime review.

---

## 🛠️ Technical Stack

| Layer | Technology |
| :--- | :--- |
| **Framework** | Kotlin Multiplatform (KMP) |
| **UI** | Compose Multiplatform (Material 3) |
| **AI Engine** | [Koog](https://docs.koog.ai/) + Google Gemini 1.5 Flash |
| **Database** | SQLDelight (SQLite) |
| **Dependency Injection** | Koin |
| **State Management** | Unidirectional Data Flow (UDF) |
| **Local Storage** | Multiplatform Settings |

---

## 🏗️ Architecture Detail



LIV EM utilizes a strict **Unidirectional Data Flow (UDF)**:
1.  **Curation:** `LiveEmDbUtil` converts SQLite rows into a "Structured Context String."
2.  **Agency:** The `AiReflectionService` (Koog Agent) receives context and a "Life Architect" system prompt.
3.  **Observation:** The `AppViewModel` manages the loading and result states via nullable `MutableState`.
4.  **Composition:** The UI reacts to state changes, automatically triggering Material 3 BottomSheets when analysis completes.

---

## ⚙️ Build & Configuration

### For Android
1. Open the project in **Android Studio**.
2. Wait for Gradle to sync.
3. Select an Android Emulator or physical device.
4. Click **Run** (Shift + F10).

### For iOS
*Requirements: macOS with Xcode installed.*
1. Open the `iosApp/iosApp.xcworkspace` file in **Xcode**.
2. Select an iPhone Simulator from the target list.
3. Click the **Play** button (Cmd + R).
4. *Alternative:* Run directly from Android Studio using the **Kotlin Multiplatform** plugin by selecting the `iosApp` configuration.

---
