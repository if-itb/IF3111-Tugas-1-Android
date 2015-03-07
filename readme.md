# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
	Tom adalah kucing yang gemar makan. Ia selalu menimbun makanan yang telah ia dapatkan di suatu daerah di ITB untuk kemudia hari ia makan.
	Namun, secara tiba-tiba, makanan yang ia timbun itu hilang. Menurutnya, penyebabnya hanya dua, kalau tidak karena ketauan KPK gara-gara menimbun makanan
	atau ketauan Jerry yang melahap habis semua timbunan makanannya. Untuk itu, Tom datang ke HMIF untuk meminta tim di sana membuat aplikasi yang dapat
	melacak Jerry dan melaporkannya ke Spike, Anjing ITB yang belum diracuni oleh entah siapa
## Spesifikasi Aplikasi
	Aplikasi JerryCatcher memanfaatkan GoogleMaps dalam melacak Jerry. Aplikasi akan meminta ke server untuk mengembalikan posisi Jerry sehingga dapat 
	terlacak. Ketika sudah berada di tempat Jerry berada, aplikasi dapat melakukan scanning terhadap QR code yang ada di sana. Bacaan dari QR code akan
	dikirimkan ke server
## Spesifikasi Endpoint
	endpoint "catch" berupa JSON yang dikembalikan ke aplikasi.
## License

MIT License
