# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang

## Spesifikasi Aplikasi
Terdapat 3 menu pada aplikasi ini
- QR Code scanner
- Maps
- Compass

- QR Code scanner
Digunakan untuk melakukan scan QR Code. Hasil dari scan dapat diunggah ke endpoint. Respon dari server akan ditampilkan oleh aplikasi.

- Maps
Digunakan untuk melakukan tracking posisi anda sekarang dan posisi target. Pastikan terdapat **koneksi internet**. Posisi target diupdate secara otomatis, namun anda dapat melakukan *force update* pada posisi target.

- Compass
Digunakan untuk menentukan arah mata angin. Compass ini juga tersedia pada fitur peta.

## Spesifikasi Endpoint
Endpoint digunakan untuk mengambil data lokasi jerry.

## License

MIT License