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

1. GPS Tracking
Aplikasi menampilkan posisi Jerry pada map untu membantu pengguna menangkap jerry.

2. Geomagnetic Sensor
Aplikasi menggunakan geomagnetic sensor untuk menunjukkan arah mata angin.

3. QR-Code Scanner
Aplikasi dapat melakukan scan terhadap QR-code untuk menangkap Jerry dengan membaca token dari QR-code. Token kemudian dikirimkan kepada Spike.

## Spesifikasi Endpoint

Alamat server: 167.205.32.46/pbd

## License

Mario Filino - 13512055

## Cara Pemakaian

1. Ketika aplikasi dijalankan, pada halaman awal dapat dilihat posisi dan lama persembunyian Jerry saat itu. 

2. Untuk melihat posisi Jerry pada peta, tekan tombol "Show Map". Di halaman peta, juga terdapat kompas untuk mempermudah pengguna menentukan arah.

3. Untuk menangkap Jerry, tekan tombol "Catch Jerry" dari halaman utama aplikasi kemudian scan QR-code untuk mendapatkan token penangkapan. Setelah mendapatkan token, aplikasi akan secara otomatis mengirimkan token tersebut kepada spike.