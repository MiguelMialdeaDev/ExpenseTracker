package di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import viewmodel.FilterViewModel

val filterUiModule = module {
    viewModel { FilterViewModel(get()) }
}
