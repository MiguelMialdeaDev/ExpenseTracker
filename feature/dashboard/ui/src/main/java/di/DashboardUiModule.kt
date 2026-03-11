package di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import viewmodel.DashboardViewModel

val dashboardUiModule = module {
    viewModel { DashboardViewModel(get(), get()) }
}
