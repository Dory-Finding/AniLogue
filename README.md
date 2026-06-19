# 🎧 AniLogue (アニログ)

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack-Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Media3](https://img.shields.io/badge/Media-ExoPlayer-red.svg)](https://developer.android.com/media/media3)

**AniLogue** (Anime, Dialogue, Log) adalah aplikasi pemutar media luring yang dirancang sebagai pendamping mandiri (_standalone companion app_) untuk memfasilitasi metode _active immersion_ dalam pemerolehan bahasa Jepang. Aplikasi ini secara khusus digunakan untuk memutar kompilasi percakapan padat hasil ekstraksi audio anime—di mana bagian tanpa dialog telah dipotong secara otomatis menggunakan skrip Python buatan pengembang—guna melatih kemampuan menyimak (_choukai_) secara persisten sepanjang hari.

---

## ✨ Fitur Utama

- **⏱️ Pemutar Audio Presisi Tinggi**: Dilengkapi dengan kontrol kecepatan (_playback speed_) mulai dari 0.5x hingga 2.0x, serta tombol navigasi presisi untuk melompat mundur atau maju sebanyak 2 detik (-2s / +2s) guna memudahkan metode _dictation_ (dikte).
- **📊 Pelacak Riwayat Otomatis (History Tracker)**: Mencatat setiap episode yang diputar dan menampilkannya secara kronologis di tab 'Library' untuk memantau progres dan konsistensi belajar pengguna.
- **📴 Mode Luring (100% Offline)**: Menggunakan basis data lokal untuk seluruh aset gambar dan audio (dirender dari folder `drawable` dan `raw`), sehingga aplikasi dapat berjalan tanpa latensi dan tanpa koneksi internet.
- **🌙 Antarmuka Imersif & Adaptif**: Menggunakan desain _Dark Mode_ untuk kenyamanan mata dan secara otomatis menyembunyikan bilah navigasi bawah (_bottom bar_) saat pengguna memasuki layar pemutar, memberikan ruang fokus yang maksimal.

---

## 📸 Tampilan Aplikasi

<p align="center">
  <img src="flow.png" width="800" alt="Tampilan Aplikasi AniLogue">
</p>

---

## 🛠️ Teknologi yang Digunakan

- **Bahasa Pemrograman**: Kotlin
- **UI Framework**: Jetpack Compose (Declarative UI) untuk membangun antarmuka yang reaktif dan _smooth_
- **Media Engine**: AndroidX Media3 (ExoPlayer) untuk menjamin stabilitas pemutaran audio di latar depan
- **Asynchronous Programming**: Kotlin Coroutines (melalui `LaunchedEffect`) untuk menangani pembaruan durasi audio (_progress bar_) secara _real-time_ tanpa membebani memori (_memory leak_)

---

## 🚀 Cara Memulai

### Instalasi

1. Unduh file APK AniLogue terbaru dari tab **[Releases](https://github.com/Dory-Finding/AniLogue/releases)**.
2. Instal file `.apk` tersebut di perangkat Android Anda (izinkan opsi _"Install from unknown sources"_ jika muncul peringatan keamanan).
3. Buka aplikasi AniLogue yang sudah terpasang di HP Anda.
4. Anda siap memilih episode anime dan memulai metode imersi.

---

## 📝 Cara Penggunaan

1. **Eksplorasi Katalog**: Buka aplikasi dan mulai dari tab **Home** untuk melihat daftar _cover_ anime yang tersedia untuk dipelajari.
2. **Memahami Konteks**: Klik pada _cover_ anime pilihan untuk masuk ke daftar episode. Baca sinopsis yang tersedia (klik **Baca Selengkapnya**) untuk memberikan konteks cerita pada otak sebelum sesi _listening_ dimulai.
3. **Sesi Pembelajaran**: Pilih episode yang diinginkan. Setelah pemutar audio terbuka, sesuaikan kecepatan (_Speed_) yang sesuai dengan kemampuan telinga, atau gunakan tombol **-2s/+2s** untuk mengulang frasa bahasa Jepang yang belum terdengar jelas.
4. **Pencatatan Progres**: Setelah sesi belajar selesai, buka tab **Library** untuk melihat daftar riwayat putar, lalu catat progres tersebut ke dalam _habit tracker_ di Notion.

---

## 🤝 Kontribusi

Kontribusi selalu terbuka! Jika Anda memiliki ide fitur atau menemukan _bug_, silakan buat _Issue_ atau kirimkan _Pull Request_.

---

## ⚖️ Lisensi

Distribusi di bawah Lisensi MIT. Lihat `LICENSE` untuk informasi lebih lanjut.

---

_Dibuat dengan ❤️ untuk komunitas pembelajar bahasa Jepang._
