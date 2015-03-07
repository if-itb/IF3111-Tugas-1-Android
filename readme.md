# Tugas 1 Android

Tugas ini membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.

Aplikasi inilah yang bertugas untuk melakukan penangkapan Jerry dan melakukan komunikasi dengan Spike.

## Spesifikasi Aplikasi
1. GPS Tracking
Aplikasi dapat menerima informasi posisi Jerry Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry pada Goolge Map.

2. Geomagnetic Sensor
Aplikasi memanfaatkan Geomagnetic Sensor untuk menggambarkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar.

3. QR-Code Scanner
Setelah mendapatkan posisi Jerry, aplikasi dapat menangkap Jerry dengan melakukan scanning token dari QR-code yang disediakan dan dikirimkan ke server.

## Spesifikasi Endpoint
1. Aplikasi dapat menerima posisi Jerry menggunakan API track

2. Jerry dapat berpindah secara berkala

3. Setelah mencapai posisi Jerry. Aplikasi akan membaca QR-Code, yang akan mendapatkan sebuah plain text yang merupakan Token dan harus dikirim pada saat menangkap Jerry dan melaporkannya ke Spike.
Prosedur penangkapan dilakukan dengan pemanggilan api catch pada endpoint dengan format request yang ditentukan.

4. Token yang dilaporkan ke Spike merupakan token yang benar dan dalam format yang benar. Spike akan memberikan respon apakah penangkapan dilakukan dengan cara yang tepat atau tidak.

## License
"Tom and Jerry" are TM and copyright Turner Entertainment (where the rights stand today via Warner Bros).

Google Map License

MIT License
