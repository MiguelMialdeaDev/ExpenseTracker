package di

import org.koin.dsl.module
import usecase.AddExpenseUseCase
import usecase.GetExpenseByIdUseCase
import usecase.UpdateExpenseUseCase

val addDomainModule = module {
    single { AddExpenseUseCase(get()) }
    single { GetExpenseByIdUseCase(get()) }
    single { UpdateExpenseUseCase(get()) }
}
