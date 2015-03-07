# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!  
Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.  
Oleh karena itu, diperlukan aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Spesifikasi Aplikasi
A.	Functional Requirement  
    1.	GPS Tracking
        Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu            UTC+7. Setelah waktu persembunyian habis, aplikasi dapat meminta ulang posisi Jerry ketika posisi Jerry berubah.             Aplikasi dapat menampilkan posisi Jerry.
    2.	Geomagnetic Sensor 
        Aplikasi dapat menampilkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar. 
    3.	QR-Code Scanner 
        Setelah mendapatkan posisi Jerry melalui alamat server http://167.205.32.46/pbd/api/track, Jerry ditangkap dengan            melakukan scanning token dari QR-code yang disediakan dan dikirimkan ke server.

B.	Non Functional Requirement
    1.	Aplikasi hanya membutuhkan daya baterai sedikit.
    2.	Aplikasi ini user friendly, artinya user tidak perlu selalu menekan tombol refresh untuk mendapatkan informasi               terbaru dari Spike.


## Spesifikasi Endpoint
1.	Alamat server yang digunakan adalah 167.205.32.46/pbd (Alamat spike).
2.	API untuk mendapatkan posisi Jerry menggunakan request /api/track?nim=<NIM>
3.	API untuk mengirimkan hasil penangkapan Jerry menggunakan request api/catch

## License
Windy Amelia - 13512091 
MIT License
