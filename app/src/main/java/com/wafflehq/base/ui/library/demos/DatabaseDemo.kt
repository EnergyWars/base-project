package com.wafflehq.base.ui.library.demos

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.wafflehq.base.R
import com.wafflehq.lib.database.DatabaseEncryptionDefaults
import com.wafflehq.lib.database.crypto.AesGcmCipher
import com.wafflehq.lib.database.crypto.BackupPasswordStrength
import com.wafflehq.lib.database.crypto.KeystoreKeyWrapper
import com.wafflehq.lib.database.crypto.PasswordKeyWrapper
import com.wafflehq.lib.database.crypto.PasswordStrength
import com.wafflehq.lib.database.crypto.WrappedDek
import com.wafflehq.lib.database.open.DatabaseOpenPlanner
import com.wafflehq.lib.database.open.EncryptedDatabaseProbe
import com.wafflehq.lib.database.state.ConversionState
import com.wafflehq.lib.database.state.EncryptionStateStore
import com.wafflehq.lib.uicore.button.AppButton
import com.wafflehq.lib.uicore.button.AppButtonRole
import com.wafflehq.lib.uicore.button.AppButtonVariant
import com.wafflehq.lib.uicore.components.AppHorizontalDivider
import com.wafflehq.lib.uicore.components.AppTextField
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

internal class InMemoryKeyWrapper : KeystoreKeyWrapper {

    private val key = ByteArray(KEY_BYTES).also { SecureRandom().nextBytes(it) }

    override fun wrap(dek: ByteArray): WrappedDek {
        val encrypted = AesGcmCipher.encrypt(key, dek)
        return WrappedDek(iv = encrypted.iv, ciphertext = encrypted.ciphertext)
    }

    override fun unwrap(wrapped: WrappedDek): ByteArray = AesGcmCipher.decrypt(key, wrapped.iv, wrapped.ciphertext)

    override fun deleteKey() = Unit

    private companion object {
        const val KEY_BYTES = 32
    }
}

internal data class OpenPlanResult(
    val fileName: String,
    val path: String,
    val encrypted: Boolean,
    val conversionState: ConversionState,
    val hasOpenHelperFactory: Boolean,
    val keyUnavailable: Boolean,
)

internal data class WrapResult(
    val roundTripOk: Boolean,
    val wrongPasswordRejected: Boolean,
    val iterations: Int,
    val aesRoundTripOk: Boolean,
)

internal object DatabaseDemoLogic {

    const val DATABASE_FILE = "libex_demo.db"
    const val RECOVERY_FILE = "libex_demo_recovery.db"
    const val STATE_PREFS = "libex_encryption_state"
    const val DEFAULT_PASSWORD = "Waffle-Demo-2026!"
    private const val DEK_BYTES = 32
    private val SAMPLE_PLAINTEXT = "waffle".toByteArray()

    fun planOpen(context: Context): OpenPlanResult {
        val stateStore = EncryptionStateStore(context, prefsName = STATE_PREFS)
        val planner = DatabaseOpenPlanner(
            context = context,
            databaseFileName = DATABASE_FILE,
            legacyDatabaseFileName = null,
            recoveryPlaceholderFileName = RECOVERY_FILE,
            stateStore = stateStore,
            keyWrapper = InMemoryKeyWrapper(),
            probe = EncryptedDatabaseProbe { _, _ -> true },
        )
        val plan = planner.plan()
        return OpenPlanResult(
            fileName = plan.fileName,
            path = context.getDatabasePath(plan.fileName).path,
            encrypted = stateStore.isEncrypted,
            conversionState = stateStore.conversionState,
            hasOpenHelperFactory = plan.openHelperFactory != null,
            keyUnavailable = plan.keyUnavailable,
        )
    }

    fun wrapRoundTrip(password: String): WrapResult {
        val wrapper = PasswordKeyWrapper()
        val dek = ByteArray(DEK_BYTES).also { SecureRandom().nextBytes(it) }
        val wrapped = wrapper.wrap(dek, password.toCharArray())
        val restored = runCatching { wrapper.unwrap(wrapped, password.toCharArray()) }.getOrNull()
        val wrongRejected = runCatching { wrapper.unwrap(wrapped, (password + "x").toCharArray()) }.isFailure
        val encrypted = AesGcmCipher.encrypt(dek, SAMPLE_PLAINTEXT)
        val decrypted = runCatching { AesGcmCipher.decrypt(dek, encrypted.iv, encrypted.ciphertext) }.getOrNull()
        return WrapResult(
            roundTripOk = restored != null && restored.contentEquals(dek),
            wrongPasswordRejected = wrongRejected,
            iterations = wrapped.iterations,
            aesRoundTripOk = decrypted != null && decrypted.contentEquals(SAMPLE_PLAINTEXT),
        )
    }

    fun strengthTone(strength: PasswordStrength): DemoTone = when (strength) {
        PasswordStrength.WEAK -> DemoTone.Error
        PasswordStrength.MEDIUM -> DemoTone.Warning
        PasswordStrength.STRONG -> DemoTone.Success
    }

    @StringRes
    fun strengthLabel(strength: PasswordStrength): Int = when (strength) {
        PasswordStrength.WEAK -> R.string.libex_database_strength_weak
        PasswordStrength.MEDIUM -> R.string.libex_database_strength_medium
        PasswordStrength.STRONG -> R.string.libex_database_strength_strong
    }
}

internal object DatabaseTags {
    const val PASSWORD = "libex_database_password"
    const val STRENGTH = "libex_database_strength"
    const val PLAN = "libex_database_plan"
    const val PLAN_RESULT = "libex_database_plan_result"
    const val PLAN_FILE = "libex_database_plan_file"
    const val WRAP = "libex_database_wrap"
    const val WRAP_RESULT = "libex_database_wrap_result"
}

@Composable
internal fun DatabaseDemo(workDispatcher: CoroutineDispatcher = Dispatchers.Default) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf(DatabaseDemoLogic.DEFAULT_PASSWORD) }
    var plan by remember { mutableStateOf<OpenPlanResult?>(null) }
    var wrap by remember { mutableStateOf<WrapResult?>(null) }
    var wrapping by remember { mutableStateOf(false) }
    val strength = BackupPasswordStrength.evaluate(password)
    val yes = stringResource(R.string.libex_value_yes)
    val no = stringResource(R.string.libex_value_no)

    DemoSection(
        id = "database",
        titleRes = R.string.libex_database_title,
        descriptionRes = R.string.libex_database_desc,
        moduleRes = R.string.libex_module_database,
    ) {
        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.libex_database_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().testTag(DatabaseTags.PASSWORD),
        )
        DemoStatusPill(
            text = stringResource(
                R.string.libex_database_strength,
                stringResource(DatabaseDemoLogic.strengthLabel(strength)),
                BackupPasswordStrength.MIN_LENGTH,
            ),
            tone = DatabaseDemoLogic.strengthTone(strength),
            modifier = Modifier.testTag(DatabaseTags.STRENGTH),
        )
        AppButton(
            text = stringResource(R.string.libex_database_wrap),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Tonal,
            enabled = password.isNotEmpty() && !wrapping,
            onClick = {
                scope.launch {
                    wrapping = true
                    wrap = withContext(workDispatcher) { DatabaseDemoLogic.wrapRoundTrip(password) }
                    wrapping = false
                }
            },
            modifier = Modifier.testTag(DatabaseTags.WRAP),
        )
        wrap?.let { result ->
            DemoBodyText(
                text = stringResource(
                    R.string.libex_database_wrap_result,
                    if (result.roundTripOk) yes else no,
                    if (result.wrongPasswordRejected) yes else no,
                    if (result.aesRoundTripOk) yes else no,
                    result.iterations,
                ),
                modifier = Modifier.testTag(DatabaseTags.WRAP_RESULT),
            )
        }
        AppHorizontalDivider()
        AppButton(
            text = stringResource(R.string.libex_database_plan),
            role = AppButtonRole.Primary,
            variant = AppButtonVariant.Outlined,
            onClick = {
                scope.launch { plan = withContext(workDispatcher) { DatabaseDemoLogic.planOpen(context) } }
            },
            modifier = Modifier.testTag(DatabaseTags.PLAN),
        )
        plan?.let { result ->
            DemoKeyValue(
                label = stringResource(R.string.libex_database_plan_file),
                value = result.fileName,
                modifier = Modifier.testTag(DatabaseTags.PLAN_FILE),
            )
            DemoMetaText(text = result.path)
            DemoBodyText(
                text = stringResource(
                    R.string.libex_database_plan_result,
                    if (result.encrypted) yes else no,
                    result.conversionState.name,
                    if (result.hasOpenHelperFactory) yes else no,
                    if (result.keyUnavailable) yes else no,
                ),
                modifier = Modifier.testTag(DatabaseTags.PLAN_RESULT),
            )
        }
        DemoMetaText(
            text = stringResource(
                R.string.libex_database_defaults,
                DatabaseEncryptionDefaults.KEK_ALIAS,
                DatabaseEncryptionDefaults.STATE_PREFS_NAME,
            )
        )
    }
}
