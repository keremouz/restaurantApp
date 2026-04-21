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
# Şu an ekstra kural eklenmedi.

##################################################
# FIREBASE AUTH / FIRESTORE
##################################################
# Şu an ekstra kural eklenmedi.

##################################################
# GOOGLE MAPS / MAPS COMPOSE
##################################################
# Şu an ekstra kural eklenmedi.

##################################################
# RETROFIT / OKHTTP
##################################################
# Şu an ekstra kural eklenmedi.