package com.example.data.model

enum class UserRole(
    val id: String,
    val faTitle: String,
    val faSubtitle: String,
    val iconEmoji: String
) {
    STUDENT(
        id = "STUDENT",
        faTitle = "دانشجو",
        faSubtitle = "مشاهده کلاس‌ها و تکالیف",
        iconEmoji = "🎓"
    ),
    PROFESSOR(
        id = "PROFESSOR",
        faTitle = "استاد / مدرس",
        faSubtitle = "یافتن کلاس، سرفصل تدریس و نماینده",
        iconEmoji = "👨‍🏫"
    );

    companion object {
        fun fromId(id: String): UserRole {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: STUDENT
        }
    }
}
