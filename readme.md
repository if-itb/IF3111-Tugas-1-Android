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
Tom dapat melakukan  tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga 
perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry  seringkali berpindah tempat 
persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian 
Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.
Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom 
untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan 
Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu 
juga dapat melaporkan penangkapan Jerry kepada Spike.

## Spesifikasi Aplikasi

Alur jalannya aplikasi ini yaitu mula-mula terdapat menu utama. Dari situ, terdapat tombol "Tangkap Jerry" untuk mengetahui lokasi Jerry yang diberitahu oleh Spike. Lokasi Jerry bisa pindah sesuai dengan waktu yang ditentukan. Setelah ditemukan, Jerry ditangkap dengan cara melakukan pemindaian QR code yang ada. Setelah pemindaian, kembali melapor pada Spike.
Functional Requirement :
1. GPS Tracking
Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry. 
2. Geomagnetic Sensor
Peserta diminta memanfaatkan Geomagnetic Sensor untuk menggambarkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar.
3. QR-Code Scanner 
Setelah mendapatkan posisi Jerry, peserta harus menangkap Jerry dengan melakukan scanning token dari QR-code yang disediakan dan dikirimkan ke server.

Non Functional Requirement
- Penggunaan daya baterai yang minim
- User friendly
- Source code terstruktur
- Minimal Android API 9 (GingerBread).

## Spesifikasi Endpoint

Alamat Server: 167.205.32.46/pbd TRACK : [GET] /api/track?nim=

CATCH : [POST] /api/catch
 
Header request: 
Content-type: application/json 

Parameter request: 
token := “CONTOHTOKEN” 
nim := “NIM_ANDA”

Response status:

status: 200 OK (Apabila pengumpulan sukses dengan token sesuai)
status: 400 MISSING PARAMETER (Apabila parameter yang dikirim tidak lengkap)
status: 403 FORBIDDEN (Apabila terdapat parameter yang salah)

## License

MIT License