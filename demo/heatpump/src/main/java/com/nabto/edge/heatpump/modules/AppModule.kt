package com.nabto.edge.heatpump.modules

import android.app.Application
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class AppModule @Inject constructor(val application : Application) {
    @Provides
    fun provideApplication() : Application {
        return application
    }
}