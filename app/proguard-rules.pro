# Сохраняем классы и методы, нужные для Яндекс.Карт
-keep class com.yandex.** { *; }
-keepclassmembers class com.yandex.** { *; }

# Для VK ID (если ломается аутентификация)
-keep class com.vk.id.** { *; }
-keepclassmembers class com.vk.id.** { *; }

# Firebase обычно работает без проблем, но можно добавить:
-keep class com.google.firebase.** { *; }
-keepclassmembers class com.google.firebase.** { *; }

# Оставляем аннотации (если используешь)
-keepattributes *Annotation*

# Обязательно сохраняем классы, использующие reflection
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
