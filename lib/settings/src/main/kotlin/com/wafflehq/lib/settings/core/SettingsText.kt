package com.wafflehq.lib.settings.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

sealed interface SettingsText {
    data class Label(@param:StringRes val labelRes: Int) : SettingsText
    data class Formatted(@param:StringRes val labelRes: Int, val formatArgs: List<Any>) : SettingsText
    data class Literal(val text: String) : SettingsText
}

@Composable
fun settingsText(text: SettingsText): String = when (text) {
    is SettingsText.Literal -> text.text
    is SettingsText.Label -> stringResource(text.labelRes)
    is SettingsText.Formatted -> {
        val context = LocalContext.current
        stringResource(text.labelRes, *text.formatArgs.map { context.resolveArgument(it) }.toTypedArray())
    }
}

fun Context.resolveSettingsText(text: SettingsText): String = when (text) {
    is SettingsText.Literal -> text.text
    is SettingsText.Label -> getString(text.labelRes)
    is SettingsText.Formatted -> getString(text.labelRes, *text.formatArgs.map { resolveArgument(it) }.toTypedArray())
}

private fun Context.resolveArgument(arg: Any): Any =
    if (arg is SettingsText) resolveSettingsText(arg) else arg
