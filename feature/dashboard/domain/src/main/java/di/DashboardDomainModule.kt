package di

import org.koin.dsl.module
import usecase.GetExpenseStatsUseCase
import usecase.GetExpensesByCategoryUseCase

val dashboardDomainModule = module {
    factory { GetExpenseStatsUseCase(get()) }
    factory { GetExpensesByCategoryUseCase(get()) }
}
