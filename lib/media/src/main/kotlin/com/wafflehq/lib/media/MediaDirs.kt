package com.wafflehq.lib.media

object MediaDirs {
    const val FAKE_CALL = "fakecall"
    const val FAKE_CALL_IMAGES = "$FAKE_CALL/images"
    const val FAKE_CALL_AUDIO = "$FAKE_CALL/audio"
    const val VOCABULARY_IMAGES = "vocabulary/images"
    const val FINANCE_RECEIPTS = "finance/receipts"
    const val RECIPE_PHOTOS = "recipes/photos"
    const val VOICE_MEMO = "voicememo"

    val ALL: List<String> = listOf(
        FAKE_CALL_IMAGES,
        FAKE_CALL_AUDIO,
        VOCABULARY_IMAGES,
        FINANCE_RECEIPTS,
        RECIPE_PHOTOS,
        VOICE_MEMO
    )

    val ROOTS: List<String> = ALL.map { it.substringBefore('/') }.distinct()
}
