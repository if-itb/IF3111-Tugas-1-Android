# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!
Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.

## Spesifikasi Aplikasi
Functional Requirement 
1. GPS Tracking
Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry. Untuk jenis tampilan dibebaskan kepada peserta (misalnya map, atau penunjuk arah, atau indikator sederhana lainnya), silahkan dibuat sekreatif mungkin.
2. Geomagnetic Sensor
Peserta diminta memanfaatkan Geomagnetic Sensor untuk menggambarkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar.
3. QR-Code Scanner
Setelah mendapatkan posisi Jerry, peserta harus menangkap Jerry dengan melakukan scanning token dari QR-code yang disediakan dan dikirimkan ke server. Peserta diharapkan mencoba menggunakan library yang sudah tersedia di internet.

## Spesifikasi Endpoint
Alamat Server: 167.205.32.46/pbd
TRACK : [GET] /api/track?nim=13512053
CATCH : [POST] /api/catch

## Cara Penggunaan
Pada tampilan utama akan terdapat 2 button, yakni ShowMap button dan Catch button. Pada ShowMap button terdapat activity map yang memetakan keberadaan pengguna dengan target yaitu tempat persembunyian Jerry yang dapat diketahui setelah bertanya pada Spike (dalam hal ini adalah server tempat pengguna melakukan request JSON untuk mendapatkan koordinat lokasi Jerry serta waktu persembunyiannya). Setelah posisi persembunyian Jerry ditemukan, pengguna harus menangkap Jerry dengan cara melakukan scan terhadap QR code yang ditemukan di sekitar lokasi persembunyian Jerry, kemudian lapor pada Spike untuk validasi penangkapan. Penangkapan Jerry harus dilakukan dengan benar, artinya token QR code yang dibaca oleh pengguna harus sesuai dengan ketentuan yang ditentukan oleh server.

## License

MIT License