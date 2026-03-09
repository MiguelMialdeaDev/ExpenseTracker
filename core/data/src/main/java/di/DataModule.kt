package di

import androidx.room.Room
import local.database.ExpenseDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import repository.ExpenseRepository
import repository.ExpenseRepositoryImpl

val dataModule = module {
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            ExpenseDatabase::class.java,
            "expense_database"
        ).build()
    }

    // DAO
    single { get<ExpenseDatabase>().expenseDao() }

    // Repository
    single<ExpenseRepository> { ExpenseRepositoryImpl(get()) }
}
