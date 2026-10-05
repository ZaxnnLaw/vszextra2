VSZE Extras: ArmorHUD, PotionHUD, Freelook, Motion Blur, Zoom, Keystrokes, Badge, Friends + chat (menu Right Shift / tombol VSZE di menu ESC)

Build di GitHub (tanpa install apa-apa):
  1. Buat repo baru, upload ISI folder ini (termasuk folder .github).
  2. Tab Actions > build-vsze-extras. Tiap versi Minecraft dibuild sendiri-sendiri (matrix).
  3. Download artifact "vsze-extras-<versi>" yang hijau, taruh jarnya di core-mods/ launcher.
     Nama file sudah sesuai yang dicari launcher: vsze-extras-<versi>.jar
  4. Versi yang merah = kode belum cocok dengan versi itu; buka lognya, kirim error-nya untuk diperbaiki.

Versi di build.yml: 1.21 sampai 1.21.11 (12 versi). Tambah/kurangi di baris "mc:".
Versi yarn, loader, Fabric API, Loom, dan Gradle dicari/dipilih otomatis oleh workflow.
Build lokal: gradle build -Pminecraft_version=1.21.1 (nilai lain dari gradle.properties).

Struktur kode (API Minecraft berubah antar versi, jadi dibagi kelompok):
  src/main  kode umum untuk semua versi
  src/v1    1.21 - 1.21.1    (kode asli: Motion Blur, loading screen VSZE)
  src/v2    1.21.2 - 1.21.5  (drawTexture pakai RenderLayer)
  src/v3    1.21.6 - 1.21.8  (drawTexture pakai RenderPipeline)
  src/v4    1.21.9 - 1.21.11 (Click/KeyInput, KeyBinding.Category, GameProfile record)
Kelompok dipilih otomatis oleh build.gradle dari minecraft_version.
Tiap kelompok punya Compat.java (bagian yang API-nya beda) dan vsze-extras.mixins.json sendiri.

Perbedaan fitur:
  - Motion Blur dan loading screen VSZE hanya ada di 1.21 - 1.21.1 (sistem post-shader dan splash
    berubah total di 1.21.2+). Di versi lain, toggle Motion Blur otomatis disembunyikan dari menu VSZE.
  - Background menu utama VSZE di 1.21.2+ dipasang lewat renderPanoramaBackground. Kalau method itu
    ternyata tidak cocok di suatu versi, menu utama tampil vanilla (logo MINECRAFT tidak disembunyikan).

Setting HUD di game: ketik /vszeextra (alias /vszehud). Posisi, latar, warna disimpan di ~/.vsze/hud.json.
Modul PvP baru (default mati): reach (jarak pukulan) dan items (totem/pot/gapple/crystal). Kode umum ada di src/main, jadi berlaku di semua versi.
