package ru.mirus.rostovondon

data class AppItem(
    val name: String,
    val packageName: String? = null, // если null → значит нет приложения
    val url: String? = null          // откроем через браузер
)

enum class ServiceType {
    ONE_APP, TWO_APPS, THREE_APPS, ON_MAP
}

data class Service(
    val iconRes: Int,
    val name: String,
    val description: String,
    val type: ServiceType = ServiceType.ONE_APP,
    val apps: List<AppItem> = emptyList()
)
