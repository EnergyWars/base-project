package com.wafflehq.lib.uicore.query

import org.junit.Assert.assertEquals
import org.junit.Test

class SqlLikeEscaperTest {

    @Test
    fun `percent and underscore wildcards are escaped`() {
        assertEquals("100\\% done", escapeLikeWildcards("100% done"))
        assertEquals("a\\_b", escapeLikeWildcards("a_b"))
    }

    @Test
    fun `existing backslashes are escaped first to avoid double-escaping`() {
        assertEquals("a\\\\b", escapeLikeWildcards("a\\b"))
    }

    @Test
    fun `plain text is left unchanged`() {
        assertEquals("hello world", escapeLikeWildcards("hello world"))
    }

    @Test
    fun `likeContainsPattern wraps the escaped query with wildcards`() {
        assertEquals("%100\\%%", likeContainsPattern("100%"))
    }
}
