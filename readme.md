# Tugas 1 Android

## Latar Belakang
Tom merupakan kucing yang paling hebat di ITB. Tom sekarang memiliki tugas untuk menangkap 
tikus jahat bernama Jerry. Untuk menangkap Jerry Tom memerlukan bantuan mendapatkan lokasi Jerry dan melaporkannya kepada Spike.

## Spesifikasi Aplikasi
Aplikasi ini berguna untuk membantu Tom mendapatkan lokasi dengan cara meminta lokasi Jerry kepada Spike. 
Aplikasi ini kemudian menunjukkan lokasi tersebut pada peta. 
Untuk melaporkan kepada Spike bahwa Jerry telah tertangkap aplikasi ini melakukan scanning terhadap QR Code 
kemudian ia mengirimkan token hasil scanning tersebut ke Spike.

## Spesifikasi Endpoint
Spike merupakan endpoint dengan alamat : http://167.205.32.46/pbd
Untuk mendapatkan posisi Jerry saat ini digunakan api track TRACK : [GET] /api/track?nim=13512077
Untuk mengirimkan token digunaan api catch CATCH : [POST] /api/catch

## License

MIT License
