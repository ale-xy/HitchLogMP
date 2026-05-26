package org.gmautostop.hitchlogmp.di

import org.gmautostop.hitchlogmp.export.FileSaver
import org.gmautostop.hitchlogmp.export.JsFileSaver
import org.koin.dsl.module

actual val platformModule = module {
    single<FileSaver> { JsFileSaver() }
}
