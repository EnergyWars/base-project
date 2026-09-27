package com.wafflehq.base.testutil

import android.content.Context
import android.content.res.AssetManager
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.IOException

object FakeAssetContext {

    fun withFiles(files: Map<String, String>): Context {
        val assets = mock<AssetManager>()
        whenever(assets.list("features")).thenReturn(files.keys.toTypedArray())
        files.forEach { (name, content) ->
            whenever(assets.open("features/$name")).thenAnswer { ByteArrayInputStream(content.toByteArray()) }
        }
        return contextOf(assets)
    }

    fun withMissingDirectory(): Context {
        val assets = mock<AssetManager>()
        whenever(assets.list("features")).thenReturn(null)
        return contextOf(assets)
    }

    fun failingToList(): Context {
        val assets = mock<AssetManager>()
        whenever(assets.list("features")).thenThrow(IOException("boom"))
        return contextOf(assets)
    }

    private fun contextOf(assets: AssetManager): Context {
        val context = mock<Context>()
        whenever(context.assets).thenReturn(assets)
        return context
    }
}
