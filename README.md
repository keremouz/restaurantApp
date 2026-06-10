# Nerede Yiyelim? - Yapay Zekâ Destekli Restoran Öneri Uygulaması

Bu proje, kullanıcıların bulundukları konuma göre restoranları harita üzerinde görüntüleyebildiği, restoran detaylarına ulaşabildiği, favorilerine ekleyebildiği, yorum ve puanlama yapabildiği Android tabanlı bir mobil uygulamadır.

Uygulamada kullanıcıya daha uygun restoran önerileri sunmak amacıyla yapay zekâ destekli öneri yapısı kullanılmıştır. Öneri sistemi; kullanıcının konumu, restoran puanları, kategori bilgileri, yorumlar ve restoran uzaklığı gibi verileri dikkate alarak çalışmaktadır.

## Projenin Amacı

Bu projenin amacı, kullanıcıların restoran seçme sürecini kolaylaştırmak ve konuma dayalı daha uygun restoran önerileri sunmaktır. Kullanıcılar uygulama üzerinden yakınındaki restoranları görüntüleyebilir, restoranları inceleyebilir, favorilerine ekleyebilir ve yorum/puanlama işlemleri yapabilir.

## Kullanılan Teknolojiler

- Kotlin
- Android Studio
- Jetpack Compose
- MVVM Mimarisi
- Clean Architecture
- Retrofit
- OkHttp
- Firebase Authentication
- Firebase Firestore
- Google Maps API
- Google Places API
- Gemini AI
- GitHub
- ProGuard / R8

## Temel Özellikler

- Kullanıcı kayıt ve giriş işlemleri
- Harita üzerinde restoranları görüntüleme
- Konuma göre restoran listeleme
- Restoran adına ve kategoriye göre filtreleme
- Restoran detay sayfası
- Favori restoran ekleme ve çıkarma
- Kullanıcı yorumları ve puanlama sistemi
- Yapay zekâ destekli restoran önerileri
- İnternet bağlantısı kontrolü
- Release build için ProGuard / R8 optimizasyonu

## Proje Mimarisi

Proje MVVM ve Clean Architecture yapısına uygun şekilde geliştirilmiştir.

Genel veri akışı şu şekildedir:

Jetpack Compose Screen  
→ ViewModel  
→ Repository  
→ Firebase / Google Places API / Gemini AI

Presentation katmanında kullanıcı arayüzleri ve ViewModel yapıları bulunmaktadır. Domain katmanında uygulama modelleri ve iş kuralları yer almaktadır. Data katmanında ise Firebase, API servisleri, DTO modelleri ve repository implementasyonları bulunmaktadır.

## Kurulum ve Çalıştırma

1. Proje Android Studio ile açılır.
2. Gradle Sync işlemi tamamlanır.
3. Firebase bağlantısı için `google-services.json` dosyası `app/` klasörü içinde bulunmalıdır.
4. Google Maps, Google Places ve Gemini AI için gerekli API key bilgileri eklenmelidir.
5. Uygulama emulator veya gerçek Android cihaz üzerinde çalıştırılabilir.

## API Key Bilgileri

Güvenlik nedeniyle API key bilgilerinin doğrudan kod içinde paylaşılmaması önerilir. Projeyi çalıştırmak için gerekli key değerleri `local.properties` dosyası üzerinden tanımlanabilir.

Örnek kullanım:

```properties
MAPS_API_KEY=BURAYA_GOOGLE_MAPS_API_KEY_YAZILACAK
PLACES_API_KEY=BURAYA_GOOGLE_PLACES_API_KEY_YAZILACAK
GEMINI_API_KEY=BURAYA_GEMINI_API_KEY_YAZILACAK
GOOGLE_WEB_CLIENT_ID=BURAYA_GOOGLE_WEB_CLIENT_ID_YAZILACAK
