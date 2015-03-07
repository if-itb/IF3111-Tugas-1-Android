# Tugas 1 Android - Diah Fauziah / 13512049

Dalam tugas ini, kami membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi ini juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
	Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. 	Bersegeralah karena Jerry akan berpindah posisi secara berkala!
	
	Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.
	
	Dalam tugas ini, aplikasi android yang dibuat akan membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini akan digunakan untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi ini juga dapat melaporkan penangkapan Jerry kepada Spike.


## Spesifikasi Aplikasi
	Functional Requirement
		1. GPS Tracking
			Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry. Aplikasi akan menampilkan map yang menunjukkan lokasi Jerry berada.
		2. Geomagnetic Sensor
			Pada sudut kanan atas map terdapat kompas yang memanfaatkan Geomagnetic Sensor untuk menggambarkan arah mata angin yang menunjukkan arah utara dan selatan.
		3. QR-Code Scanner
			Setelah mendapatkan posisi Jerry, pengguna dapat menangkap Jerry dengan menekan tombol "tangkap Jerry". Aplikasi ini akan melakukan scanning token dari QR-code yang disediakan dan dikirimkan ke server. Jika QR code dan token yang ditampilkan benar, maka akan muncul dilayar pemberitahuan bahwa token yang diambil benar dan proses penangkapan dilakukan dengan cara yang tepat. Jika token yang diambil kurang/salah, maka server akan mengirimkan kode/status dan menampilkan pesan kesalahan dilayar.

	Non Functional Requirement
		- 	Penggunaan daya baterai sesedikit mungkin karena daya baterai yang terbatas. 
		- 	Aplikasi harus user friendly, artinya user tidak perlu selalu menekan tombol refresh untuk mendapatkan informasi terbaru dari Spike
		- 	Source code diusahakan agar terstruktur dengan baik
		- 	Minimal Android API 9 (GingerBread).
	
	Spesifikasi Tampilan
		Awalnya aplikasi akan mencari lokasi Jerry dan menampilkan peta lokasi Jerry berada. Pada sudut kanan atas akan muncul kompas yang menunjukkan arah utara dan selatan. Dibagian bawah akan ada tombol "tangkap Jerry" untuk menangkap Jerry ditempat tersebut. Untuk menangkap Jerry dan memberitahu Spike apakah cara penangkapan sudah tepat/tidak, Tom (pengguna) akan melakukan scanning QR code dengan menyentuh tombol "tangkap Jerry". Setelah dilakukan scanning QR code di layar akan muncul respon dari Spike (server) yang memberitahu apakah proses pengambilan/penangkapan yang dilakukan sudah benar/terdapat kesalahan format dalam penangkapan. 

## Spesifikasi Endpoint
		- 	Alamat Server: 167.205.32.46/pbd
		- 	Alamat yang digunakan untuk Tracking lokasi Jerry [GET] yaitu http://167.205.32.46/pbd/api/track?nim=13512049		
		-	Alamat yang digunakan untuk Catch dan mengirimkan json yang didapatkan dari hasil scanning QR code [POST] yaitu http://167.205.32.46/pbd/api/catch 
			dengan format pengiriman json :
			{"nim":"13512049", "token":"secret_token"} dimana secret_token merupakan hasil scanning QR code.
			Setelah json dikirim ke server, server akan memberikan response status :
			status == 200 OK, apabila pengumpulan sukses dengan token sesuai
			status == 400 MISSING PARAMETER, apabila parameter yang dikirimkan tidak lengkap
			status == 403 FORBIDDEN, apabila terdapat parameter yang salah


## License
	"Tom and Jerry" are TM and copyright Turner Entertainment (where the rights stand today via Warner Bros). All rights reserved. Any reproduction, duplication or distribution of these materials in any form is expressly prohibited.
	
MIT License