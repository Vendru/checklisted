package com.checklisted.app.domain.model

/** Which theme the app uses, persisted by [name]. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        val Default = SYSTEM

        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: Default
    }
}
