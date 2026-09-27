package com.wafflehq.lib.settings.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wafflehq.lib.settings.onboarding.OnboardingPage
import com.wafflehq.lib.settings.onboarding.OnboardingPageActions

@Composable
fun OnboardingFlow(
    pages: List<OnboardingPage>,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnFinish by rememberUpdatedState(onFinish)
    var finished by remember { mutableStateOf(false) }

    if (pages.isEmpty()) {
        LaunchedEffect(Unit) {
            if (!finished) {
                finished = true
                currentOnFinish()
            }
        }
        return
    }

    var storedIndex by rememberSaveable { mutableIntStateOf(0) }
    val index = storedIndex.coerceIn(0, pages.lastIndex)
    val count = pages.size
    val lastIndex = pages.lastIndex

    val actions = remember(index, count) {
        object : OnboardingPageActions {
            override val pageIndex: Int = index
            override val pageCount: Int = count

            override fun next() {
                if (index >= lastIndex) finish() else storedIndex = index + 1
            }

            override fun back() {
                if (index > 0) storedIndex = index - 1
            }

            override fun finish() {
                if (finished) return
                finished = true
                currentOnFinish()
            }
        }
    }

    OnboardingScaffold(pageIndex = index, pageCount = count, modifier = modifier) {
        key(pages[index].key) { pages[index].content(actions) }
    }
}
