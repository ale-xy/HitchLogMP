package org.gmautostop.hitchlogmp.di

import org.gmautostop.hitchlogmp.export.AndroidFileSaver
import org.gmautostop.hitchlogmp.export.FileSaver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<FileSaver> { AndroidFileSaver(androidContext()) }
}
