# Tugas 1 Android - Catch Jerry (Jerry Tracker)
Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Latar Belakang
Di kawasan ITB, terdapat banyak kucing yang berkeliaran. Salah satu kucing yang paling populer di ITB adalah Tom. Tom adalah kucing pemburu tikus yang paling handal di ITB. Suatu hari, ia bertemu dengan Jerry, seekor tikus yang sangat lihai dalam mencari makanan dan bersembunyi. Karena kelihaian Jerry, Tom tidak mampu mengejar dan menangkap Jerry ketika berkeliaran. Satu-satunya kesempatan untuk menangkap Jerry adalah ketika Jerry masih ada di dalam persembunyian. Bersegeralah karena Jerry akan berpindah posisi secara berkala! Tom mempunyai teman seekor anjing pelacak yang bernama Spike. Dengan bantuan Spike, Tom dapat melakukan tracking terhadap tempat persembunyian Jerry. Selain itu, Tom juga perlu melapor ke Spike untuk menghindari kaburnya Jerry. Jerry seringkali berpindah tempat persembunyian dalam jangka waktu tertentu. Spike dapat memberitahu posisi persembunyian Jerry saat itu dan berapa lama Jerry bersembunyi di tempat itu. Dalam tugas ini, peserta diharapkan dapat membuat aplikasi android yang membantu Tom untuk mencari dan menangkap Jerry. Aplikasi android ini dibuat untuk berkomunikasi dengan Spike untuk mengetahui lokasi dan lama persembunyian Jerry di lokasi tersebut. Aplikasi itu juga dapat melaporkan penangkapan Jerry kepada Spike.

## Spesifikasi Aplikasi
## Functional Requirement
<ol type = "1">
<li><b>GPS Tracking</b><br\>Posisi yang diberikan Spike berbentuk latitude dan longitude. Lama persembunyian Jerry juga diberikan dalam waktu UTC+7. Setelah waktu persembunyian habis, posisi Jerry akan berubah. Aplikasi dapat menampilkan posisi Jerry. Untuk jenis tampilan dibebaskan kepada peserta (misalnya map, atau penunjuk arah, atau indikator sederhana lainnya), silahkan dibuat se-kreatif mungkin.</li>
<li><b>Geomagnetic Sensor</b><br\>Peserta diminta memanfaatkan Geomagnetic Sensor untuk menggambarkan arah mata angin yang menunjukkan arah utara dan selatan dengan benar.</li>
<li><b>QR-Code Scanner</b><br\>Setelah mendapatkan posisi Jerry, peserta harus menangkap Jerry dengan melakukan scanning token dari QR-code yang disediakan dan dikirimkan ke server. Peserta diharapkan mencoba menggunakan library yang sudah tersedia di internet. (contoh : http://examples.javacodegeeks.com/android/android-barcode-and-qr-scanner-example/) </li>
</ol>

## Non Functional Requirement

<ul type ="list-style-type:disc">

<li>Perangkat smartphone saat ini memiliki fitur yang sangat lengkap. Namun salah satu tantangan dalam menggunakan fitur-fitur tersebut adalah penggunaan daya baterai sesedikit mungkin karena daya baterai yang terbatas. Oleh karena itu pada tugas kali ini, peserta diharapkan memikirkan strategi yang baik agar aplikasi membutuhkan daya sesedikit mungkin. </li>
<li>Aplikasi harus user friendly, artinya user tidak perlu selalu menekan tombol refresh untuk mendapatkan informasi terbaru dari Spike</li>
<li>Source code diusahakan agar terstruktur dengan baik (Silahkan membaca guidelines standar yang dirilis di https://source.android.com/source/code-style.html) </li>
<li>Minimal Android API 9 (GingerBread).</li> </ul>

## Spesifikasi Tampilan

Anda dibebaskan dalam berkreasi mendesain tampilan selama fitur-fitur pada spesifikasi aplikasi tercapai dan aplikasi dapat digunakan sesuai dengan alur pengujian yang diberikan.

## Spesifikasi Endpoint

<b>Alamat Server: 167.205.32.46/pbd </b>

## Track
<b>Request</b>: <EndPoint>/api/track?nim=13512018 <br\>
<b>Response</b>: {"lat":"-6.890323","long":"107.610381","valid_until":1425833999}

## Catch
<b>Header request</b> <br\><br\>
Content-type: application/json <br\>
<b>Parameter request </b>
<ul type = "list-style-type:disc"> 
<li> nim: 13512018</li>
<li> token: <secret_token> (hasil QR Code Scanning)</li></ul> 

<b> Response Status </b>
<ul type ="list-style-type:disc">
<li> status : <b>200 -> OK</b> (pengumpulan sukses, secret_token sesuai) </li>
<li> status : <b>400 -> MISSING PARAMETER</b> (paramter yang dikirim tidak lengkap yaitu 2 parameter) </li>
<li> status : <b>403 -> FORBIDDEN </b>(terdapat parameter yang salah (nim atau secret token) </li> </ul>

## License
<ul type ="list-style-type:disc">
<li>MIT License </li>
<li>"Tom and Jerry" are TM and copyright Turner Entertainment (where the rights stand today via Warner Bros). All rights reserved. Any reproduction, duplication or distribution of these materials in any form is expressly prohibited.</li>

## HOW TO PLAY "Catch Jerry"
## MAIN SCREEN
Klik icon "Play" dan bersialah untuk melacak dan menangkap Jerry!

## MAP SCREEN
Anda akan langsung diarahkan ke tempat dimana Jerry berada. Posisi Jerry ditandai oleh tanda berbentuk balon warna merah pada peta. Anda dapat melihat posisi Jerry pada pojok kiri bawah (berbentuk Latitude dan Longitude). Anda bisa memencet icon berbentuk lup untuk memfokuskan diri Anda pada posisi Jerry pada Peta. Anda juga dapat melihat dimana posisi anda dengan mengklik tombol "My Position" pada bagian kiri bawah layar. Anda ditandai oleh lingkaran berwarna biru pada Peta. <br\
Selain itu, pada bagian kanan bawah navigasi, anda dapat melihat ada kompas beserta switchnya. Anda dapat menggunakan kompas tersebut bila dibutuhkan. Lalu di bawah kompas, ada icon berbentuk Lup dan QRCode yang akan mengarahkan anda pada menu QR Code.

## QRCode Scanning Screen
Anda akan dapat langsung memindai QR Code lewat kamera. Setelah itu, pesan secret_token akan muncul dari hasil QR Code yang anda pindai tersebut. Lalu anda dapat langsung mengirimkan secret_token tersebut ke endpoint dan langsung mendapatkan response dari endpoint.
