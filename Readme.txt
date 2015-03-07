Tugas 1 Android

Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!
Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.
Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike

Spesifikasi Aplikasi
Functional Requirement
1. GPS Tracking
Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry. Untuk jenis tampilan dibebaskan kepada peserta (misalnya map, atau penunjuk arah, atau indikator sederhana lainnya), silahkan dibuat se-kreatif mungkin.
2. Geomagnetic Sensor
Peserta diminta memanfaatkan Geomagnetic Sensor untuk menggambarkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar.
3. QR-Code Scanner
Setelah mendapatkan posisi Jerry, peserta harus menangkap Jerry dengan melakukan scanning token dari QR-code yang disediakan dan dikirimkan ke server. Peserta diharapkan mencoba menggunakan library yang sudah tersedia di internet.
(contoh : http://examples.javacodegeeks.com/android/android-barcode-and-qr-scanner-example/)
Non Functional Requirement
- Perangkat smartphone saat ini memiliki fitur yang sangat lengkap. Namun salah satu tantangan dalam menggunakan fitur-fitur tersebut adalah penggunaan daya baterai sesedikit mungkin karena daya baterai yang terbatas. Oleh karena itu pada tugas kali ini, peserta diharapkan memikirkan strategi yang baik agar aplikasi membutuhkan daya sesedikit mungkin.
- Aplikasi harus user friendly, artinya user tidak perlu selalu menekan tombol refresh untuk mendapatkan informasi terbaru dari Spike
- Source code diusahakan agar terstruktur dengan baik (Silahkan membaca guidelines standar yang dirilis di https://source.android.com/source/code-style.html)
- Minimal Android API 9 (GingerBread).
Spesifikasi Tampilan
Anda dibebaskan dalam berkreasi mendesain tampilan selama fitur-fitur pada spesifikasi aplikasi tercapai dan aplikasi dapat digunakan sesuai dengan alur pengujian yang diberikan.

Spesifikasi Endpoint
Alamat Server: 167.205.32.46/pbd
TRACK : [GET] /api/track?nim=<NIM_ANDA>
contoh pemanggilan:
/api/track?nim=13512000 (167.205.32.46/pbd/api/track?nim=13512000)
contoh kembalian:
{
“lat”: “-6.890323” // Koordinat latitude tujuan
“long”: “107.610381” // Koordinat longitude tujuan “valid_until”:1425833999 // Date dalam format UTC+7 kapan token hangus
}
CATCH : [POST] /api/catch
Header request:
Content-type: application/json
Parameter request:
token := “CONTOHTOKEN”
nim := “NIM_ANDA”
Contoh body request:
{"nim": "13512000", "token": "secret_token"}
Response status:
- status: 200 OK (Apabila pengumpulan sukses dengan token sesuai)
- status: 400 MISSING PARAMETER (Apabila parameter yang dikirim tidak lengkap)
- status: 403 FORBIDDEN (Apabila terdapat parameter yang salah)

License
MIT License