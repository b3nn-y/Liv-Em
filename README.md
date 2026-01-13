# 📔 LIV EM: AI Life Architect

**LIV EM** is a sophisticated personal growth ecosystem built with **Kotlin Multiplatform (KMP)**. It bridges the gap between productivity tracking and emotional reflection by using a specialized **AI Agentic Framework** to analyze the alignment between your actions and your internal state.

<div align="center">
  <h3>📱 App Screenshots</h3>

  <table>
    <tr>
      <td width="33%"><img src="https://github.com/user-attachments/assets/e87fb564-b49a-47f2-b695-0155ee5feb25" alt="Home Screen"></td>
      <td width="33%"><img src="https://github.com/user-attachments/assets/22d4adb9-aafb-4382-a0eb-b4ff13700628" alt="Journal List"></td>
      <td width="33%"><img src="https://github.com/user-attachments/assets/4d6c93b3-edd6-4f17-b1f3-b2f91c42da48" alt="Missions"></td>
    </tr>
    <tr>
      <td width="33%"><img src="https://github.com/user-attachments/assets/16cd95d5-64de-4b3a-8c6e-56c2144265c2" alt="Locked Reports"></td>
      <td width="33%"><img src="https://github.com/user-attachments/assets/41d3d76c-90e9-4b7f-9f85-c8b161f76273" alt="Analysis"></td>
      <td width="33%"><img src="https://github.com/user-attachments/assets/117a6059-0d66-4936-bdb2-ed4045aa4509" alt="Missions List"></td>
    </tr>
    <tr>
      <td width="33%"><img src="https://github.com/user-attachments/assets/c1f0c236-3f79-431e-ab64-73a86782cd2b" alt="AI Review"></td>
      <td width="33%"><img src="https://github.com/user-attachments/assets/7a000898-14de-4b3a-8c6e-56c2144265c2" alt="Calendar Streak"></td>
      <td width="33%"><img src="https://github.com/user-attachments/assets/fd352c66-d706-4c32-a930-71bb7c15a86f" alt="Report Result"></td>
    </tr>
    <tr>
      <td width="33%"><img src="https://github.com/user-attachments/assets/ee68e0b6-d78e-4320-875e-7d106aca4696" alt="Final UI"></td>
      <td width="33%"></td> <td width="33%"></td> </tr>
  </table>
</div>

---
# 📔 LIV EM: AI Life Architect

**LIV EM** is a professional-grade personal growth ecosystem built with **Kotlin Multiplatform (KMP)**. It bridges the gap between productivity tracking and emotional reflection by using a specialized **AI Agentic Framework** to analyze the alignment between your actions and your internal state.

---

## 🚀 Features to Explore

* **🔥 Everyday Streaks:** Stay consistent! The app tracks your daily activity. Complete a mission or write a journal to maintain and grow your streak.
* **✅ Holistic Management:** * **Journaling:** Capture your story with full-featured entries.
    * **Daily ToDos:** Manage your day by creating, tracking, and completing missions.
* **🔍 Advanced Story Search:** Navigate your past with ease. The Journaling module includes advanced search and sorting implementation so you can find specific memories or themes instantly.
* **🏷️ Custom Organization:** * Add **custom tags** to your journals for thematic sorting.
    * **Bookmark/Favorite** specific entries to fetch your most important reflections easily.
* **🧠 AI Life Analysis:** Leverage your data. Based on your journal sentiment and task completion rates, the app generates a **Comprehensive Report**, offering a deep analysis of your current state and clear strategic steps for your future.

---

## 🗺️ Upcoming Features

### **Phase 1: Deep Reflection & Organization**
- [ ] **On Device AI:** For privacy reasons, using on device ai models.
- [ ] **Talk to Your Past Self:** An AI-powered interaction mode where you can "chat" with the person you were months ago based on your stored journals.
- [ ] **Creation of Threads:** Link related journals together to create a continuation of a specific thought or life event.

### **Phase 2: Health & Visualization**
- [ ] **AI Fitness Coach:** Personalized workout plans and real-time coaching integrated with your lifestyle and movement data.
- [ ] **Home Tab Data Population:** A fully dynamic dashboard giving you an "at-a-glance" view of your progress.

### **Phase 3: Periodic Insights**
- [ ] **Weekly & Monthly Reports:** Long-form automated summaries to track growth over larger time horizons.

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



**Please add your GEMINI API to the AiReflectionService() class before testing.
**
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
