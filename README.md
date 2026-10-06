# CSI Events 📱✨

> 🌟 A beautifully crafted Android app for managing CSI events with a modern, futuristic UI and role-based workflows.

---

## 🚀 Overview

![Android](https://img.shields.io/badge/Android-App-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)

CSI Events is a polished mobile application built with Kotlin and Jetpack Compose for organizing and running event-based competitions. It brings together organizers, judges, and students in one seamless experience for managing events, teams, scoring, and real-time participation. 🌐

---

## 🎯 What this app does

This project is designed for three main roles:

- 🧑‍💼 Organizer: create events, manage roles, assign judges, view leaderboards, and lock scoring
- 🧑‍⚖️ Judge: review assigned events and teams, evaluate criteria, and submit scores
- 🎓 Student: explore events, join teams, and participate in the competition flow

---

## ✨ Highlights

- 🔐 Smooth login and registration experience
- 🧭 Role-based dashboards and navigation
- 🏗️ Event creation and management workflow
- 👥 Team and judge assignment system
- 🏆 Leaderboard and scoring support
- 🎨 Animated, premium-looking UI with Compose
- 🌩️ API-driven architecture with secure auth handling

---

## 🛠️ Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-5C2D91?style=for-the-badge&logo=android&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-00BFA5?style=for-the-badge&logo=android&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-6A1B9A?style=for-the-badge&logo=kotlin&logoColor=white)


---

## 📁 Project Structure

The app is organized into clean, layered folders:

- app/src/main/java/com/example/csievent/data/ — repositories, local storage, API clients, DTOs
- app/src/main/java/com/example/csievent/domain/ — models and repository contracts
- app/src/main/java/com/example/csievent/presentation/ — screens, view models, and UI components
- app/src/main/java/com/example/csievent/di/ — dependency injection modules
- app/src/main/res/ — resources, themes, strings, and launcher assets

---

## ▶️ Getting Started

1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Run the app on an emulator or physical device.
4. Ensure the backend API is reachable at the configured base URL.

---

## 🌐 API Configuration

The backend URL is set in `app/build.gradle.kts` (`BASE_URL`, default
https://csieventmangement.onrender.com/). To test a debug build against a backend running on
your PC:

```bash
./gradlew installDebug -PapiUrl=http://10.0.2.2:8080/
```

(`10.0.2.2` is your PC from the emulator; on a phone use your PC's LAN IP.)

---

## 🚀 Releasing to Google Play

App ID: `com.csi.events` (permanent once published). Target SDK 36.

**1. Create an upload key (once).** Keep the `.jks` file and passwords safe — you need them for
every update.

```bash
keytool -genkeypair -v -keystore csi-events-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias csi-events
```

**2. Create `keystore.properties`** in the project root (it is gitignored):

```properties
storeFile=csi-events-upload.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=csi-events
keyPassword=YOUR_KEY_PASSWORD
```

**3. Build the bundle:**

```bash
./gradlew bundleRelease
```

Upload `app/build/outputs/bundle/release/app-release.aab` in Play Console and enrol in
**Play App Signing**. Before each release bump `versionCode` (and `versionName`) in
`app/build.gradle.kts`. Test the signed release build on a phone first with
`./gradlew installRelease`.

**Play Console checklist**

- Privacy policy URL: `https://csieventmangement.onrender.com/privacy-policy`
- Data safety: collects name, email (account management); not shared; encrypted in transit;
  users can request deletion.
- Account deletion URL: `https://csieventmangement.onrender.com/delete-account`
  (in-app: Profile → Delete account)
- App access: give reviewers a test student account (and a judge/organizer if you want those
  screens reviewed).
- New personal developer accounts must run a closed test with at least 12 testers for 14 days
  before production access.

---

## 🧩 Main Screens

- Splash screen
- Login screen
- Registration screen
- Profile (logout, privacy policy, delete account) — all roles
- Organizer dashboard (lock / unlock scoring, delete event)
- Event creation screen
- Criteria management
- Judge assignment (all teams or specific teams per judge)
- Judge dashboard, team list (shows scored teams) and scoring screen
- Student dashboard, team screen, and results once scoring is locked
- Role management and leaderboard views

---

## 📝 Notes

This project is a great example of a modern Android app using Compose, layered architecture, and interactive UI design. It is perfect for learning, demos, and extending with new features. 📚

---

## ✅ Final Thoughts

CSI Events combines elegant design with practical functionality to deliver a futuristic event-management experience right on your phone. 🚀
