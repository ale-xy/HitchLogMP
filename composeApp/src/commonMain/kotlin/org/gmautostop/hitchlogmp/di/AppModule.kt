package org.gmautostop.hitchlogmp.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.data.FirestoreRepository
import org.gmautostop.hitchlogmp.data.FirestoreSyncTracker
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.ui.EditLogViewModel
import org.gmautostop.hitchlogmp.ui.LogListViewModel
import org.gmautostop.hitchlogmp.ui.auth.AuthViewModel
import org.gmautostop.hitchlogmp.ui.auth.EmailLoginViewModel
import org.gmautostop.hitchlogmp.ui.auth.EmailRegisterViewModel
import org.gmautostop.hitchlogmp.ui.auth.ForgotPasswordViewModel
import org.gmautostop.hitchlogmp.ui.hitchlog.HitchLogViewModel
import org.gmautostop.hitchlogmp.ui.recordedit.EditRecordViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        AuthService(Firebase.auth)
    }

    singleOf(::FirestoreSyncTracker)
    singleOf(::FirestoreRepository).bind<Repository>()

    // Auth ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { EmailLoginViewModel(get(), get()) }
    viewModel { EmailRegisterViewModel(get(), get()) }
    viewModel { ForgotPasswordViewModel(get(), get()) }
    
    // Other ViewModels
    viewModel { LogListViewModel(get(), get(), get()) }
    viewModel { params ->
        EditLogViewModel(params[0], get(), get())
    }
    viewModel { params ->
        HitchLogViewModel(params[0], get())
    }
    viewModel { params ->
        EditRecordViewModel(get(), params[0], params[1], params[2])
    }
}