package di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import screen.AddExpenseViewModel

val addUiModule = module {
    viewModel { AddExpenseViewModel(get(), get(), get()) }
}
