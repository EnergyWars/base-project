package com.wafflehq.lib.uicore.query

private const val LIKE_ESCAPE_CHAR = '\\'

fun escapeLikeWildcards(query: String): String =
    query
        .replace(LIKE_ESCAPE_CHAR.toString(), LIKE_ESCAPE_CHAR.toString() + LIKE_ESCAPE_CHAR)
        .replace("%", "${LIKE_ESCAPE_CHAR}%")
        .replace("_", "${LIKE_ESCAPE_CHAR}_")

fun likeContainsPattern(query: String): String = "%${escapeLikeWildcards(query)}%"
