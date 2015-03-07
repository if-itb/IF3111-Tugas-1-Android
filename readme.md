# Tugas 1 Android

## Latar Belakang
Tom adalah kucing pemburu tikus paling hebat di kawasan ITB. Satu tikus yang belum berhasil ia tangkap bernama Jerry. Dengan penuh ambisius, Tom mengupayakan segala cara untuk menangkap Jerry. Dia mempunyai ide untuk menangkap Jerry saat Jerry masih berada di tempat persembunyiannya. Lalu bagaimana cara mencari tempat persembunyian Jerry?

Tom beruntung mempunyai anjing pelacak bernama Spike. Spike akan memberitahukan kepada Tom posisi dan waktu persembunyian Jerry. 

## Spesifikasi Aplikasi
Aplikasi ini akan membantu Tom untuk menemukan Jerry. Berikut spesifikasi aplikasi.
1. GPS Tracking
   Dengan fitur ini, Tom dapat mengetahui tempat persembunyian Jerry. Tom akan diberi peta dengan marker Jerry dan       dirinya. Ketika waktu bersembunyi Jerry habis, maka peta akan otomatis mengubah posisi marker Jerry.
2. Geomagnetic Sensor
   Dengan fitur ini, Tom dapat mengetahui arah mata angin. Arah mata angin ini akan bergerak sesuai dengan perubahan pada sensor. 
3. QR-Code Scanner
   Dengan fitur ini, Tom dapat mengetahui token dari sebuah QR-Code. Kemudian token ini akan dikirimkan ke endpoint (Spike). Kemudian aplikasi akan menerima informasi keberhasilan penangkapan Jerry.

## Spesifikasi Endpoint
   Endpoint merupakan perwujudan dari Spike. Alamat server yang digunakan adalah 167.205.32.46/pbd. Untuk mendapatkan posisi dan waktu persembunyian Jerry menggunakan api track dengan pemanggilan [GET] 167.205.32.46/pbd/api/track?nim=13512084. Untuk mengirimkan token menggunakan api catch dengan mengirimkan [POST] 167.205.32.46/pbd/api/catch. 

## License

MIT License
