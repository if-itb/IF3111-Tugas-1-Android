# Tugas 1 Android

Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang

Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala! 
Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu.

## Spesifikasi Aplikasi

###Functional Requirements
1. GPS tracking: fitur yang dapat menampilkan posisi Jerry
2. Geomagnetic sensor: fitur kompas untuk navigasi Tom
3. QR-Code Scanner: fitur untuk mengonfirmasi penangkapan Jerry oleh Tom

###Non Functional Requirements
1. Aplikasi menggunakan daya baterai sesedikit mungkin
2. Aplikasi harus user friendly, user tidak perlu menekan tombol refresh untuk mendapatkan info terbaru terkait posisi Jerry
3. Source code diusahakan agar terstruktur dengan baik
4. Minimal Android API 9 (GingerBread)

###Spesifikasi Tampilan
Aplikasi terdiri dari 3 tampilan, yaitu tampilan awal yang memiliki 2 tombol menu, tampilan peta (map) untuk mencari lokasi Jerry, dan tampilan QR-code scanner.

## Spesifikasi Endpoint
###Alamat Server: 167.205.32.46/pbd
###Track : [GET]/api/track?nim=13512080
contoh kembalian:<return>
{<return>
"lat" : "-6.890323"			//latitude Jerry<return>
"long" : "107.610381"		//longitude Jerry<return>
"valid_until" : 1425833999	//date dalam format UTC+7, deadline token (hangus)<return>
}<return>

###Catch : [POST]/api/catch
Parameter request:<return>
token := "secret_token"<return>
nim := "13512080"<return>

body request untuk Catch: {"nim":"13512080", "token":"secret_token"}<return>

Response Status:<return>
200 = OK (token yang dikirimkan benar)<return>
403 = FORBIDDEN (token yang dikirimkan salah)<return>
400 = MISSING PARAMETER (parameter tidak lengkap)<return>

## License

MIT License