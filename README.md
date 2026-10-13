# JWS Jam BT Android app

Aplikasi Android untuk mengendalikan sketch `JWS_Jam_BT.ino` lewat Bluetooth serial klasik (SPP), misalnya modul HC-05/HC-06.

## Build APK di GitHub (tanpa Android Studio)
1. Unggah semua isi folder ini ke repositori GitHub.
   Kalau folder `.github` tidak ikut terunggah: di GitHub buka file `build.yml` (di luar folder), tekan ikon pensil (Edit), ubah nama file di bagian atas menjadi `.github/workflows/build.yml` (garis miring otomatis membuat folder), lalu **Commit changes**.
2. Buka tab **Actions**, pilih **Build APK**, lalu **Run workflow** (atau cukup push ke branch `main`).
3. Setelah selesai (sekitar 3 sampai 6 menit), buka hasil run dan unduh **JWSJamBT-debug-apk** di bagian Artifacts.
4. Ekstrak zip-nya, kirim `app-debug.apk` ke HP, lalu pasang (izinkan pemasangan dari sumber tidak dikenal).

Link unduhan tetap: beri tag, misalnya `git tag v1.0 && git push origin v1.0`. APK otomatis terlampir di halaman **Releases**.

## Build APK di Android Studio
1. Buka folder ini lewat Android Studio versi terbaru.
2. Izinkan Android Studio mengunduh Android Gradle Plugin/SDK bila diminta.
3. Pilih **Build > Build APK(s)**.
4. APK debug berada di `app/build/outputs/apk/debug/app-debug.apk`.

## Sebelum menghubungkan
- Pasangkan HC-05/HC-06 melalui Settings Bluetooth Android terlebih dahulu.
- Pastikan baud rate modul dan sketch sama-sama 9600.
- Aplikasi mengirim perintah berakhiran newline, sesuai sketch.
- Waktu menggunakan format `SDTddmmyyhhmmssH`; `H`: Minggu=1 sampai Sabtu=7.

Catatan: lingkungan pembuatan saat ini tidak memiliki Android SDK/Gradle terpasang, sehingga APK belum dapat dikompilasi di sini. Source project ini siap dibuka dan dibuild di Android Studio.
