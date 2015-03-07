# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!
Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.
Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Spesifikasi Aplikasi
1. GPS Tracking
Aplikasi dapat menampilkan posisi Jerry secara real-time tanpa harus menekan tombol. Posisi Jerry didapatkan dengan mengirimkan request ke server(Spike).

2. Geomagnetic Sensor
Aplikasi dapat menunjukkan arah utara dan selatan dengan benar dengan memanfaatkan Geomagnetic Sensor.

3. QR-Code Scanner
Aplikasi dapat melakukan scanning token dari QR-code yang disediakan dan mengirimkannya ke server serta menerima respon dari server terkait token tsb apakah diterima atau tidak

Selain hal diatas, aplikasi jg diharapkan dapat mengggunakan daya baterai sekecil mungkin dan user-friendly. Selain itu API yang digunakan harus versi API >=9 dan source code terstruktur dengan baik.

## Spesifikasi Endpoint
Alamat Server request lokasi spike: http://167.205.32.46/pbd/api/track?nim=13512095 (GET)
Alamat Server post token: http://167.205.32.46/pbd//api/catch (POST)

Header pengiriman token:
Content-type: application/json

Body pengiriman token:
{"nim": "13512000", "token": "secret_token"}

Response status dari server terkait pengiriman token:
- status: 200 OK (Apabila pengumpulan sukses dengan token sesuai)
- status: 400 MISSING PARAMETER (Apabila parameter yang dikirim tidak lengkap)
- status: 403 FORBIDDEN (Apabila terdapat parameter yang salah)

## Cara Penggunaan Aplikasi
Aplikasi dapat dijalankan dengan cara mengimport project ke android studio dan melakukan run aplikasi nya pada device/emulator.

Pemakaian:
Terdapat label:
1. Latitude		: latitude posisi jerry saat ini
2. Longitude	: longitude posisi jerry saat ini
3. Expire		: waktu expirenya posisi jerry saat ini

Terdapat 2 tombol:
1. Show map -> Untuk menunjukkan lokasi jerry saat ini pada peta
2. Scan QR -> untuk melakukan scan terhadap QR Code dan mengirimkan hasilnya ke server

## License
MIT License