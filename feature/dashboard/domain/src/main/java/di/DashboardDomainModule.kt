package di

import org.koin.dsl.module
import usecase.GetExpensesByCategoryUseCase
import usecase.GetExpenseStatsUseCase

val dashboardDomainModule = module {
    factory { GetExpenseStatsUseCase(get()) }
    factory { GetExpensesByCategoryUseCase(get()) }
}
