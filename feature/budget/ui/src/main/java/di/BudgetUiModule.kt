package di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import viewmodel.BudgetViewModel

val budgetUiModule = module {
    viewModel { BudgetViewModel(get(), get(), get(), get()) }
}
