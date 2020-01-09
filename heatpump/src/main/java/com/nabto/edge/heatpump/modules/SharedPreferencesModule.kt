package com.nabto.edge.heatpump.modules

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class SharedPreferencesModule @Inject constructor(private val sharedPreferences : SharedPreferences) {
    @Provides
    fun provideSharedPreferences() : SharedPreferences {
        return sharedPreferences
    }
}