# සේවක පැමිණීමේ සහ නොපැමිණීමේ කළමනාකරණය (Diyathalawa Hospital Attendants)

Android application (Kotlin + Jetpack Compose + Material 3 + Room) for managing daily attendance, employee profiles, and monthly shift reports for the Cleaning Division, Base Hospital, Diyathalawa (`පවිත්‍රතා අංශය, මූලික රෝහල, දියතලාව`).

## Features

- **Attendance (`පැමිණීම`)**: Daily attendance tracking for **Day (`දිවා`)** and **Night (`රාත්‍රි`)** shifts with Present (`පැමිණ ඇත`), Absent (`නොපැමිණි`), and Leave (`නිවාඩු`) states, live counters, bulk actions, and date locking (`Lock` / `Unlock`).
- **Employees (`සේවකයන්`)**: Full CRUD management for staff profiles (name, employee number, role, NIC, shift assignment, join date, phone, emergency contact, blood group, date of birth, address), live search, one-tap phone dialing, and CSV export.
- **Reports (`වාර්තා`)**: Separate monthly matrix reports for Day and Night shifts (`✔`, `✘`, `L`, `-`) with month selection and UTF-8 CSV export.
- **Settings & Backup (`සැකසුම්`)**: Offline-first Room database persistence, Light/Dark theme toggle, JSON Backup & Restore (compatible with existing `Attendance_Backup_*.json` files), and Reset All Data.