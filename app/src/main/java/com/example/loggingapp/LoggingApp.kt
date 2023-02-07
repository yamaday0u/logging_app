package com.example.loggingapp

import android.app.Application
import android.content.Context
import timber.log.Timber

class LoggingApp: Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(LogTree(this))
    }

    class LogTree(private val context: Context): Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            LogFile().postLog(context, message)
        }
    }
}