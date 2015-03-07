# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang

Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!

Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.

## Spesifikasi Aplikasi

### 1. GPS Tracking

Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry dalam Google Map beserta sisa waktu sebelum Jerry berpindah tempat.

### 2. Geomagnetic Sensor

Terdapat kompas yang menggunakan Geomagnetic Sensor untuk menggambarkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar yang dapat digunakan untuk mengejar Jerry.

### 3. QR-Code Scanner

Setelah mencapai posisi Jerry, Jerry dapat ditangkap menggunakan QR-Code Scanner yang telah disediakan dalam aplikasi.

## Spesifikasi Endpoint

### Alamat Server: 167.205.32.46/pbd

### TRACK : [GET] /api/track?nim=<NIM_ANDA>

Contoh kembalian : { “lat”: “-6.890323”, “long”: “107.610381”, “valid_until”:1425833999 }

### CATCH : [POST] /api/catch

Response status:
- status: 200 OK (Apabila pengumpulan sukses dengan token sesuai)
- status: 400 MISSING PARAMETER (Apabila parameter yang dikirim tidak lengkap)
- status: 403 FORBIDDEN (Apabila terdapat parameter yang salah)

## License

MIT License