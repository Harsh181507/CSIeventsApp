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

The application uses the following backend endpoint:

- https://csieventmangement.onrender.com/

This base URL is wired into the networking layer for auth, events, teams, scores, and related operations. 📡

---

## 🧩 Main Screens

- Splash screen
- Login screen
- Registration screen
- Organizer dashboard
- Event creation screen
- Judge dashboard and criteria screen
- Student dashboard and team screen
- Role management and leaderboard views

---

## 📝 Notes

This project is a great example of a modern Android app using Compose, layered architecture, and interactive UI design. It is perfect for learning, demos, and extending with new features. 📚

---

## ✅ Final Thoughts

CSI Events combines elegant design with practical functionality to deliver a futuristic event-management experience right on your phone. 🚀
