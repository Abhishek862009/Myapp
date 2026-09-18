# Countdown Dots — Android App (source code)

Ye poora Android Studio project hai. Isme app ka wallpaper **har roz khud-ba-khud update hota hai** — ek dot roz color badal leta hai, kisi manual download/set ki zaroorat nahi.

## Kaise build karein

1. [Android Studio](https://developer.android.com/studio) install kar (free hai).
2. Is `CountdownWallpaperApp` folder ko **File → Open** karke Android Studio mein khol.
3. Studio khud Gradle sync karega, thoda wait kar (pehli baar 2-5 min lagega, internet chahiye).
   - Agar "Gradle wrapper missing" jaisa message aaye, to Studio khud prompt karega "Create Gradle wrapper" — usko accept kar de.
4. Ek Android phone USB se connect kar (Developer Options + USB Debugging on kar), ya ek emulator bana le.
5. Upar **Run ▶** button dabao. App phone mein install ho jayegi.

## App use kaise karein

1. App kholke apni **start date** aur **target/exam date** set kar.
2. Total days, colours (dots pe tap karke cycle hota hai), background photo — sab customize kar.
3. **"Save + Set Wallpaper Now"** dabao. Ye do cheezein karta hai:
   - Turant wallpaper set kar deta hai (home + lock screen dono).
   - Ek daily background job schedule kar deta hai jo roz apne aap wallpaper regenerate karke set karta rahega.
4. Bas — ab app dobara kholne ki zaroorat nahi, ye khud chalta rahega. Din guzarne ke saath dots apne aap fill/change hote jayenge.

## Kaise kaam karta hai (technical)

- `WallpaperGenerator.kt` — dono live preview aur daily update ke liye ek hi function se wallpaper bitmap banata hai (dot grid, colors, background photo, sab is file mein).
- `DailyWallpaperWorker.kt` — Android ka `WorkManager` use karta hai, jo ek `PeriodicWorkRequest` (24-hour interval) schedule karta hai. Ye system-level scheduler hai, isliye phone reboot ya battery-optimisation ke baad bhi chalta rehta hai.
- `Prefs.kt` — saari settings (dates, colours, photo, spacing) `SharedPreferences` mein save hoti hain, taaki worker background mein bhi unhe padh sake.
- `BootReceiver.kt` — phone restart hone par schedule ko dobara confirm karta hai (safety net; WorkManager khud bhi persist karta hai).

## Notes

- Kuch phones (Xiaomi/MIUI, Oppo, Vivo, OnePlus etc.) apni aggressive battery optimisation ki wajah se background apps ko kill kar dete hain. Agar daily update na ho, to phone ki Settings → Battery → App ke liye "No restrictions" / "Autostart allowed" kar dena.
- Android 13+ pe photo permission (`READ_MEDIA_IMAGES`) app pehli baar photo choose karte waqt khud maang legi.
