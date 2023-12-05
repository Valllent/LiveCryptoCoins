package com.valllent.websocket

import android.app.Application
import com.valllent.websocket.di.MainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TheApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin() {
            androidLogger()
            androidContext(this@TheApplication)
            modules(listOf(MainModule()))
        }
    }

}