package di

import org.koin.dsl.module
import usecase.DeleteExpenseUseCase
import usecase.GetExpensesUseCase

val homeDomainModule = module {
    factory { GetExpensesUseCase(get()) }
    factory { DeleteExpenseUseCase(get()) }
}
