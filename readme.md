Tugas 1 Android
Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala! Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu. Oleh karena itu dibutuhkan sebuah aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

Spesifikasi Aplikasi
1. GPS Tracking, Aplikasi memanfaatkan navigasi GPS tracking untuk membantu pengguna menangkan jerry
2. Geomagnetic Sensor, aplikasi memanfaatkan sensor geomagnetic sebagai petunjuk arah
3. QR-Code Scanner, aplikasi memiliki QR-code scanner untuk menangkap jerry
4. aplikasi dapat menerima lokasi jerry melalui informasi dari spike (endpoint)
5. aplikasi dapat meminta ulang lokasi jerry ketika ia berpindah tempat
	
Spesifikasi Endpoint
167.205.32.46/pbd

License
Jan Wira Gotama Putra / 13512015

Deskripsi
Aplikasi ini menggunakan google maps API V2 sebagai interface. Kompas terletak pada kiri bawah layar utama, sedangkan QR-Scanner (button) terletak pada bagian kanan bawah layar utama.
Pada aplikasi ini, terdapat sebuah task asynchronus untuk melakukan HttpGet Request ke endpoint, response tersebut akan diproses sebagai penanda pada MAP. HttpGet request tersebut dikirimkan secara berkala dan sesuai kebutuhan, sehingga tombol refresh tidak dibutuhkan pengguna. Saat pengguna melakukan scan terhadap QR-code, maka akan dikirim HttpPost.