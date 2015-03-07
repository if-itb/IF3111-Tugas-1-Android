# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah  arena Jerry akan berpindah posisi secara berkala! Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu. Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Spesifikasi Aplikasi
###Functional Requirement
1. GPS Tracking
Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry. Untuk jenis tampilan dibebaskan kepada peserta (misalnya map, atau penunjuk arah, atau indikator sederhana lainnya), silahkan dibuat sekreatif mungkin.

2. Geomagnetic Sensor
Peserta diminta memanfaatkan Geomagnetic Sensor untuk menggambarkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar.

3. QR-Code Scanner
Setelah mendapatkan posisi Jerry, peserta harus menangkap Jerry dengan melakukan scanning token dari QR-code yang disediakan dan dikirimkan ke server. Peserta diharapkan mencoba menggunakan library yang sudah tersedia di internet. (contoh : http://examples.javacodegeeks.com/android/android-barcode-and-qr-scanner-example/)

###Non Functional Requirement 
- Perangkat smartphone saat ini memiliki fitur yang sangat lengkap. Namun salah satutantangan dalam menggunakan fitur-fitur tersebut adalah penggunaan daya baterai
sesedikit mungkin karena daya baterai yang terbatas. Oleh karena itu pada tugas kali ini, peserta diharapkan memikirkan strategi yang baik agar aplikasi membutuhkan daya sesedikit mungkin.
- Aplikasi harus user friendly, artinya user tidak perlu selalu menekan tombol refresh untuk mendapatkan informasi terbaru dari Spike
- Source code diusahakan agar terstruktur dengan baik (Silahkan membaca guidelines standar yang dirilis di https://source.android.com/source/code-style.html)
- Minimal Android API 9 (GingerBread).

###Spesifikasi Tampilan
Anda dibebaskan dalam berkreasi mendesain tampilan selama fitur-fitur pada spesifikasi aplikasi tercapai dan aplikasi dapat digunakan sesuai dengan alur pengujian yang diberikan.

### Spesifikasi Endpoint
Alamat Spike (server yang digunakan) berada di "167.205.32.46/pbd". API untuk meminta lokasi Jerry menggunakan request "/api/track?". API untuk mengirimkan laporan penangkapan Jerry menggunakan request "/api/catch"

## License

###MIT License
Copyright (c) 2015 

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
