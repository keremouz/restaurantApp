# RestaurantApp - ProGuard / R8 rules

# Release build'de R8/ProGuard ile
# - unused code removal
# - optimization
# - obfuscation
# uygulanacaktır.
#
# Bu projede geniş keep kuralları bilinçli olarak eklenmemiştir.
# Gerekli olursa yalnızca sorun çıkaran sınıflar hedefli olarak korunacaktır.

##################################################
# BİLİNÇLİ OLARAK EKLENMEYEN GENİŞ KURALLAR
##################################################
# -keep class com.google.** { *; }
# -keep class retrofit2.** { *; }
# -keep class okhttp3.** { *; }
# -keep class kotlinx.serialization.** { *; }
# -keep class com.example.restaurantapp.** { *; }

##################################################
# KOTLINX SERIALIZATION
##################################################
# DTO sınıfları @Serializable kullandığı için geniş kotlinx kuralı yerine
# yalnızca API response/request modelleri hedefli korunur.
-keep class com.example.restaurantapp.data.remote.dto.** { *; }

# Serialization metadata ve generic imzalar korunur.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses, EnclosingMethod

##################################################
# FIREBASE AUTH / FIRESTORE
##################################################
# Firestore document -> data class mapping sırasında model field isimlerinin
# obfuscate edilmemesi için Firebase model sınıfları hedefli korunur.

-keep class com.example.restaurantapp.data.firebase.FavoriteRestaurant { *; }
-keep class com.example.restaurantapp.data.firebase.UserComment { *; }
-keep class com.example.restaurantapp.data.firebase.CommentRatings { *; }
-keep class com.example.restaurantapp.data.firebase.UserProfile { *; }

-keepclassmembers class com.example.restaurantapp.data.firebase.FavoriteRestaurant { *; }
-keepclassmembers class com.example.restaurantapp.data.firebase.UserComment { *; }
-keepclassmembers class com.example.restaurantapp.data.firebase.CommentRatings { *; }
-keepclassmembers class com.example.restaurantapp.data.firebase.UserProfile { *; }

-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

##################################################
# GOOGLE MAPS / MAPS COMPOSE
##################################################
# Şu an geniş keep kuralı eklenmedi.
# Maps tarafında sorun çıkarsa yalnızca ilgili sınıf hedefli korunacaktır.
-dontwarn com.google.maps.android.**
-dontwarn com.google.android.gms.maps.**

##################################################
# RETROFIT / OKHTTP
##################################################
# Retrofit interface imzaları ve annotation bilgileri korunur.
# Geniş retrofit/okhttp keep kuralı bilinçli olarak eklenmedi.

-keep interface com.example.restaurantapp.data.remote.api.** { *; }

-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

##################################################
# DOMAIN MODELS
##################################################
# Navigation / savedState / Parcelable veya mapping tarafında sorun yaşanmaması için
# domain modeller hedefli korunur.
-keep class com.example.restaurantapp.domain.model.** { *; }