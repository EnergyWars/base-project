package com.wafflehq.lib.modules.permissions

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ModulePermissionRequesterTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun specWithoutPermissionsReportsGrantedImmediately() {
        val spec = ModulePermissionSpec(emptyList(), 1, 2, 3)
        var result: Pair<ModulePermissionSpec, Boolean>? = null
        rule.setContent {
            val request = rememberModulePermissionRequester { s, granted -> result = s to granted }
            Button(onClick = { request(spec) }) { Text("Request") }
        }
        rule.onNodeWithText("Request").performClick()
        assertEquals(spec to true, result)
    }
}
