package di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import screen.HomeViewModel

val homeUiModule = module {
    viewModel { HomeViewModel(get(), get()) }
}
