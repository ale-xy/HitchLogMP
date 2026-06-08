package org.gmautostop.hitchlogmp.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.data.FirestoreRepository
import org.gmautostop.hitchlogmp.data.FirestoreSyncTracker
import org.gmautostop.hitchlogmp.data.SortPreferences
import org.gmautostop.hitchlogmp.domain.repository.Repository
import org.gmautostop.hitchlogmp.export.ChronicleFormatter
import org.gmautostop.hitchlogmp.export.ExportFormat
import org.gmautostop.hitchlogmp.ui.auth.AuthViewModel
import org.gmautostop.hitchlogmp.ui.auth.EmailLoginViewModel
import org.gmautostop.hitchlogmp.ui.auth.EmailRegisterViewModel
import org.gmautostop.hitchlogmp.ui.auth.ForgotPasswordViewModel
import org.gmautostop.hitchlogmp.ui.editlog.EditLogViewModel
import org.gmautostop.hitchlogmp.ui.export.CsvChronicleFormatter
import org.gmautostop.hitchlogmp.ui.export.HtmlChronicleFormatter
import org.gmautostop.hitchlogmp.ui.export.TextChronicleFormatter
import org.gmautostop.hitchlogmp.ui.export.XlsxChronicleFormatter
import org.gmautostop.hitchlogmp.ui.history.LogHistoryViewModel
import org.gmautostop.hitchlogmp.ui.history.RecordHistoryViewModel
import org.gmautostop.hitchlogmp.ui.hitchlog.HitchLogViewModel
import org.gmautostop.hitchlogmp.ui.loglist.LogListViewModel
import org.gmautostop.hitchlogmp.ui.recordedit.EditRecordViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single {
        AuthService(Firebase.auth)
    }

    singleOf(::FirestoreSyncTracker)
    singleOf(::FirestoreRepository).bind<Repository>()
    single { SortPreferences() }
    
    // Export formatters
    single<ChronicleFormatter>(named("text")) { TextChronicleFormatter() }
    single<ChronicleFormatter>(named("csv")) { CsvChronicleFormatter() }
    single<ChronicleFormatter>(named("html")) { HtmlChronicleFormatter() }
    single<ChronicleFormatter>(named("xlsx")) { XlsxChronicleFormatter() }
    
    // Formatter map for easy lookup
    single<Map<ExportFormat, ChronicleFormatter>> {
        mapOf(
            ExportFormat.Text to get(named("text")),
            ExportFormat.Csv to get(named("csv")),
            ExportFormat.Html to get(named("html")),
            ExportFormat.Xlsx to get(named("xlsx"))
        )
    }
    
    // Platform-specific file saver (defined in platform modules)
    // Android: single<FileSaver> { AndroidFileSaver(androidContext()) }
    // iOS: single<FileSaver> { IosFileSaver() }
    // JS: single<FileSaver> { JsFileSaver() }

    // Auth ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { EmailLoginViewModel(get(), get()) }
    viewModel { EmailRegisterViewModel(get(), get()) }
    viewModel { ForgotPasswordViewModel(get(), get()) }
    
    // Other ViewModels
    viewModel { LogListViewModel(get(), get(), get(), get()) }
    viewModel { params ->
        EditLogViewModel(params[0], get(), get())
    }
    viewModel { params ->
        HitchLogViewModel(params[0], get(), get(), get())
    }
    viewModel { params ->
        EditRecordViewModel(get(), params[0], params[1], params[2])
    }
    viewModel { params ->
        RecordHistoryViewModel(get(), params[0], params[1])
    }
    viewModel { params ->
        LogHistoryViewModel(get(), params[0])
    }
}