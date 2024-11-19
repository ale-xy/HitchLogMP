package org.gmautostop.hitchlogmp.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.data.FirestoreRepository
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.ui.viewmodel.AuthViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.LogListViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.RecordViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        AuthService(Firebase.auth)
    }

    singleOf(::FirestoreRepository).bind<Repository>()

    viewModelOf(::AuthViewModel)
    viewModelOf(::LogListViewModel)
    viewModelOf(::EditLogViewModel)
    viewModel { HitchLogViewModel(get(), get()) }
    viewModel { params ->
        RecordViewModel( get(), params[0], params[1], params[2],)
    }
}