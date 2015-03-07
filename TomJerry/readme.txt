Copyright (C) 2015 CatchJerry
Bagaskara Pramudita - 13512073

Ini merupakan sebuah aplikasi untuk melacak lokasi Jerry
Lokasi Jerry akan didapat dari 167.205.32.46/pbd/api/track?nim=13512073
Lokasi akan di GET pada saat aplikasi pertama kali menjalankan aplikasi
Pada 167.205.32.46/pbd/api/track?nim=13512073 terdapat data latitude dan longitude dan waktu jerry akan berpindah
Saat lokasi jerry sudah didapat, ada marker yang menunjukkan lokasi jerry yang terletak di tengah layar
Waktu jerry berpindah akan dicetak di kiri atas dan ada CountDownTimer untuk mengambil data baru lokasi jerry setelah jerry berpindahtempat secara otomatis
Di lokasi jerry akan ada QR Code yang bisa di scan
Tekan tombol Catch untuk membuka QR Code scanner
Token yang didapat dari QR Code akan dikirim ke 167.205.32.46/pbd/api/catch dengan parameter Nim dan Token yang diambil dari QR
Akan ada toast untuk memeritahu jawaban dari server