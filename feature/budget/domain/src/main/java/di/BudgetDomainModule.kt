package di

import org.koin.dsl.module
import usecase.CreateBudgetUseCase
import usecase.DeleteBudgetUseCase
import usecase.GetAllBudgetStatusesUseCase
import usecase.GetAllBudgetsUseCase
import usecase.GetBudgetStatusUseCase
import usecase.UpdateBudgetUseCase

val budgetDomainModule = module {
    factory { CreateBudgetUseCase(get()) }
    factory { GetAllBudgetsUseCase(get()) }
    factory { UpdateBudgetUseCase(get()) }
    factory { DeleteBudgetUseCase(get()) }
    factory { GetBudgetStatusUseCase(get(), get()) }
    factory { GetAllBudgetStatusesUseCase(get(), get()) }
}
