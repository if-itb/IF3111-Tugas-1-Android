# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang palingpopuler di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatuhari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan danbersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketikaberkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada didalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!
Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.
Pada aplikasi ini, user dapat yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi ini juga dapat melaporkan penangkapan Jerry kepada Spike.

## Spesifikasi Aplikasi
1. GPS Tracking
Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry. User dapat melihat posisi Jerry sebagai icon Jerry pada peta. User juga dapat melihat sisa waktu untuk menangkap Jerry sebelum Jerry pindah

2. Geomagnetic Sensor
Aplikasi dapat menggambarkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar menggunkan Geomagnetic Sensor pada device yang digunakan.

3. QR-Code Scanner
Setelah user mendatangi lokasi Jerry ssesuai yang ditampilka di peta. User akan menemukan QRCode yang dapat di-scan dengan cara menekan tombol "Catch Jerry". Jika QRCode sesuai, maka Jerry berhasil ditangkap.

## Spesifikasi Endpoint
1. TRACK : [GET] /api/track?nim=13512020
	Pemanggilan: http://167.205.32.46/pbd/api/track?nim=13512020
	Kembalian:
	{
	 “lat”: “-6.890323” // Koordinat latitude tujuan
	 “long”: “107.610381” // Koordinat longitude tujuan
	 “valid_until”:1425833999 // Date dalam format UTC+7 kapan token hangus
	}

2. CATCH : [POST] /api/catch
	Header request:
	Content-type: application/json
	Parameter request:
	token := “CONTOHTOKEN”
	nim := “13512020”
	Body request:
	{"nim": "13512020", "token": "secret_token"}
	Response status:
	- status: 200 OK (Apabila pengumpulan sukses dengan token sesuai)
	- status: 400 MISSING PARAMETER (Apabila parameter yang dikirim tidak lengkap)
	- status: 403 FORBIDDEN (Apabila terdapat parameter yang salah)

## License

MIT License

## Source
Map & Current Location = http://wptrafficanalyzer.in/blog/showing-current-location-in-google-maps-using-api-v2-with-supportmapfragment/
Compass = http://www.javacodegeeks.com/2013/09/android-compass-code-example.html
Images 	= 	http://img2.wikia.nocookie.net/__cb20140714021527/pachirapong/images/c/cd/Jerry-tom-and-jerry.png
			http://www.logoeps.com/wp-content/uploads/2013/03/jerry-vector-logo.png
			http://www.kids-coloringpages.com/image/tom_and_jerry.png
