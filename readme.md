# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang

Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala!

Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.

## Spesifikasi Aplikasi

1. GPS Tracking
	GPS ini untuk menemukan posisi Jerry. Posisi Jerry ditandai dengan icon Jerry pada Map yang ada di layar. Waktu persembunyian Jerry tertera di atas layar gadget. Sedangkan di samping kiri terdapat posisi GPS Jerry (latitude dan longitude).
2. Geomagnetic Sensor
	Fitur ini menampilkan arah mata angin. Posisinya ada di pojok kanan atas
3. QR-Code Scanner
	Fitur ini untuk menangkap Jerry dengan cara melakukan scanning token dari QR-Code yang disediakan dan dikirimkan ke server.
## Spesifikasi Endpoint
	Alamat Server: 167.205.32.46/pbd
	
	TRACK : [GET] /api/track?nim=<NIM_ANDA> 
	contoh pemanggilan:
	/api/track?nim=13512000 (167.205.32.46/pbd/api/track?nim=13512000)

	contoh kembalian: 
	{
    	“lat”: “-6.890323”                // Koordinat latitude tujuan
    	“long”: “107.610381”          // Koordinat longitude tujuan
    	“valid_until”:1425833999   // Date dalam format UTC+7 kapan token hangus
	}
	
	CATCH : [POST] /api/catch 
	Header request:
	Content-type: application/json

	Parameter request:
	token := “CONTOHTOKEN”
	nim := “NIM_ANDA”

	Contoh body request:
	{"nim": "13512000", "token": "secret_token"}

	Response status: 
	- status: 200 OK (Apabila pengumpulan sukses dengan token sesuai)
	- status: 400 MISSING PARAMETER (Apabila parameter yang dikirim tidak lengkap)
	- status: 403 FORBIDDEN (Apabila terdapat parameter yang salah) 

## License

MIT License