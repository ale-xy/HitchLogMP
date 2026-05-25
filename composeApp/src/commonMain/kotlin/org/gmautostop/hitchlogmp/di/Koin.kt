package org.gmautostop.hitchlogmp.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

expect val platformModule: org.koin.core.module.Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            appModule,
            platformModule
        )
    }
