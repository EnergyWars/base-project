package com.wafflehq.lib.settings.onboarding

import androidx.compose.runtime.Composable

data class OnboardingPage(
    val key: String,
    val content: @Composable (OnboardingPageActions) -> Unit,
)

interface OnboardingPageActions {
    val pageIndex: Int
    val pageCount: Int
    val isFirstPage: Boolean get() = pageIndex == 0
    val isLastPage: Boolean get() = pageIndex == pageCount - 1

    fun next()
    fun back()
    fun finish()
}
