# Deskripsi Aplikasi
Aplikasi android yang bertujuan membantu Tom untuk mencari dan menangkap Jerry. Aplikasi ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi ini juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala! Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu. Oleh karena itu dibutuhkan sebuah aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Spesifikasi Aplikasi
>Aplikasi dapat menerima lokasi Jerry yang didapat dari Spike melalui alamat http://167.205.32.46/pbd/api/track
>Aplikasi dapat menampilkan navigasi kepada pengguna untuk menemukan Jerry dengan Google Map
>Aplikasi dapat melakukan scanning terhadap QR code untuk melaporkan penangkapan Jerry pada Spike
>Aplikasi dapat menampilkan kompas menggunakan geomagnetic sensor pada android
>Aplikasi dapat meminta ulang lokasi Jerry kepada Spike saat Jerry telah berpindah tempat

## Spesifikasi Endpoint
>Alamat Spike (server yang digunakan) berada di 167.205.32.46/pbd
>API untuk meminta lokasi Jerry menggunakan request /api/track?
>API untuk mengirimkan laporan penangkapan Jerry menggunakan request /api/catch

## License

MIT License
