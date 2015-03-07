# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!

Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.
## Spesifikasi Aplikasi
Aplikasi ini adalah aplikasi simulasi penggunaan GPS Tracking, Geomagnetic Sensor dan QR Code Scanner
Terdapat 3 menu dalam aplikasi ini
- Show Map

   Aplikasi akan meminta geolocation  target ke server. Jika berhasil, aplikasi akan menampilkan posisi target di dalam sebuah map.

- Scan QR Code

   Aplikasi dapat melakukan scan terhadap QR Code. Jika berhasil, aplikasi dapat mengirimkan token hasil scan ke server. Respon dari server akan ditampilkan oleh aplikasi

- Show Compass

## Spesifikasi Endpoint
Endpoint yang digunakan pada aplikasi ini adalah 167.205.32.46/pbd
## License

MIT License