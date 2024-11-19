package org.gmautostop.hitchlogmp

import android.app.Application
import org.gmautostop.hitchlogmp.app.Initializer
import org.gmautostop.hitchlogmp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent

class MainApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@MainApplication)
        }
        Initializer.onApplicationStart()
    }
}