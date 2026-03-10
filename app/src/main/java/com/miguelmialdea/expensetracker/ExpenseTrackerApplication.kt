package com.miguelmialdea.expensetracker

import android.app.Application
import di.addDomainModule
import di.addUiModule
import di.dashboardDomainModule
import di.dashboardUiModule
import di.dataModule
import di.filterDomainModule
import di.filterUiModule
import di.homeDomainModule
import di.homeUiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ExpenseTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)

            androidContext(this@ExpenseTrackerApplication)

            modules(
                dataModule,

                // Feature add
                addDomainModule,
                addUiModule,

                // Feature dashboard
                dashboardDomainModule,
                dashboardUiModule,

                // Feature filter
                filterDomainModule,
                filterUiModule,

                // Feature home
                homeDomainModule,
                homeUiModule,
            )
        }
    }
}
