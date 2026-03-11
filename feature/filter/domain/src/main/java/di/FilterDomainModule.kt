package di

import org.koin.dsl.module
import usecase.GetFilteredExpensesUseCase

val filterDomainModule = module {
    factory { GetFilteredExpensesUseCase(get()) }
}
