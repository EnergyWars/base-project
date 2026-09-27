package com.wafflehq.lib.diagnostics

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle

class AppLifecycleDiagnosticsLogger(
    private val logger: DiagnosticLogger
) : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) =
        log(activity, if (savedInstanceState != null) "onCreate (recreated)" else "onCreate")

    override fun onActivityStarted(activity: Activity) = log(activity, "onStart")
    override fun onActivityResumed(activity: Activity) = log(activity, "onResume")
    override fun onActivityPaused(activity: Activity) = log(activity, "onPause")
    override fun onActivityStopped(activity: Activity) = log(activity, "onStop")
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = log(activity, "onDestroy")

    override fun onTrimMemory(level: Int) {
        logger.w(TAG, "onTrimMemory level=$level")
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onLowMemory() {
        logger.w(TAG, "onLowMemory")
    }

    override fun onConfigurationChanged(newConfig: Configuration) = Unit

    private fun log(activity: Activity, event: String) {
        logger.d(TAG, "${activity.javaClass.simpleName} $event")
    }

    companion object {
        internal const val TAG = "Lifecycle"
    }
}
