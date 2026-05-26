package org.gmautostop.hitchlogmp.di

import org.gmautostop.hitchlogmp.export.FileSaver
import org.gmautostop.hitchlogmp.export.IosFileSaver
import org.koin.dsl.module

actual val platformModule = module {
    single<FileSaver> { IosFileSaver() }
}
