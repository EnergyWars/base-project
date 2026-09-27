package com.wafflehq.lib.settings.colors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable

@Serializable
data class ColorThemeData(
    val overridesLight: Map<String, String> = emptyMap(),
    val overridesDark: Map<String, String> = emptyMap(),
    val baseRamps: Map<String, Int> = emptyMap()
) {
    val isEmpty: Boolean get() = overridesLight.isEmpty() && overridesDark.isEmpty() && baseRamps.isEmpty()

    fun overrides(isDark: Boolean): Map<ColorTokenId, ColorValue> =
        (if (isDark) overridesDark else overridesLight).entries
            .mapNotNull { (rawId, raw) -> ColorValueCodec.decode(raw)?.let { ColorTokenId(rawId) to it } }
            .toMap()

    fun baseRampOverrides(): Map<ColorRamp, Color> =
        baseRamps.entries
            .mapNotNull { (name, argb) -> ColorRamp.entries.firstOrNull { it.name == name }?.let { it to Color(argb) } }
            .toMap()

    fun withOverride(id: ColorTokenId, isDark: Boolean, value: ColorValue?): ColorThemeData {
        val current = if (isDark) overridesDark else overridesLight
        val updated = if (value == null) current - id.value else current + (id.value to ColorValueCodec.encode(value))
        return if (isDark) copy(overridesDark = updated) else copy(overridesLight = updated)
    }

    fun withoutTokens(ids: Collection<ColorTokenId>, isDark: Boolean): ColorThemeData {
        val names = ids.map { it.value }.toSet()
        return if (isDark) copy(overridesDark = overridesDark - names) else copy(overridesLight = overridesLight - names)
    }

    fun withBaseRamp(ramp: ColorRamp, color: Color?): ColorThemeData =
        copy(baseRamps = if (color == null) baseRamps - ramp.name else baseRamps + (ramp.name to color.toArgb()))
}

@Serializable
data class StoredColorTheme(
    val id: String,
    val name: String,
    val data: ColorThemeData = ColorThemeData()
) {
    val info: ColorThemeInfo get() = ColorThemeInfo(id, name)
}

data class ColorThemeInfo(val id: String, val name: String)

@Serializable
data class ColorThemeLibrary(
    val activeId: String? = null,
    val themes: List<StoredColorTheme> = emptyList()
) {
    val active: StoredColorTheme? get() = activeId?.let { id -> themes.firstOrNull { it.id == id } }

    fun theme(id: String): StoredColorTheme? = themes.firstOrNull { it.id == id }

    fun updateActive(transform: (ColorThemeData) -> ColorThemeData): ColorThemeLibrary {
        val current = active ?: return this
        return copy(themes = themes.map { if (it.id == current.id) it.copy(data = transform(it.data)) else it })
    }
}

@Serializable
data class ColorSafetyPending(
    val snapshot: ColorThemeLibrary,
    val deadlineMillis: Long
)

object ColorSafety {
    const val CONFIRM_WINDOW_MILLIS = 15_000L
}

fun uniqueThemeName(desired: String, existing: Collection<String>): String {
    val base = desired.trim()
    if (base !in existing) return base
    var counter = 2
    while ("$base ($counter)" in existing) counter++
    return "$base ($counter)"
}
