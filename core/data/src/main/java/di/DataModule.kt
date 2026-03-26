package di

import androidx.room.Room
import local.database.ExpenseDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import repository.BudgetRepository
import repository.BudgetRepositoryImpl
import repository.ExpenseRepository
import repository.ExpenseRepositoryImpl

val dataModule = module {
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            ExpenseDatabase::class.java,
            "expense_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAO
    single { get<ExpenseDatabase>().expenseDao() }
    single { get<ExpenseDatabase>().budgetDao() }

    // Repository
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
}
