# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling
populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu
hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan
bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika
berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di
dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!
Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike,
Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga
perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat
persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian
Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.
Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom
untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan
Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu
juga dapat melaporkan penangkapan Jerry kepada Spike.

## Spesifikasi Aplikasi
Functional Requirement:
1. GPS Tracking
Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga
diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah.
Aplikasi dapat menampilkan posisi Jerry. Untuk jenis tampilan dibebaskan kepada peserta
(misalnya map, atau penunjuk arah, atau indikator sederhana lainnya), silahkan dibuat sekreatif mungkin.

2. Geomagntic Sensor
Peserta diminta memanfaatkan Geomagnetic Sensor untuk menggambarkan arah mata angin
yang menunjukkan arah utara dan selatan dengan benar.

3. QR-Code Scanner
Setelah mendapatkan posisi Jerry, peserta harus menangkap Jerry dengan melakukan
scanning token dari QR-code yang disediakan dan dikirimkan ke server. Peserta diharapkan
mencoba menggunakan library yang sudah tersedia di internet.

Non Functional Requirement:
-Perangkat smartphone saat ini memiliki fitur yang sangat lengkap. Namun salah satu
tantangan dalam menggunakan fitur-fitur tersebut adalah penggunaan daya baterai
sesedikit mungkin karena daya baterai yang terbatas. Oleh karena itu pada tugas kali
ini, peserta diharapkan memikirkan strategi yang baik agar aplikasi membutuhkan daya
sesedikit mungkin.
- Aplikasi harus user friendly, artinya user tidak perlu selalu menekan tombol refresh
untuk mendapatkan informasi terbaru dari Spike
- Source code diusahakan agar terstruktur dengan baik (Silahkan membaca guidelines
standar yang dirilis di https://source.android.com/source/code-style.html)
- Minimal Android API 9 (GingerBread).

Tampilan:

## Spesifikasi Endpoint
Alamat server: 167.205.32.46/pbd
1. Response Lokasi Endpoint
Endpoint akan mengembalikan response dengan format JSON ketika pengguna melakukan request pada endpoint dengan method GET
TRACK : [GET] /api/track?nim=<NIM_ANDA>
Hasil kembalian dari server adalah:
Latitute, Longitude, dan Valid_Until

2. Response submisi token
Endpoint akan mengembalikan response dengan format JSON ketika pengguna melakukan submisi nim dan token dengan method POST
format request: {"nim":"13512043","token":"secret_token"}
CATCH : [POST] /api/catch
dan memberikan kembalian berupa status yakni:
- status: 200 OK (Apabila pengumpulan sukses dengan token sesuai)
- status: 400 MISSING PARAMETER (Apabila parameter yang dikirim tidak lengkap)
- status: 403 FORBIDDEN (Apabila terdapat parameter yang salah)


## License

MIT License